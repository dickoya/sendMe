package ibrahima.dickoya.sendme.dto.request;

import ibrahima.dickoya.sendme.enums.ChannelType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO pour simuler les frais et le taux de change avant un transfert.
 *
 * Permet à l'utilisateur de voir exactement :
 * - Les frais prélevés par la plateforme
 * - Le taux de change appliqué
 * - Le montant exact que recevra le bénéficiaire
 * Tout ceci AVANT de confirmer et débiter la carte.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SimulateRequest {

    /**
     * Montant que l'utilisateur souhaite envoyer.
     */
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1.00", message = "Le montant minimum est de 1.00")
    private BigDecimal amount;

    /**
     * Devise d'envoi.
     * Exemple : EUR, USD
     */
    @NotBlank(message = "La devise d'envoi est obligatoire")
    private String currencyFrom;

    /**
     * Devise de réception du bénéficiaire.
     * Exemple : XOF (Franc CFA), GNF (Franc Guinéen)
     */
    @NotBlank(message = "La devise de reception est obligatoire")
    private String currencyTo;

    /**
     * Canal de réception choisi.
     * Influence potentiellement les frais appliqués.
     */
    @NotNull(message = "Le canal est obligatoire")
    private ChannelType channel;
}