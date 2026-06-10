package ibrahima.dickoya.sendme.dto.response;

import lombok.*;
import java.util.Map;

/**
 * DTO de réponse pour le tableau de bord administrateur.
 *
 * Contient toutes les statistiques globales de la plateforme :
 * - Statistiques des utilisateurs
 * - Statistiques des transactions
 * - Statistiques financières
 * - Répartition par canal
 * - Connexions SSE actives
 *
 * Ces données sont affichées sur la page d'accueil
 * du tableau de bord administrateur.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardResponse {

    // ── Statistiques utilisateurs ─────────────────────────────────────────

    /** Nombre total d'utilisateurs inscrits sur la plateforme. */
    private Long totalUsers;

    /** Nombre d'utilisateurs avec un compte actif. */
    private Long activeUsers;

    /** Nombre d'utilisateurs avec un compte désactivé. */
    private Long inactiveUsers;

    // ── Statistiques transactions ─────────────────────────────────────────

    /** Nombre total de transactions toutes périodes confondues. */
    private Long totalTransactions;

    /** Nombre de transactions complétées avec succès. */
    private Long completedTransactions;

    /** Nombre de transactions échouées. */
    private Long failedTransactions;

    /** Nombre de transactions remboursées. */
    private Long refundedTransactions;

    /** Nombre de transactions en attente ou en cours. */
    private Long pendingTransactions;

    /**
     * Taux de succès des transactions en pourcentage.
     * Calculé : (completedTransactions / totalTransactions) × 100
     */
    private Double successRate;

    // ── Statistiques financières ──────────────────────────────────────────

    /**
     * Volume total transféré sur la plateforme (en EUR).
     * Somme de tous les amountSent des transactions COMPLETED.
     */
    private Double totalVolume;

    /**
     * Total des frais collectés par la plateforme (en EUR).
     * Somme de tous les fees des transactions COMPLETED.
     */
    private Double totalFees;

    /**
     * Volume transféré aujourd'hui (en EUR).
     * Utile pour le monitoring quotidien.
     */
    private Double todayVolume;

    /**
     * Nombre de transactions aujourd'hui.
     * Utile pour le monitoring quotidien.
     */
    private Long todayTransactions;

    // ── Répartition par canal ─────────────────────────────────────────────

    /**
     * Nombre de transactions par canal de réception.
     * Clé : nom du canal (ex: "ORANGE_MONEY")
     * Valeur : nombre de transactions
     * Exemple : {"ORANGE_MONEY": 45, "BANK_TRANSFER": 12, "WAVE": 8}
     */
    private Map<String, Long> transactionsByChannel;

    // ── Monitoring temps réel ─────────────────────────────────────────────

    /**
     * Nombre de connexions SSE actives en ce moment.
     * Indique combien d'utilisateurs sont connectés en temps réel.
     */
    private Integer activeSSEConnections;
}