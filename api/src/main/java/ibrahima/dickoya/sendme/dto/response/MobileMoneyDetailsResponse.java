package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

/**
 * DTO de réponse pour les détails Mobile Money d'un bénéficiaire.
 * Retourné pour tous les canaux sauf BANK_TRANSFER.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MobileMoneyDetailsResponse {

    /** Identifiant des détails Mobile Money. */
    private Long id;

    /** Numéro de téléphone Mobile Money du bénéficiaire. */
    private String phoneNumber;
}