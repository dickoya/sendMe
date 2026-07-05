package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

import java.time.LocalDate;

/**
 * Point de données pour le graphique d'évolution du volume par jour.
 * <p>
 * Utilisé par l'API {@code GET /api/admin/dashboard/volume?days=30}
 * pour alimenter le graphique en barres du dashboard admin.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VolumeDataPoint {

    /** Date du point de données. */
    private LocalDate date;

    /** Volume total transféré ce jour (en EUR). */
    private Double volume;

    /** Nombre de transactions complétées ce jour. */
    private Long transactionCount;
}
