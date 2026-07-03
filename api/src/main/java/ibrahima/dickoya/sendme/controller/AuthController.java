package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.*;
import ibrahima.dickoya.sendme.service.AuthService;
import ibrahima.dickoya.sendme.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification des utilisateurs.
 * Gère l'inscription et la connexion avec génération de tokens JWT.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints publics pour l'inscription et la connexion. " +
                "Aucun token JWT requis pour ces routes.")
public class AuthController {

        private final AuthService authService;
        private final EmailService emailService;

        /**
         * Inscription d'un nouvel utilisateur.
         * 
         * Après inscription, un email de vérification est envoyé.
         * L'utilisateur doit cliquer sur le lien pour activer son compte.
         * Aucun token JWT n'est retourné — l'utilisateur doit d'abord vérifier son
         * email.
         */
        @PostMapping("/register")
        @Operation(summary = "Inscription d'un nouvel utilisateur", description = """
                        Crée un nouveau compte utilisateur sur la plateforme SendMe.

                        **Processus :**
                        1. Validation des données (format email, longueur mot de passe)
                        2. Vérification de l'unicité de l'email
                        3. Hachage du mot de passe avec BCrypt
                        4. Création du compte (email_non_vérifié)
                        5. Envoi d'un email de vérification
                        6. Retour des infos utilisateur (sans tokens JWT)

                        **Important :** L'utilisateur doit vérifier son email
                        avant de pouvoir se connecter.

                        **Règles de validation :**
                        - Email : format valide et unique
                        - Mot de passe : minimum 8 caractères
                        """, security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Compte créé avec succès — email de vérification envoyé", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Email déjà utilisé par un autre compte"),
                        @ApiResponse(responseCode = "400", description = "Données invalides (format email, mot de passe trop court)")
        })
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<AuthResponse>> register(
                        @Valid @RequestBody RegisterRequest request) {
                var response = authService.register(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Compte cree avec succes. Un email de verification vous a ete envoye.",
                                                                response));
        }

        /**
         * Connexion d'un utilisateur existant.
         */
        @PostMapping("/login")
        @Operation(summary = "Connexion utilisateur", description = """
                        Authentifie un utilisateur et retourne les tokens JWT.

                        **Processus :**
                        1. Vérification des identifiants (email + mot de passe)
                        2. Vérification que le compte est actif
                        3. Génération d'un access token (24h) et d'un refresh token (7j)

                        **Cartes de test Stripe (sandbox) :**
                        -  Succès : `4242 4242 4242 4242`
                        -  Refusée : `4000 0000 0000 0002`
                        """, security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Connexion réussie — tokens JWT retournés", content = @Content(schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name = "Exemple de réponse", value = """
                                        {
                                            "success": true,
                                            "message": "Connexion reussie",
                                            "data": {
                                                "accessToken": "eyJhbGci...",
                                                "refreshToken": "eyJhbGci...",
                                                "tokenType": "Bearer",
                                                "expiresIn": 86400000,
                                                "email": "ibrahima@sendme.com",
                                                "role": "ROLE_USER"
                                            }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect"),
                        @ApiResponse(responseCode = "403", description = "Compte désactivé par l'administrateur")
        })
        /**
         * Connexion utilisateur avec génération des tokens JWT.
         * La HttpServletRequest est injectée automatiquement par Spring
         * pour récupérer l'adresse IP de la connexion.
         */

        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<AuthResponse>> login(
                        @Valid @RequestBody LoginRequest request,
                        jakarta.servlet.http.HttpServletRequest httpRequest) {

                var response = authService.login(request, httpRequest);
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Connexion reussie", response));
        }

        /**
         * Rafraîchit les tokens JWT (access + refresh) à partir d'un refresh token
         * valide.
         * 
         * Utilisé par le frontend quand l'access token expire pour éviter
         * de déconnecter l'utilisateur.
         * 
         * Test : POST http://localhost:8080/api/auth/refresh
         * Body : { "refreshToken": "eyJhbGci..." }
         */
        @PostMapping("/refresh")
        @Operation(summary = "Rafraîchir les tokens JWT", description = """
                        Valide le refresh token et génère un nouveau jeu de tokens.
                        Utilisé par le frontend pour le refresh automatique sans déconnexion.

                        **Processus :**
                        1. Extraction de l'email depuis le refresh token
                        2. Validation du refresh token (non expiré, signature valide)
                        3. Vérification que le compte est toujours actif
                        4. Génération d'un nouvel access token (24h) et refresh token (7j)
                        """, security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Tokens rafraîchis avec succès", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Refresh token invalide ou expiré")
        })
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<AuthResponse>> refresh(
                        @Valid @RequestBody RefreshTokenRequest request) {
                var response = authService.refresh(request.getRefreshToken());
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Tokens rafraichis avec succes", response));
        }

        /**
         * Vérifie l'adresse email d'un utilisateur à partir d'un token.
         * 
         * L'utilisateur clique sur le lien reçu par email, ce qui appelle cet endpoint.
         * Si le token est valide et non expiré, l'email est marqué comme vérifié
         * et l'utilisateur reçoit ses tokens JWT pour être connecté automatiquement.
         * 
         * Test : GET http://localhost:8080/api/auth/verify-email?token=uuid
         */
        @GetMapping("/verify-email")
        @Operation(summary = "Vérifier l'adresse email", description = """
                        Valide l'adresse email d'un utilisateur via le token reçu par email.
                        Si le token est valide, l'utilisateur est automatiquement connecté
                        et reçoit ses tokens JWT.
                        """, security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Email vérifié avec succès — tokens JWT retournés"),
                        @ApiResponse(responseCode = "400", description = "Token invalide ou expiré")
        })
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<AuthResponse>> verifyEmail(
                        @RequestParam String token) {
                var response = authService.verifyEmail(token);
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Email verifie avec succes. Vous etes maintenant connecte.",
                                                                response));
        }

        /**
         * Renvoie l'email de vérification à un utilisateur.
         * 
         * Utilisé quand l'utilisateur n'a pas reçu l'email de vérification
         * ou si le lien a expiré.
         * 
         * Test : POST http://localhost:8080/api/auth/resend-verification
         * Body : { "email": "user@example.com" }
         */
        @PostMapping("/resend-verification")
        @Operation(summary = "Renvoyer l'email de vérification", description = """
                        Renvoie un nouvel email de vérification à l'utilisateur.
                        Un nouveau token est généré et le précédent est invalidé.
                        """, security = @SecurityRequirement(name = ""))
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Email de vérification renvoyé"),
                        @ApiResponse(responseCode = "400", description = "Email déjà vérifié ou compte introuvable")
        })
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<Void>> resendVerificationEmail(
                        @Valid @RequestBody ResendVerificationRequest request) {
                authService.resendVerificationEmail(request.getEmail());
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Un nouvel email de verification vous a ete envoye.", null));
        }

        /**
         * Endpoint temporaire pour tester la configuration email.
         * Envoie un email de test vers l'adresse spécifiée.
         *
         * À SUPPRIMER après validation de la configuration email.
         *
         * Test : POST http://localhost:8080/api/auth/test-email?to=ton@email.com
         */
        @PostMapping("/test-email")
        public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<Void>> testEmail(
                        @RequestParam String to) {
                emailService.sendTestEmail(to);
                return ResponseEntity.ok(
                                ibrahima.dickoya.sendme.dto.response.ApiResponse
                                                .success("Email de test envoyé vers : " + to, null));
        }

}
