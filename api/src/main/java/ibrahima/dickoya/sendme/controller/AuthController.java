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
@Tag(
        name = "Authentification",
        description = "Endpoints publics pour l'inscription et la connexion. " +
                "Aucun token JWT requis pour ces routes."
)
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    /**
     * Inscription d'un nouvel utilisateur.
     */
    @PostMapping("/register")
    @Operation(
            summary = "Inscription d'un nouvel utilisateur",
            description = """
            Crée un nouveau compte utilisateur sur la plateforme SendMe.
            
            **Processus :**
            1. Validation des données (format email, longueur mot de passe)
            2. Vérification de l'unicité de l'email
            3. Hachage du mot de passe avec BCrypt
            4. Création du compte
            5. Retour des tokens JWT (access + refresh)
            
            **Règles de validation :**
            - Email : format valide et unique
            - Mot de passe : minimum 8 caractères
            """,
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Compte créé avec succès — tokens JWT retournés",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email déjà utilisé par un autre compte"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides (format email, mot de passe trop court)"
            )
    })
    public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<AuthResponse>>
    register(@Valid @RequestBody RegisterRequest request) {
        var response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ibrahima.dickoya.sendme.dto.response.ApiResponse
                        .success("Compte cree avec succes", response));
    }

    /**
     * Connexion d'un utilisateur existant.
     */
    @PostMapping("/login")
    @Operation(
            summary = "Connexion utilisateur",
            description = """
            Authentifie un utilisateur et retourne les tokens JWT.
            
            **Processus :**
            1. Vérification des identifiants (email + mot de passe)
            2. Vérification que le compte est actif
            3. Génération d'un access token (24h) et d'un refresh token (7j)
            
            **Cartes de test Stripe (sandbox) :**
            -  Succès : `4242 4242 4242 4242`
            -  Refusée : `4000 0000 0000 0002`
            """,
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Connexion réussie — tokens JWT retournés",
                    content = @Content(
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Exemple de réponse",
                                    value = """
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
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Email ou mot de passe incorrect"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Compte désactivé par l'administrateur"
            )
    })
    public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<AuthResponse>>
    login(@Valid @RequestBody LoginRequest request) {
        var response = authService.login(request);
        return ResponseEntity.ok(ibrahima.dickoya.sendme.dto.response.ApiResponse
                .success("Connexion reussie", response));
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
    public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<Void>>
    testEmail(@RequestParam String to) {
        emailService.sendTestEmail(to);
        return ResponseEntity.ok(
                ibrahima.dickoya.sendme.dto.response.ApiResponse
                        .success("Email de test envoyé vers : " + to, null));
    }

}