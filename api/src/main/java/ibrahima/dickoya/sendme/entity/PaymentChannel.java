package ibrahima.dickoya.sendme.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entité représentant un canal de paiement configurable par l'admin.
 *
 * Cette table permet à l'administrateur d'activer/désactiver
 * dynamiquement les canaux de réception sans modifier le code.
 * Elle sert également de catalogue pour l'ajout de nouveaux canaux.
 */
@Entity
@Table(name = "payment_channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom technique du canal (ex: "ORANGE_MONEY", "BANK_TRANSFER").
     * Doit correspondre à une valeur de l'enum ChannelType.
     */
    @Column(name = "channel_type", nullable = false, unique = true, length = 50)
    private String channelType;

    /** Nom d'affichage pour l'interface utilisateur (ex: "Orange Money"). */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** Emoji représentant le canal (ex: "🟠" pour Orange Money). */
    @Column(length = 10)
    private String icon;

    /** Si true, le canal est disponible pour les utilisateurs. */
    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", updatable = false)
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
}
