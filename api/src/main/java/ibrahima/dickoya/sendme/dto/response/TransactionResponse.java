package ibrahima.dickoya.sendme.dto.response;

import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour une transaction.
 *
 * Retourné après création d'un transfert ou consultation de l'historique.
 * Contient toutes les informations nécessaires pour afficher le détail
 * d'une transaction dans l'interface utilisateur.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionResponse {

    /** Identifiant interne de la transaction. */
    private Long id;

    /**
     * Référence unique et immuable de la transaction.
     * Format : TXN-YYYYMMDD-XXXX
     * Exemple : TXN-20250601-0042
     */
    private String reference;

    /** Montant envoyé par l'expéditeur. */
    private BigDecimal amountSent;

    /** Devise d'envoi. */
    private String currencySent;

    /** Montant reçu par le bénéficiaire après conversion. */
    private BigDecimal amountReceived;

    /** Devise de réception. */
    private String currencyReceived;

    /** Taux de change appliqué. */
    private BigDecimal exchangeRate;

    /** Frais prélevés par la plateforme. */
    private BigDecimal fees;

    /**
     * Statut actuel de la transaction.
     * Valeurs : PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
     */
    private TransactionStatus status;

    /** Canal de réception utilisé. */
    private ChannelType channel;

    /** Informations sur le bénéficiaire. */
    private BeneficiaryResponse beneficiary;

    /** Date et heure de création de la transaction. */
    private LocalDateTime createdAt;

    /** Date et heure de la dernière mise à jour du statut. */
    private LocalDateTime updatedAt;
}