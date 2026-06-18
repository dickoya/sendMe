package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

/**
 * DTO de réponse pour les statistiques personnelles de l'utilisateur
 * affichées sur son tableau de bord.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDashboardStatsResponse {

    /** Montant total envoyé (transactions COMPLETED). */
    private Double totalSent;

    /** Montant total reçu par les bénéficiaires. */
    private Double totalReceived;

    /** Total des frais payés par l'utilisateur. */
    private Double totalFees;

    /** Nombre total de transactions. */
    private Long totalTransactions;

    /** Nombre de transactions complétées avec succès. */
    private Long completedTransactions;

    /**
     * Économies estimées (simulation basée sur les frais évités vs concurrents).
     */
    private Double savings;

    /** Bénéficiaire le plus fréquent. */
    private FavoriteBeneficiary favoriteBeneficiary;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FavoriteBeneficiary {
        private Long id;
        private String firstName;
        private String lastName;
        private Long count;
    }
}
