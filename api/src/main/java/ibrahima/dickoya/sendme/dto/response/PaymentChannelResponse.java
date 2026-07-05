package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

/**
 * DTO de réponse pour un canal de paiement.
 * Utilisé dans l'interface d'administration et pour
 * la sélection des canaux par les utilisateurs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentChannelResponse {

    private Long id;
    private String channelType;
    private String displayName;
    private String icon;
    private boolean isActive;
}
