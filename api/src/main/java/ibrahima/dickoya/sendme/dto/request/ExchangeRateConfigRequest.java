package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO pour la modification de la configuration des taux de change
 * par l'administrateur.
 *
 * Permet à l'admin de changer la durée du cache et/ou la marge interne
 * sans modifier le code source ni redémarrer l'application.
 *
 * Les deux champs sont optionnels : on peut modifier un seul des deux
 * ou les deux à la fois.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateConfigRequest {

    /**
     * Nouvelle durée de vie du cache en minutes.
     * Doit être au moins 1 minute.
     * Optionnel : si non fourni, la durée actuelle est conservée.
     */
    @Min(value = 1, message = "La durée du cache doit être d'au moins 1 minute")
    private Integer cacheDurationMinutes;

    /**
     * Nouvelle marge interne en pourcentage.
     * Doit être entre 0% et 50%.
     * Exemple : 2.0 pour 2% de marge.
     * Optionnel : si non fourni, la marge actuelle est conservée.
     */
    @DecimalMin(value = "0.0", message = "La marge ne peut pas être négative")
    @DecimalMax(value = "50.0", message = "La marge ne peut pas dépasser 50%")
    private BigDecimal marginPercent;

    /**
     * Raison du changement.
     * Conservée dans les logs pour audit.
     */
    @NotBlank(message = "La raison du changement est obligatoire")
    private String reason;
}
