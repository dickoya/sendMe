package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.AuthResponse;
import ibrahima.dickoya.sendme.entity.LoginHistory;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.Role;
import ibrahima.dickoya.sendme.exception.EmailAlreadyExistsException;
import ibrahima.dickoya.sendme.exception.EmailNotVerifiedException;
import ibrahima.dickoya.sendme.repository.LoginHistoryRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

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
    private final LoginHistoryRepository loginHistoryRepository;
    private final ReferralService referralService;
    private final LoginAttemptService loginAttemptService;
    private final IpBlockingService ipBlockingService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;

    // ── Durée de validité du token de vérification email (48h) ──────────────
    private static final long EMAIL_VERIFICATION_TOKEN_EXPIRY_HOURS = 48;

    // ── Inscription ──────────────────────────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Un compte existe deja avec l'email : " + request.getEmail());
        }

        // Générer un code de parrainage unique
        String referralCode = referralService.generateReferralCode();

        // Générer un token de vérification email unique
        String verificationToken = UUID.randomUUID().toString();

        var user = User.builder()
                .referralCode(referralCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.ROLE_USER)
                .isActive(true)
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(EMAIL_VERIFICATION_TOKEN_EXPIRY_HOURS))
                .build();

        userRepository.save(user);

        // Appliquer le code de parrainage si fourni
        if (request.getReferralCode() != null && !request.getReferralCode().isBlank()) {
            referralService.applyReferralCode(user, request.getReferralCode());
        }

        log.info("Nouvel utilisateur enregistre : {} (code parrainage: {}, email non vérifié)", user.getEmail(),
                referralCode);

        // Enregistrer un log d'audit
        auditService.logEvent("AUTH", "REGISTER", user.getEmail(), user.getId(), "SUCCESS",
                "Nouvel utilisateur inscrit : " + user.getEmail());

        // Envoyer l'email de vérification (au lieu de l'email de bienvenue)
        try {
            emailService.sendEmailVerificationEmail(user, verificationToken);
        } catch (Exception e) {
            log.error("[EMAIL] Impossible d'envoyer l'email de vérification : {}", e.getMessage());
        }

        // Retourner une réponse sans tokens JWT — l'utilisateur doit d'abord vérifier
        // son email
        return AuthResponse.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .referralCode(user.getReferralCode())
                .emailVerified(false)
                .build();
    }

    // ── Vérification d'email ─────────────────────────────────────────────────
    /**
     * Valide l'email d'un utilisateur à partir d'un token de vérification.
     *
     * @param token le token de vérification envoyé par email
     * @return AuthResponse avec les tokens JWT si la validation réussit
     * @throws IllegalArgumentException si le token est invalide ou expiré
     */
    public AuthResponse verifyEmail(String token) {
        var user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de vérification invalide."));

        // Vérifier si le token a expiré
        if (user.getEmailVerificationTokenExpiresAt() == null ||
                user.getEmailVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("[AUTH] Token de vérification expiré pour : {}", user.getEmail());
            throw new IllegalArgumentException("Le lien de vérification a expiré. Veuillez demander un nouveau lien.");
        }

        // Vérifier que le compte n'est pas déjà vérifié
        if (user.getEmailVerified()) {
            log.info("[AUTH] Email déjà vérifié pour : {}", user.getEmail());
            // Générer quand même les tokens pour connecter l'utilisateur
        } else {
            // Marquer l'email comme vérifié
            user.setEmailVerified(true);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiresAt(null);
            userRepository.save(user);

            log.info("[AUTH] Email vérifié avec succès pour : {}", user.getEmail());

            // Envoyer l'email de bienvenue après validation
            try {
                emailService.sendWelcomeEmail(user);
            } catch (Exception e) {
                log.error("[EMAIL] Impossible d'envoyer l'email de bienvenue : {}", e.getMessage());
            }
        }

        // Générer les tokens JWT pour connecter automatiquement l'utilisateur
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
                .referralCode(user.getReferralCode())
                .emailVerified(true)
                .build();
    }

    // ── Renvoi de l'email de vérification ────────────────────────────────────
    /**
     * Renvoie l'email de vérification à un utilisateur dont l'email n'est pas
     * encore vérifié.
     *
     * @param email l'adresse email de l'utilisateur
     */
    public void resendVerificationEmail(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte trouvé avec cet email."));

        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("Cet email est déjà vérifié. Vous pouvez vous connecter.");
        }

        // Générer un nouveau token
        String newToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(newToken);
        user.setEmailVerificationTokenExpiresAt(LocalDateTime.now().plusHours(EMAIL_VERIFICATION_TOKEN_EXPIRY_HOURS));
        userRepository.save(user);

        log.info("[AUTH] Nouveau token de vérification généré pour : {}", email);

        // Envoyer l'email
        try {
            emailService.sendEmailVerificationEmail(user, newToken);
        } catch (Exception e) {
            log.error("[EMAIL] Impossible d'envoyer l'email de vérification : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification.");
        }
    }

    // ── Connexion ────────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);

        // 1. Vérifier l'exponential backoff avant même de tenter l'auth
        ipBlockingService.applyBackoff(ipAddress);

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

            // 4. Vérifier que l'email a été vérifié
            if (!user.getEmailVerified()) {
                log.warn("[AUTH] Tentative de connexion pour un email non vérifié : {}", user.getEmail());
                throw new EmailNotVerifiedException(
                        "Veuillez vérifier votre adresse email avant de vous connecter. " +
                                "Un email de vérification vous a été envoyé lors de l'inscription.",
                        user.getEmail());
            }

            // Réinitialiser les compteurs d'échecs
            loginAttemptService.resetAttempts(request.getEmail());
            ipBlockingService.resetForIp(ipAddress);

            // 5. Génération des jetons JWT
            var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            var accessToken = jwtService.generateToken(userDetails);
            var refreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("Connexion reussie pour : {} depuis IP : {}", user.getEmail(), ipAddress);

            // Enregistrer un log d'audit pour la connexion
            auditService.logEvent("AUTH", "LOGIN", user.getEmail(), user.getId(), "SUCCESS",
                    "Connexion réussie depuis " + ipAddress);

            // 6. Enregistrer l'historique de connexion
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

            // 7. Enregistrer un log d'audit si c'est un admin qui se connecte
            if (user.getRole() == Role.ROLE_ADMIN) {
                auditService.logEvent("ADMIN", "LOGIN", user.getEmail(), user.getId(), "SUCCESS",
                        "Connexion admin depuis " + ipAddress);
            }

            // 8. Envoi sécurisé de l'alerte e-mail
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
                    .referralCode(user.getReferralCode())
                    .emailVerified(true)
                    .build();

        } catch (BadCredentialsException e) {
            // Enregistrer l'échec pour l'IP (exponential backoff + blocage IP)
            ipBlockingService.recordFailure(ipAddress);

            // Compteur par email (5 tentatives max) + alerte email si seuil atteint
            if (request.getEmail() != null) {
                loginAttemptService.recordFailedAttempt(request.getEmail(), ipAddress);
            }
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }
    }

    // ── Rafraîchissement de token ───────────────────────────────────────────
    public AuthResponse refresh(String refreshToken) {
        // 1. Extraire l'email depuis le refresh token
        String email = jwtService.extractUsername(refreshToken);

        // 2. Rate limiting : max 5 refresh par minute par email
        refreshTokenService.checkRateLimit(email);

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
                .referralCode(user.getReferralCode())
                .emailVerified(user.getEmailVerified())
                .build();
    }

    // ── Utilitaires ──────────────────────────────────────────────────────────

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
