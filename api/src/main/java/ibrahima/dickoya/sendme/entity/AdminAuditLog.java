package ibrahima.dickoya.sendme.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité représentant une action d'administration tracée dans les logs d'audit.
 * <p>
 * Chaque action sensible (suspension, réactivation, modification des frais,
 * envoi de maintenance, actions batch) est enregistrée automatiquement
 * via l'annotation {@code @Auditable} et l'AOP.
 * <p>
 * Table : {@code admin_audit_log}
 */
@Entity
@Table(name = "admin_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant de l'administrateur ayant effectué l'action. */
    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    /** Email de l'administrateur (dénormalisé pour les requêtes rapides). */
    @Column(name = "admin_email", nullable = false, length = 255)
    private String adminEmail;

    /**
     * Type d'action.
     * Exemples : USER_SUSPENDED, USER_REACTIVATED, FEE_UPDATED,
     * MAINTENANCE_SENT, BATCH_TOGGLE
     */
    @Column(nullable = false, length = 50)
    private String action;

    /**
     * Type d'entité ciblée.
     * Exemples : USER, CONFIG, NOTIFICATION
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /** Identifiant de l'entité ciblée (nullable pour les actions globales). */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Détails JSON de l'action.
     * Exemple : {"oldStatus": true, "newStatus": false, "reason": "Compte inactif"}
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** Adresse IP de l'administrateur au moment de l'action. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** Horodatage de l'action (géré automatiquement). */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
