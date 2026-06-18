package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.AuthResponse;
import ibrahima.dickoya.sendme.entity.LoginHistory;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.Role;
import ibrahima.dickoya.sendme.exception.EmailAlreadyExistsException;
import ibrahima.dickoya.sendme.exception.RateLimitExceededException;
import ibrahima.dickoya.sendme.repository.LoginHistoryRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.security.JwtService;
import ibrahima.dickoya.sendme.security.RateLimitingFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;
    private final RateLimitingFilter rateLimitingFilter;
    private final LoginHistoryRepository loginHistoryRepository;

    // ── Compteurs de tentatives (email) ──────────────────────────────────────
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();

    // ── Exponential backoff par IP ───────────────────────────────────────────
    private static final int BACKOFF_THRESHOLD = 3; // Délai progressif après 3 échecs
    private final Map<String, Integer> ipFailedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> ipLastFailedTime = new ConcurrentHashMap<>();

    // ── Rate limiting pour le refresh token ──────────────────────────────────
    private static final int MAX_REFRESH_PER_MINUTE = 5;
    private final Map<String, Integer> refreshCounts = new ConcurrentHashMap<>();
    private final Map<String, Instant> refreshWindowStart = new ConcurrentHashMap<>();

    // ── Inscription ──────────────────────────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Un compte existe deja avec l'email : " + request.getEmail());
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("Nouvel utilisateur enregistre : {}", user.getEmail());

        try {
            emailService.sendWelcomeEmail(user);
        } catch (Exception e) {
            log.error("[EMAIL] Impossible d'envoyer l'email de bienvenue : {}", e.getMessage());
        }

        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        var accessToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    // ── Connexion ────────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);

        // 1. Vérifier l'exponential backoff avant même de tenter l'auth
        applyExponentialBackoff(ipAddress);

        try {
            // 2. Authentification Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            // 3. Récupération de l'utilisateur
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

            if (!user.getIsActive()) {
                throw new DisabledException("Compte desactive. Contactez l'administrateur.");
            }

            // Réinitialiser les compteurs d'échecs
            loginAttempts.remove(request.getEmail());
            ipFailedAttempts.remove(ipAddress);
            ipLastFailedTime.remove(ipAddress);

            // 4. Génération des jetons JWT
            var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            var accessToken = jwtService.generateToken(userDetails);
            var refreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("Connexion reussie pour : {} depuis IP : {}", user.getEmail(), ipAddress);

            // 5. Enregistrer l'historique de connexion
            try {
                String userAgent = httpRequest.getHeader("User-Agent");
                LoginHistory loginHistory = LoginHistory.builder()
                        .user(user)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .success(true)
                        .build();
                loginHistoryRepository.save(loginHistory);
            } catch (Exception e) {
                log.error("[AUTH] Échec de l'enregistrement de l'historique de connexion : {}", e.getMessage());
            }

            // 6. Envoi sécurisé de l'alerte e-mail
            try {
                final String finalIpAddress = ipAddress;
                emailService.sendLoginAlertEmail(user, finalIpAddress);
            } catch (Exception e) {
                log.error("[EMAIL] Échec de l'envoi de l'alerte de connexion : {}", e.getMessage());
            }

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400000L)
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().name())
                    .build();

        } catch (BadCredentialsException e) {
            // Enregistrer l'échec pour l'IP (exponential backoff)
            recordIpFailure(ipAddress);

            // Signaler l'échec au RateLimitingFilter pour blocage IP si seuil atteint
            rateLimitingFilter.recordFailedAttempt(ipAddress);

            if (request.getEmail() != null) {
                // Compteur par email (5 tentatives max)
                int currentAttempts = loginAttempts.merge(request.getEmail(), 1, Integer::sum);
                log.warn("Tentative de connexion echouee pour : {} — tentative n°{} depuis IP : {}",
                        request.getEmail(), currentAttempts, ipAddress);

                // Alerte email si seuil atteint
                if (currentAttempts >= MAX_LOGIN_ATTEMPTS) {
                    final String finalIpForSuspicious = ipAddress;
                    try {
                        userRepository.findByEmail(request.getEmail())
                                .ifPresent(user -> emailService.sendSuspiciousLoginEmail(user, finalIpForSuspicious,
                                        currentAttempts));
                    } catch (Exception ex) {
                        log.error("[EMAIL] Impossible d'envoyer l'alerte de sécurité : {}", ex.getMessage());
                    }
                    loginAttempts.put(request.getEmail(), 0);
                }
            }
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }
    }

    // ── Rafraîchissement de token ───────────────────────────────────────────
    public AuthResponse refresh(String refreshToken) {
        // 1. Extraire l'email depuis le refresh token
        String email = jwtService.extractUsername(refreshToken);

        // 2. Rate limiting : max 5 refresh par minute par email
        checkRefreshRateLimit(email);

        // 3. Charger l'utilisateur
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 4. Vérifier que le refresh token est valide
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            log.warn("[AUTH] Tentative de refresh avec un token invalide ou expiré pour : {}", email);
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }

        // 5. Vérifier que le compte est toujours actif
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!user.getIsActive()) {
            log.warn("[AUTH] Tentative de refresh pour un compte désactivé : {}", email);
            throw new IllegalArgumentException("Compte désactivé. Contactez l'administrateur.");
        }

        // 6. Générer un nouveau jeu de tokens
        var newAccessToken = jwtService.generateToken(userDetails);
        var newRefreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("[AUTH] Tokens rafraîchis avec succès pour : {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    // ── Exponential Backoff ──────────────────────────────────────────────────
    /**
     * Applique un délai progressif (exponential backoff) basé sur le nombre
     * d'échecs consécutifs depuis une même IP.
     * <p>
     * Après 3 échecs : délai de 1s
     * Après 4 échecs : délai de 2s
     * Après 5 échecs : délai de 4s
     * Après 6+ échecs : délai de 8s
     */
    private void applyExponentialBackoff(String ipAddress) {
        Integer failedCount = ipFailedAttempts.get(ipAddress);
        if (failedCount == null || failedCount < BACKOFF_THRESHOLD) {
            return; // Pas de délai en dessous du seuil
        }

        // Calculer le délai : 2^(failedCount - BACKOFF_THRESHOLD) secondes
        // Plafonné à 8 secondes max
        int delaySeconds = Math.min(
                (int) Math.pow(2, failedCount - BACKOFF_THRESHOLD),
                8);

        Instant lastFailed = ipLastFailedTime.get(ipAddress);
        if (lastFailed != null) {
            Instant now = Instant.now();
            long elapsedSeconds = java.time.Duration.between(lastFailed, now).getSeconds();
            if (elapsedSeconds < delaySeconds) {
                long waitMs = (delaySeconds - elapsedSeconds) * 1000;
                log.debug("[BACKOFF] IP {} : attente de {}ms avant nouvelle tentative", ipAddress, waitMs);
                try {
                    Thread.sleep(waitMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Enregistre un échec pour une IP et met à jour le timestamp.
     */
    private void recordIpFailure(String ipAddress) {
        ipFailedAttempts.merge(ipAddress, 1, Integer::sum);
        ipLastFailedTime.put(ipAddress, Instant.now());
    }

    // ── Refresh Rate Limiting ────────────────────────────────────────────────
    /**
     * Vérifie que le nombre de refresh par minute pour un email ne dépasse pas
     * la limite autorisée (5/min).
     */
    private void checkRefreshRateLimit(String email) {
        Instant now = Instant.now();
        Instant windowStart = refreshWindowStart.get(email);

        // Réinitialiser la fenêtre si elle est expirée (plus d'une minute)
        if (windowStart == null || java.time.Duration.between(windowStart, now).getSeconds() >= 60) {
            refreshWindowStart.put(email, now);
            refreshCounts.put(email, 1);
            return;
        }

        // Incrémenter le compteur dans la fenêtre actuelle
        int count = refreshCounts.merge(email, 1, Integer::sum);

        if (count > MAX_REFRESH_PER_MINUTE) {
            long retryAfter = 60 - java.time.Duration.between(windowStart, now).getSeconds();
            log.warn("[AUTH] Rate limit de refresh dépassé pour : {} — {} tentatives en 1 min", email, count);
            throw new RateLimitExceededException(
                    "Trop de tentatives de rafraîchissement. Veuillez réessayer dans " + retryAfter + " secondes.",
                    retryAfter);
        }
    }

    // ── Utilitaires ──────────────────────────────────────────────────────────
    /**
     * Extrait l'adresse IP réelle du client en tenant compte des proxys.
     */
    private String extractIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}
