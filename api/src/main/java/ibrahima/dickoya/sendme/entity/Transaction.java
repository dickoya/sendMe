package ibrahima.dickoya.sendme.entity;

import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(name = "amount_sent", nullable = false)
    private BigDecimal amountSent;

    @Column(name = "currency_sent", nullable = false)
    private String currencySent;

    @Column(name = "amount_received")
    private BigDecimal amountReceived;

    @Column(name = "currency_received")
    private String currencyReceived;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    private BigDecimal fees;

    /**
     * Montant de la réduction appliquée (coupon de parrainage).
     * Si aucune réduction n'a été appliquée, vaut 0.
     */
    @Column(name = "discount")
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channel;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Identifiant du PaymentIntent Stripe.
     * Généré lors de la création du paiement côté Stripe.
     * Permet de retrouver et rembourser le paiement si nécessaire.
     * Format : pi_XXXXXXXXXXXXXXXXXXXXXXXX
     */
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    /**
     * Statut du paiement retourné par Stripe.
     * Valeurs possibles : requires_confirmation, processing, succeeded, canceled
     */
    @Column(name = "stripe_payment_status")
    private String stripePaymentStatus;
}
