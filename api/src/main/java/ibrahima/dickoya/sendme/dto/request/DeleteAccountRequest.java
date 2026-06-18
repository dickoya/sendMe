package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Requête de suppression de compte.
 * L'utilisateur doit confirmer son mot de passe pour supprimer son compte.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAccountRequest {

    @NotBlank(message = "Le mot de passe est requis pour supprimer le compte")
    private String password;
}
