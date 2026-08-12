package ibrahima.dickoya.sendme.config;

import ibrahima.dickoya.sendme.repository.BlockedIpRepository;
import ibrahima.dickoya.sendme.security.JwtAuthenticationFilter;
import ibrahima.dickoya.sendme.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final BlockedIpRepository blockedIpRepository;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    private static final String[] PUBLIC_ROUTES = {
            // Authentification
            "/api/auth/**",

            // Simulation de transfert (page d'accueil publique)
            "/api/transfers/simulate",

            // Vérification publique d'un code de parrainage
            "/api/referral/verify",

            // Stripe Webhook
            "/api/webhooks/**",

            // Health check public (keep-alive contre le cold start Railway)
            "/api/health",

            // Swagger UI / OpenAPI / Actuator
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/actuator",
            "/actuator/**"
    };

    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter(blockedIpRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RateLimitingFilter rateLimitingFilter)
            throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Autoriser le mode asynchrone (SSE)
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ASYNC).permitAll()

                        // Autoriser les requêtes de pré-vérification OPTIONS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Routes publiques
                        .requestMatchers(PUBLIC_ROUTES).permitAll()

                        // Administration
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Tout le reste est sécurisé
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                // Filtre JWT : intercepte toutes les requêtes pour extraire le token
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Filtre Rate Limiting : protège les endpoints d'auth contre le brute-force
                .addFilterBefore(rateLimitingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
    
        // Traitement et nettoyage strict des origines
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .map(s -> s.replace("\"", ""))
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    
        configuration.setAllowedOrigins(origins);
    
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Cache-Control",
                "X-Requested-With",
                "Accept",
                "Origin"));
    
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "X-Refresh-Token"));
    
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(Duration.ofHours(1));
    
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
