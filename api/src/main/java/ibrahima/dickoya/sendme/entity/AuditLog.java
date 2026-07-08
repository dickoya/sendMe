package ibrahima.dickoya.sendme.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité unique centralisant TOUS les événements d'audit de la plateforme.
 * <p>
 * Remplace les anciennes tables {@code admin_audit_log} et
 * {@code system_audit_log} par une seule table {@code audit_logs}.
 * <p>
 * Catégories (category) :
 * <ul>
 * <li>AUTH → inscriptions, connexions, déconnexions</li>
 * <li>TRANSFER → création, succès, échec, remboursement de transferts</li>
 * <li>ADMIN → actions des administrateurs</li>
 * <li>CONFIG → modifications de configuration</li>
 * <li>SYSTEM → erreurs système, jobs, maintenance</li>
 * </ul>
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Catégorie principale (AUTH, TRANSFER, ADMIN, CONFIG, SYSTEM). */
    @Column(nullable = false, length = 50)
    private String category;

    /** Type d'événement spécifique. */
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    /** Email de l'utilisateur/admin concerné (nullable). */
    @Column(name = "user_email", length = 255)
    private String userEmail;

    /** Identifiant de l'utilisateur/admin concerné (nullable). */
    @Column(name = "user_id")
    private Long userId;

    /** Identifiant de la ressource concernée (transactionId, etc.). */
    @Column(name = "resource_id")
    private Long resourceId;

    /** Type de ressource (TRANSACTION, USER, CONFIG, etc.). */
    @Column(name = "resource_type", length = 50)
    private String resourceType;

    /** Statut (SUCCESS, FAILED, PENDING). */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "SUCCESS";

    /** Message descriptif. */
    @Column(columnDefinition = "TEXT")
    private String message;

    /** Détails JSON supplémentaires. */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** Adresse IP. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** User-Agent. */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /** Horodatage de l'événement. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
