package ibrahima.dickoya.sendme.entity;

import ibrahima.dickoya.sendme.enums.ChannelType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un moyen de réception d'un bénéficiaire.
 * 
 * Un bénéficiaire peut avoir plusieurs canaux (ex: Orange Money + Wave + Bank).
 * Chaque canal a ses propres détails (numéro de téléphone ou coordonnées
 * bancaires)
 * et un code pays qui détermine la devise de réception.
 */
@Entity
@Table(name = "beneficiary_channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType channel;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "country_code", length = 3)
    private String countryCode;

    @Column(name = "bank_name", length = 150)
    private String bankName;

    @Column(length = 34)
    private String iban;

    @Column(length = 11)
    private String bic;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

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
}
