package ibrahima.dickoya.sendme.entity;

import ibrahima.dickoya.sendme.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité représentant une notification envoyée à un utilisateur.
 *
 * Une notification est créée à chaque événement important :
 * - Initiation d'un transfert
 * - Confirmation du paiement Stripe (webhook)
 * - Succès ou échec du transfert vers l'opérateur
 *
 * Les notifications sont stockées en base ET poussées
 * en temps réel via SSE (Server-Sent Events).
 */
@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    /** Identifiant unique de la notification. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Message lisible par l'utilisateur.
     * Exemple : "Votre transfert TXN-20250601-0042 a été complété avec succès."
     */
    @Column(nullable = false)
    private String message;

    /**
     * Type de notification pour le frontend.
     * Permet d'afficher la bonne couleur et icône.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /**
     * Indique si l'utilisateur a lu cette notification.
     * false = nouveau (badge rouge), true = déjà vu
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * Utilisateur destinataire de cette notification.
     * Relation ManyToOne car un utilisateur peut avoir plusieurs notifications.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Transaction liée à cette notification (optionnel).
     * Null pour les notifications génériques (INFO).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    /** Date et heure de création de la notification. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Initialisation automatique de la date de création. */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}