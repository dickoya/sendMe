package ibrahima.dickoya.sendme.dto.response;

import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de réponse complet pour une transaction avec informations Stripe.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionResponse {

    /** Identifiant interne de la transaction. */
    private Long id;

    /**
     * Référence unique de la transaction.
     * Format : TXN-YYYYMMDD-XXXX
     */
    private String reference;

    /** Montant envoyé. */
    private BigDecimal amountSent;

    /** Devise d'envoi. */
    private String currencySent;

    /** Montant reçu par le bénéficiaire. */
    private BigDecimal amountReceived;

    /** Devise de réception. */
    private String currencyReceived;

    /** Taux de change appliqué. */
    private BigDecimal exchangeRate;

    /** Frais prélevés. */
    private BigDecimal fees;

    /**
     * Statut de la transaction.
     * PENDING → PROCESSING → COMPLETED / FAILED / REFUNDED
     */
    private TransactionStatus status;

    /** Canal de réception. */
    private ChannelType channel;

    /**
     * Identifiant du PaymentIntent Stripe.
     * Format : pi_XXXXXXXXXXXXXXXXXXXXXXXX
     * Permet de retrouver le paiement dans le dashboard Stripe.
     */
    private String stripePaymentIntentId;

    /**
     * Statut du paiement Stripe.
     * Valeurs : requires_payment_method, requires_confirmation,
     *           requires_action, processing, succeeded, canceled
     */
    private String stripePaymentStatus;

    /** Informations sur le bénéficiaire. */
    private BeneficiaryResponse beneficiary;

    /** Date de création. */
    private LocalDateTime createdAt;

    /** Date de dernière mise à jour. */
    private LocalDateTime updatedAt;
}