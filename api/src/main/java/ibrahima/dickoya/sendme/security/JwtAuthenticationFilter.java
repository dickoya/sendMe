package ibrahima.dickoya.sendme.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Utilisation de la nouvelle méthode d'extraction hybride (Header ou Query
        // Param)
        final String jwt = extractToken(request);
        final String userEmail;

        // Pas de token trouvé — on laisse passer la requête vers le filtre suivant
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userEmail = jwtService.extractUsername(jwt);

            // Si un utilisateur est trouvé et qu'il n'est pas encore authentifié dans le
            // contexte
            if (userEmail != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                var userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token invalide ou expiré — on laisse Spring Security gérer la suite (ex:
            // renvoyer un 401/403)
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT de la requête.
     * Uniquement depuis le header "Authorization: Bearer <token>".
     * 
     * Le token n'est PLUS accepté en query param pour des raisons de sécurité :
     * - Les URLs sont loggées par les serveurs proxy, reverse proxies, load
     * balancers
     * - Les URLs sont stockées dans l'historique du navigateur
     * - Le token peut fuiter via le Referer header
     * 
     * La connexion SSE utilise désormais fetch() avec le header Authorization
     * standard au lieu d'EventSource (qui ne supporte pas les headers
     * personnalisés).
     * 
     * @param request la requête HTTP
     * @return le token JWT, ou null si absent
     */
    private String extractToken(HttpServletRequest request) {
        // Uniquement le header Authorization standard
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}