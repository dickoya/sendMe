package ibrahima.dickoya.sendme.config;

import ibrahima.dickoya.sendme.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuration principale de Spring Security pour SendMe.
 *
 * Définit :
 * - Les routes publiques (auth, swagger, webhooks)
 * - Les routes admin (ROLE_ADMIN uniquement)
 * - La politique de session (STATELESS — JWT)
 * - Le filtre JWT
 * - Le provider d'authentification BCrypt
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Liste complète des routes publiques accessibles sans JWT.
     * Swagger nécessite plusieurs patterns pour fonctionner correctement.
     */
    private static final String[] PUBLIC_ROUTES = {

            // ── Authentification ──────────────────────────────────────────────
            "/api/auth/**",

            // ── Stripe Webhook ────────────────────────────────────────────────
            // Sécurisé par signature Stripe, pas par JWT
            "/api/webhooks/**",
            "/api/webhooks/stripe",

            // ── Swagger UI ────────────────────────────────────────────────────
            // Interface graphique interactive
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-ui/index.html",

            // ── OpenAPI JSON/YAML ─────────────────────────────────────────────
            // Fichier de définition de l'API
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",

            // ── SpringDoc ressources statiques ────────────────────────────────
            // Fichiers JS, CSS, images de Swagger UI
            "/swagger-resources",
            "/swagger-resources/**",
            "/webjars/**",
            "/configuration/**",
            "/configuration/ui",
            "/configuration/security",

            // ── Actuator (optionnel — monitoring) ─────────────────────────────
            "/actuator",
            "/actuator/**"
    };

    /**
     * Configure la chaîne de filtres de sécurité.
     *
     * Ordre des règles :
     * 1. Désactiver CSRF (API stateless — pas de session)
     * 2. Autoriser les routes publiques (Swagger, Auth, Webhooks)
     * 3. Restreindre les routes admin au rôle ADMIN
     * 4. Exiger JWT pour tout le reste
     * 5. Session stateless (pas de HttpSession)
     * 6. Ajouter le filtre JWT avant UsernamePasswordAuthenticationFilter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Activer et lier la configuration CORS globale
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Désactiver CSRF — inutile pour une API REST stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Configurer les autorisations par route
                .authorizeHttpRequests(auth -> auth
                        // ── LA LIGNE MAGIQUE : Autoriser toutes les requêtes de vérification OPTIONS ──
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Routes publiques — aucun JWT requis ──────────────────
                        .requestMatchers(PUBLIC_ROUTES).permitAll()

                        // ── Routes admin — ROLE_ADMIN requis ─────────────────────
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // ── Toutes les autres routes — JWT requis ─────────────────
                        .anyRequest().authenticated()
                )

                // Session STATELESS
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    /**
     * Configuration CORS globale pour l'ensemble de l'application Spring Security.
     * C'est elle qui va injecter les bons headers requis par ton application React (Vite).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Autorise ton application Frontend
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // Autorise les méthodes standards
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Autorise l'envoi des headers comme "Authorization" (essentiel pour ton JWT) et "Content-Type"
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control"));
        // Permet l'échange de cookies ou d'en-têtes de session si nécessaire
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    /**
     * Configure le provider d'authentification.
     * Utilise BCrypt pour vérifier les mots de passe
     * et UserDetailsService pour charger les utilisateurs.
     *
     * @return DaoAuthenticationProvider configuré
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Bean AuthenticationManager exposé pour l'injection dans AuthService.
     * Nécessaire pour appeler authenticationManager.authenticate()
     * lors du login.
     *
     * @param config configuration d'authentification Spring
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Bean PasswordEncoder utilisant BCrypt.
     * Coût par défaut : 10 (recommandé pour la sécurité).
     * Plus le coût est élevé, plus le hachage est lent et sécurisé.
     *
     * @return BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}