package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO de requête pour ajouter un nouveau canal de paiement.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentChannelRequest {

    @NotBlank(message = "Le type de canal est obligatoire")
    @Size(max = 50, message = "Le type de canal ne peut pas dépasser 50 caractères")
    private String channelType;

    @NotBlank(message = "Le nom d'affichage est obligatoire")
    @Size(max = 100, message = "Le nom d'affichage ne peut pas dépasser 100 caractères")
    private String displayName;

    @Size(max = 10, message = "L'icône ne peut pas dépasser 10 caractères")
    private String icon;
}
