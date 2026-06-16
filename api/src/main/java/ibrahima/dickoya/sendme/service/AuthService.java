package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.AuthResponse;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.Role;
import ibrahima.dickoya.sendme.exception.EmailAlreadyExistsException;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // Déclaration des constantes et compteurs EN HAUT de la classe
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();

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
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        try {
            // 1. Authentification Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            // 2. Récupération de l'utilisateur
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

            if (!user.getIsActive()) {
                throw new DisabledException("Compte desactive. Contactez l'administrateur.");
            }

            // Réinitialiser le compteur d'échecs
            loginAttempts.remove(request.getEmail());

            // 3. Génération des jetons JWT
            var userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            var accessToken = jwtService.generateToken(userDetails);
            var refreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("Connexion reussie pour : {} depuis IP : {}", user.getEmail(), ipAddress);

            // 4. Envoi sécurisé de l'alerte e-mail (ne doit pas bloquer le login en cas de
            // panne SMTP)
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
            if (request.getEmail() != null) {
                // 1. On calcule le nombre de tentatives directement
                int currentAttempts = loginAttempts.merge(request.getEmail(), 1, Integer::sum);
                log.warn("Tentative de connexion echouee pour : {} — tentative n°{}", request.getEmail(),
                        currentAttempts);

                // 2. Si le seuil est atteint, on envoie l'alerte
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
    /**
     * Valide un refresh token et génère un nouveau jeu de tokens (access +
     * refresh).
     * 
     * @param refreshToken le refresh token à valider
     * @return AuthResponse contenant les nouveaux tokens
     * @throws IllegalArgumentException si le refresh token est invalide ou expiré
     */
    public AuthResponse refresh(String refreshToken) {
        // 1. Extraire l'email depuis le refresh token
        String email = jwtService.extractUsername(refreshToken);

        // 2. Charger l'utilisateur
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 3. Vérifier que le refresh token est valide
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            log.warn("[AUTH] Tentative de refresh avec un token invalide ou expiré pour : {}", email);
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }

        // 4. Vérifier que le compte est toujours actif
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (!user.getIsActive()) {
            log.warn("[AUTH] Tentative de refresh pour un compte désactivé : {}", email);
            throw new IllegalArgumentException("Compte désactivé. Contactez l'administrateur.");
        }

        // 5. Générer un nouveau jeu de tokens
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
}
