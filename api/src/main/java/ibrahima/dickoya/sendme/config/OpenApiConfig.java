
package ibrahima.dickoya.sendme.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI (Swagger) pour la documentation de l'API SendMe.
 *
 * Définit les métadonnées globales de l'API :
 * - Titre, description et version
 * - Contact du développeur
 * - Licence
 * - Serveur de développement
 * - Schéma d'authentification JWT (Bearer)
 *
 * Accessible sur : http://localhost:8080/swagger-ui/index.html
 * JSON de spécification : http://localhost:8080/v3/api-docs
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "SendMe API", version = "1.0.0", description = """
                API REST de SendMe — Passerelle de Transfert d'Argent Direct Multi-Canaux.

                ## Vue d'ensemble
                SendMe permet à un utilisateur d'envoyer de l'argent depuis sa carte bancaire
                vers un bénéficiaire, soit par virement bancaire, soit via un opérateur
                Mobile Money (Orange Money, MTN, Wave, Moov, Airtel).

                ## Authentification
                La plupart des endpoints nécessitent un token JWT dans le header `Authorization` :
                ```
                Authorization: Bearer <votre-token-jwt>
                ```

                Obtenez un token via :
                - `POST /api/auth/register` — création de compte
                - `POST /api/auth/login` — connexion
                - `POST /api/auth/refresh` — rafraîchissement du token

                ## Flux de transfert typique
                1. Simuler les frais : `POST /api/transfers/simulate`
                2. Créer un bénéficiaire : `POST /api/beneficiaries`
                3. Initier le transfert : `POST /api/transfers` (avec Idempotency-Key)
                4. Suivre via SSE : `GET /api/notifications/stream`
                5. Télécharger le reçu : `GET /api/transactions/{id}/receipt`

                ## Cartes de test Stripe (mode sandbox)
                | Carte | Résultat |
                |------|---------|
                | `4242 4242 4242 4242` | Paiement réussi |
                | `4000 0000 0000 0002` | Paiement refusé |
                | `4000 0025 0000 3155` | 3D Secure requis |

                ## Compte administrateur par défaut
                - Email : `admin@sendme.com`
                - Mot de passe : `Admin@sendme2025`
                """, contact = @Contact(name = "Ibrahima Dickoya", email = "ibrahima@sendme.com", url = "https://github.com/dickoya/sendMe"), license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")), servers = {
                @Server(url = "http://localhost:8080", description = "Serveur de développement local")
})
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Token JWT obtenu via /api/auth/login ou /api/auth/register. "
                +
                "Format : 'Bearer <token>'")
public class OpenApiConfig {
        // La configuration est entièrement déclarative via les annotations ci-dessus.
        // SpringDoc détecte automatiquement cette classe et l'utilise pour générer
        // la spécification OpenAPI.
}
