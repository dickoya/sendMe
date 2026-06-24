package ibrahima.dickoya.sendme.dto.response;

import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse retourné au frontend après création,
 * modification ou consultation d'un bénéficiaire.
 * Ne contient jamais le mot de passe ni les données sensibles de l'utilisateur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponse {

    /** Identifiant unique du bénéficiaire en base de données. */
    private Long id;

    /** Prénom du bénéficiaire. */
    private String firstName;

    /** Nom du bénéficiaire. */
    private String lastName;

    /**
     * Canal par défaut du bénéficiaire.
     */
    private ChannelType channel;

    /**
     * Liste des canaux de réception de ce bénéficiaire.
     * Un bénéficiaire peut avoir 1 ou plusieurs canaux.
     */
    private List<BeneficiaryChannelResponse> channels;

    /** Date de création du bénéficiaire. */
    private LocalDateTime createdAt;
}
