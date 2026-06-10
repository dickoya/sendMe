package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO pour la modification des frais de la plateforme par l'administrateur.
 *
 * Permet à l'admin de changer le taux de commission
 * sans modifier le code source ni redémarrer l'application.
 *
 * Le nouveau taux est appliqué immédiatement à tous
 * les nouveaux transferts dès la mise à jour.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeeConfigRequest {

    /**
     * Nouveau taux de commission en pourcentage.
     * Doit être entre 0% et 10% maximum.
     * Exemple : 1.5 pour 1.5%
     */
    @NotNull(message = "Le taux de commission est obligatoire")
    @DecimalMin(value = "0.0", message = "Le taux ne peut pas etre negatif")
    @DecimalMax(value = "10.0", message = "Le taux ne peut pas depasser 10%")
    private BigDecimal feeRate;

    /**
     * Raison du changement de tarif.
     * Conservée dans les logs pour audit.
     * Exemple : "Promotion du mois de juin"
     */
    @NotBlank(message = "La raison du changement est obligatoire")
    private String reason;
}