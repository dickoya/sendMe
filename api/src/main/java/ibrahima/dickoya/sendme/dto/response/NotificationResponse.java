package ibrahima.dickoya.sendme.dto.response;

import ibrahima.dickoya.sendme.enums.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour une notification.
 *
 * Retourné au frontend via :
 * - L'endpoint GET /api/notifications (liste historique)
 * - Le flux SSE /api/notifications/stream (temps réel)
 *
 * Le frontend utilise le champ "type" pour choisir
 * la couleur et l'icône d'affichage.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {

    /** Identifiant unique de la notification. */
    private Long id;

    /**
     * Message affiché à l'utilisateur.
     * Exemple : " Transfert TXN-20250601-0042 complété — 128 745 XOF envoyés."
     */
    private String message;

    /**
     * Type de notification.
     * Le frontend utilise ce champ pour styliser la notification.
     * TRANSFER_COMPLETED → vert, TRANSFER_FAILED → rouge, etc.
     */
    private NotificationType type;

    /**
     * Statut de lecture.
     * false = non lu (badge rouge affiché), true = déjà lu
     */
    private Boolean isRead;

    /**
     * Identifiant de la transaction liée (si applicable).
     * Permet au frontend de faire un lien vers le détail de la transaction.
     */
    private Long transactionId;

    /**
     * Référence de la transaction liée (si applicable).
     * Exemple : TXN-20250601-0042
     */
    private String transactionReference;

    /** Date et heure de création de la notification. */
    private LocalDateTime createdAt;
}