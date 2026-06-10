package ibrahima.dickoya.sendme.enums;

/**
 * Types de notifications envoyées à l'utilisateur.
 *
 * Chaque type correspond à un événement métier précis.
 * Utilisé pour styliser les notifications côté frontend
 * (couleur, icône, son) et pour filtrer les notifications.
 */
public enum NotificationType {

    /** Transfert initié — statut PENDING. */
    TRANSFER_INITIATED,

    /** Transfert en cours de traitement — statut PROCESSING. */
    TRANSFER_PROCESSING,

    /** Transfert complété avec succès — statut COMPLETED. */
    TRANSFER_COMPLETED,

    /** Transfert échoué — statut FAILED. */
    TRANSFER_FAILED,

    /** Transfert remboursé suite à un échec — statut REFUNDED. */
    TRANSFER_REFUNDED,

    /** Paiement Stripe confirmé par webhook. */
    PAYMENT_CONFIRMED,

    /** Paiement Stripe échoué — webhook. */
    PAYMENT_FAILED,

    /** Notification générique d'information. */
    INFO
}