package ibrahima.dickoya.sendme.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité JPA représentant un taux de change récupéré depuis
 * l'API OpenExchangeRates.
 *
 * Chaque ligne correspond à un taux pour un couple de devises
 * à un instant donné. Utilisé pour :
 * - Audit et conformité (traçabilité des taux appliqués)
 * - Historique dans le dashboard admin
 * - Fallback en cas d'indisponibilité de l'API
 */
@Entity
@Table(name = "exchange_rates", indexes = {
        @Index(name = "idx_exchange_rates_base_target", columnList = "baseCurrency, targetCurrency"),
        @Index(name = "idx_exchange_rates_fetched_at", columnList = "fetchedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Devise de base (ex: EUR, USD). */
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    /** Devise cible (ex: XOF, GNF). */
    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    /** Taux brut récupéré depuis l'API OpenExchangeRates. */
    @Column(name = "raw_rate", nullable = false, precision = 18, scale = 10)
    private BigDecimal rawRate;

    /** Taux avec marge interne appliquée (rawRate × margin). */
    @Column(name = "margin_rate", nullable = false, precision = 18, scale = 10)
    private BigDecimal marginRate;

    /** Pourcentage de marge appliqué (ex: 2.00 pour 2%). */
    @Column(name = "margin_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal marginPct;

    /** Date de récupération du taux depuis l'API. */
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    /** Statut de l'appel API (SUCCESS / FALLBACK). */
    @Column(name = "api_status", nullable = false, length = 20)
    private String apiStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
