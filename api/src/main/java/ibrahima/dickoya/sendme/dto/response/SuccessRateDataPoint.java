package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

import java.time.LocalDate;

/**
 * Point de données pour le graphique d'évolution du taux de succès par jour.
 * <p>
 * Utilisé par l'API {@code GET /api/admin/dashboard/success-rate?days=30}
 * pour alimenter le graphique en ligne du dashboard admin.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessRateDataPoint {

    /** Date du point de données. */
    private LocalDate date;

    /** Taux de succès ce jour (en pourcentage). */
    private Double successRate;

    /** Nombre total de transactions ce jour. */
    private Long total;

    /** Nombre de transactions complétées avec succès ce jour. */
    private Long completed;

    /** Nombre de transactions échouées ce jour. */
    private Long failed;
}
