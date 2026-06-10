package ibrahima.dickoya.sendme.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration Swagger / OpenAPI 3.0 pour SendMe.
 *
 * Cette configuration génère automatiquement une documentation
 * interactive de toute l'API REST du projet.
 *
 * Accessible après démarrage sur :
 * - Interface graphique : http://localhost:8080/swagger-ui.html
 * - JSON brut          : http://localhost:8080/v3/api-docs
 *
 * Fonctionnalités configurées :
 * - Authentification JWT directement dans Swagger UI
 * - Informations du projet (titre, version, description)
 * - Coordonnées du développeur
 * - Serveurs disponibles (local, production)
 */
@Configuration
public class SwaggerConfig {

    /**
     * Nom du schéma de sécurité JWT utilisé dans Swagger.
     * Ce nom est référencé dans toutes les annotations @SecurityRequirement.
     */
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Crée et configure l'objet OpenAPI principal.
     *
     * Contient :
     * - Les métadonnées du projet (titre, version, description)
     * - Le schéma de sécurité JWT (Bearer Token)
     * - La liste des serveurs disponibles
     *
     * @return OpenAPI l'objet de configuration Swagger
     */
    @Bean
    public OpenAPI sendMeOpenAPI() {
        return new OpenAPI()
                // ── Informations générales du projet ────────────────────────
                .info(buildApiInfo())
                // ── Serveurs disponibles ─────────────────────────────────────
                .servers(buildServers())
                // ── Configuration du schéma de sécurité JWT ─────────────────
                .components(buildSecurityComponents())
                // ── Appliquer la sécurité JWT à toutes les routes ────────────
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }

    /**
     * Construit les informations générales de l'API.
     * Affichées en haut de la page Swagger UI.
     *
     * @return Info les métadonnées de l'API
     */
    private Info buildApiInfo() {
        return new Info()
                .title("SendMe API")
                .version("1.0.0")
                .description("""
                        ## SendMe — Passerelle de transfert d'argent multi-canaux
                        
                        API REST complète pour la gestion des transferts d'argent 
                        depuis une carte bancaire vers différents canaux de réception.
                        
                        ### Canaux supportés
                        - 🏦 Virement bancaire (SEPA/SWIFT)
                        - 🟠 Orange Money
                        - 🟡 MTN Mobile Money
                        - 🌊 Wave
                        - 📱 Moov Money
                        - 📡 Airtel Money
                        
                        ### Authentification
                        Cette API utilise l'authentification **JWT (Bearer Token)**.
                        1. Connectez-vous via `POST /api/auth/login`
                        2. Copiez le `accessToken` retourné
                        3. Cliquez sur **Authorize** en haut de la page
                        4. Entrez : `Bearer <votre_token>`
                        
                        ### Principe fondamental
                        La plateforme ne stocke aucun argent. Chaque débit validé
                        déclenche immédiatement un ordre d'envoi vers l'opérateur cible.
                        """)
                .contact(new Contact()
                        .name("Ibrahima Dickoya")
                        .email("ibrahima@sendme.com")
                        .url("https://github.com/ibrahima-dickoya/sendme-backend"))
                .license(new License()
                        .name("Projet académique TFE 2024-2025")
                        .url("https://github.com/ibrahima-dickoya/sendme-backend"));
    }

    /**
     * Construit la liste des serveurs disponibles.
     * Permet de tester l'API sur différents environnements
     * directement depuis Swagger UI.
     *
     * @return List<Server> la liste des serveurs
     */
    private List<Server> buildServers() {
        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Serveur de développement local");

        Server productionServer = new Server()
                .url("https://api.sendme.com")
                .description("Serveur de production");

        return List.of(localServer, productionServer);
    }

    /**
     * Configure le schéma de sécurité JWT pour Swagger UI.
     *
     * Permet aux développeurs de s'authentifier directement
     * dans l'interface Swagger en entrant leur token JWT.
     * Le token est ensuite ajouté automatiquement dans le
     * header Authorization de toutes les requêtes.
     *
     * @return Components les composants de sécurité
     */
    private Components buildSecurityComponents() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                // Type HTTP avec schéma Bearer
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                // Format JWT pour l'affichage dans Swagger
                                .bearerFormat("JWT")
                                // Nom affiché dans Swagger UI
                                .name(SECURITY_SCHEME_NAME)
                                // Description affichée dans le popup d'auth
                                .description(
                                        "Entrez votre token JWT obtenu via " +
                                                "POST /api/auth/login. " +
                                                "Format : Bearer <token>")
                );
    }
}