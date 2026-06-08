package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO pour les détails d'un compte Mobile Money.
 * Utilisé pour tous les canaux sauf BANK_TRANSFER (RG-02).
 * Un seul champ suffit : le numéro de téléphone associé au compte Mobile Money.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MobileMoneyDetailsRequest {

    /**
     * Numéro de téléphone associé au compte Mobile Money du bénéficiaire.
     * Doit inclure l'indicatif pays.
     * Exemple : +225 07 12 34 56 78 (Côte d'Ivoire)
     */
    @NotBlank(message = "Le numero de telephone est obligatoire")
    @Pattern(
            regexp = "^\\+?[0-9]{8,15}$",
            message = "Format de numero de telephone invalide"
    )
    private String phoneNumber;
}