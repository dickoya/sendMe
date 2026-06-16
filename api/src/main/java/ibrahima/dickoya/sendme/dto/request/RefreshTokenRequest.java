package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Requête pour rafraîchir un token JWT.
 * Contient le refresh token à valider pour générer un nouveau jeu de tokens.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Le refresh token est requis")
    private String refreshToken;
}
