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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    /**
     * Service email injecté pour envoyer l'email de bienvenue
     * après l'inscription d'un nouvel utilisateur.
     */
    private final EmailService emailService;

    // Inscription
    public AuthResponse register(RegisterRequest request) {
        // Verifier unicite email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Un compte existe deja avec l'email : " + request.getEmail());
        }

        // Creer l'utilisateur
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
        // Envoyer l'email de bienvenue en arrière-plan (@Async)
        // Ne bloque pas la réponse HTTP si l'email échoue
        emailService.sendWelcomeEmail(user);

        // Generer les tokens
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

    /**
     * Authentifie un utilisateur et génère ses tokens JWT.
     *
     * Sécurité intégrée :
     * - Comptage des tentatives échouées par email
     * - Email d'alerte à chaque connexion réussie
     * - Email d'alerte de sécurité après MAX_LOGIN_ATTEMPTS échecs
     *
     * @param request   les identifiants de connexion
     * @param httpRequest la requête HTTP pour extraire l'IP
     * @return AuthResponse avec les tokens JWT
     */
    public AuthResponse login(LoginRequest request,
                              jakarta.servlet.http.HttpServletRequest httpRequest) {

        // Récupérer l'adresse IP de la requête
        // X-Forwarded-For est utilisé quand l'app est derrière un proxy/nginx
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        try {
            // Vérifier les credentials via Spring Security
            // Lance BadCredentialsException si email/mdp incorrect
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Credentials valides → récupérer l'utilisateur
            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException(
                            "Utilisateur non trouve"));

            // Vérifier que le compte est actif
            if (!user.getIsActive()) {
                throw new RuntimeException(
                        "Compte desactive. Contactez l'administrateur.");
            }

            // ── CONNEXION RÉUSSIE ────────────────────────────────────────────

            // Remettre le compteur de tentatives à zéro
            loginAttempts.remove(request.getEmail());

            // Générer les tokens JWT
            var userDetails = userDetailsService
                    .loadUserByUsername(user.getEmail());
            var accessToken = jwtService.generateToken(userDetails);
            var refreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("Connexion reussie pour : {} depuis IP : {}",
                    user.getEmail(), ipAddress);

            // Envoyer l'email d'alerte de connexion en arrière-plan
            final String finalIpAddress = ipAddress;
            emailService.sendLoginAlertEmail(user, finalIpAddress);

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

        } catch (org.springframework.security.authentication
                         .BadCredentialsException e) {

            // ── CONNEXION ÉCHOUÉE ────────────────────────────────────────────

            // Incrémenter le compteur de tentatives pour cet email
            int attempts = loginAttempts.merge(
                    request.getEmail(), 1, Integer::sum);

            log.warn("Tentative de connexion echouee pour : {} — tentative n°{}",
                    request.getEmail(), attempts);

            // Si le seuil est atteint, envoyer l'alerte de sécurité
            if (attempts >= MAX_LOGIN_ATTEMPTS) {
                // Chercher l'utilisateur pour l'email (peut ne pas exister)
                final String finalIpForSuspicious = ipAddress;
                userRepository.findByEmail(request.getEmail())
                        .ifPresent(user ->
                                emailService.sendSuspiciousLoginEmail(
                                        user, finalIpForSuspicious, attempts)
                        );

                // Remettre le compteur à zéro après l'alerte
                loginAttempts.put(request.getEmail(), 0);
            }

            // Toujours retourner le même message (sécurité)
            throw new org.springframework.security.authentication
                    .BadCredentialsException(
                    "Email ou mot de passe incorrect");
        }
    }
    /**
     * Compteur des tentatives de connexion échouées par email.
     * Clé   : email de l'utilisateur
     * Valeur : nombre de tentatives échouées consécutives
     *
     * ConcurrentHashMap est thread-safe pour les requêtes concurrentes.
     * Le compteur est remis à zéro après une connexion réussie.
     *
     * Note : En production, ce compteur serait stocké dans Redis
     * pour persister entre les redémarrages du serveur.
     */
    private final Map<String, Integer> loginAttempts =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Nombre maximum de tentatives avant l'envoi de l'alerte de sécurité.
     * Au-delà de ce seuil, un email d'alerte est envoyé à l'utilisateur.
     */
    private static final int MAX_LOGIN_ATTEMPTS = 5;
}