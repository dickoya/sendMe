package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un log d'audit admin.
 * <p>
 * Contient les informations affichées dans la page AdminLogs :
 * action, admin, cible, détails, IP, date.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLogResponse {

    private Long id;

    /** Email de l'admin ayant effectué l'action. */
    private String adminEmail;

    /** Type d'action (USER_SUSPENDED, FEE_UPDATED, etc.). */
    private String action;

    /** Type d'entité ciblée (USER, CONFIG, NOTIFICATION). */
    private String entityType;

    /** Identifiant de l'entité ciblée (nullable). */
    private Long entityId;

    /** Détails JSON de l'action. */
    private String details;

    /** Adresse IP de l'admin. */
    private String ipAddress;

    /** Horodatage de l'action. */
    private LocalDateTime createdAt;
}
