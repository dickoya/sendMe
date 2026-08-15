package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Requête du formulaire de contact public.
 *
 * Reçue via POST /api/contact, puis transmise à EmailService pour
 * envoi via Brevo vers l'adresse de contact configurée.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 120, message = "Le nom ne doit pas dépasser 120 caractères")
    private String name;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 120, message = "L'email ne doit pas dépasser 120 caractères")
    private String email;

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(max = 160, message = "Le sujet ne doit pas dépasser 160 caractères")
    private String subject;

    @NotBlank(message = "Le message est obligatoire")
    @Size(max = 4000, message = "Le message ne doit pas dépasser 4000 caractères")
    private String message;
}
