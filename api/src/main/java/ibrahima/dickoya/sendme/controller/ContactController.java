package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.request.ContactRequest;
import ibrahima.dickoya.sendme.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST public du formulaire de contact.
 * Aucun token JWT requis. L'envoi est asynchrone via l'API Brevo.
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Formulaire de contact public (envoi via Brevo). Aucun token JWT requis.")
public class ContactController {

    private final EmailService emailService;

    /**
     * Envoie le message du formulaire de contact vers l'adresse de contact.
     *
     * Test : POST http://localhost:8080/api/contact
     * Body : { "name": "...", "email": "visiteur@exemple.com",
     *          "subject": "...", "message": "..." }
     */
    @PostMapping
    @Operation(summary = "Envoyer un message de contact", description = """
                    Envoie le message du formulaire de contact vers l'adresse
                    de contact configurée via l'API Brevo (envoi asynchrone).
                    """, security = @SecurityRequirement(name = ""))
    @ApiResponses({
                    @ApiResponse(responseCode = "202", description = "Message accepté pour envoi", content = @Content(schema = @Schema(implementation = ibrahima.dickoya.sendme.dto.response.ApiResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<ibrahima.dickoya.sendme.dto.response.ApiResponse<Void>> submit(
                    @Valid @RequestBody ContactRequest request) {
            emailService.sendContactEmail(
                            request.getName(),
                            request.getEmail(),
                            request.getSubject(),
                            request.getMessage());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                            .body(ibrahima.dickoya.sendme.dto.response.ApiResponse
                                            .success("Votre message a bien été pris en compte. Nous vous répondrons rapidement.", null));
    }

}
