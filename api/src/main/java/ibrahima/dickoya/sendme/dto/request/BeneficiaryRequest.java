package ibrahima.dickoya.sendme.dto.request;

import ibrahima.dickoya.sendme.enums.ChannelType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

/**
 * DTO (Data Transfer Object) pour la création et modification d'un
 * bénéficiaire.
 * 
 * Un bénéficiaire peut avoir 1 ou plusieurs canaux de réception.
 * Chaque canal a ses propres détails (bancaires ou Mobile Money)
 * et un code pays qui détermine la devise de réception.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryRequest {

    /**
     * Prénom du bénéficiaire — obligatoire.
     */
    @NotBlank(message = "Le prenom est obligatoire")
    private String firstName;

    /**
     * Nom du bénéficiaire — obligatoire.
     */
    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    /**
     * Canal par défaut (conservé pour compatibilité).
     */
    @NotNull(message = "Le canal de reception est obligatoire")
    private ChannelType channel;

    /**
     * Liste des canaux de réception de ce bénéficiaire.
     * Un bénéficiaire peut avoir 1 ou plusieurs canaux.
     * Chaque canal contient ses propres détails selon le type.
     */
    @NotEmpty(message = "Au moins un canal de reception est requis")
    @Valid
    private List<BeneficiaryChannelRequest> channels;
}
