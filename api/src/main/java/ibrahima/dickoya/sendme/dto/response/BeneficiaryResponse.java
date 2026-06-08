package ibrahima.dickoya.sendme.dto.response;

import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO de réponse retourné au frontend après création,
 * modification ou consultation d'un bénéficiaire.
 * Ne contient jamais le mot de passe ni les données sensibles de l'utilisateur.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BeneficiaryResponse {

    /** Identifiant unique du bénéficiaire en base de données. */
    private Long id;

    /** Prénom du bénéficiaire. */
    private String firstName;

    /** Nom du bénéficiaire. */
    private String lastName;

    /**
     * Canal de réception associé à ce bénéficiaire.
     * Détermine quel type de détails (bankDetails ou mobileDetails) est renseigné.
     */
    private ChannelType channel;

    /**
     * Détails bancaires — non null uniquement si channel = BANK_TRANSFER.
     */
    private BankDetailsResponse bankDetails;

    /**
     * Détails Mobile Money — non null uniquement si channel != BANK_TRANSFER.
     */
    private MobileMoneyDetailsResponse mobileDetails;

    /** Date de création du bénéficiaire. */
    private LocalDateTime createdAt;
}