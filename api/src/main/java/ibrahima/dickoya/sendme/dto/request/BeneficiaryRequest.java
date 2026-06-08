package ibrahima.dickoya.sendme.dto.request;

import ibrahima.dickoya.sendme.enums.ChannelType;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO (Data Transfer Object) pour la création et modification d'un bénéficiaire.
 * Contient toutes les informations envoyées par le frontend lors de la création
 * d'un bénéficiaire. Les champs bankDetails et mobileDetails sont optionnels
 * car ils dépendent du canal choisi (RG-02).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
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
     * Canal de réception choisi.
     * Valeurs possibles : BANK_TRANSFER, ORANGE_MONEY, MTN_MOBILE_MONEY,
     * WAVE, MOOV_MONEY, AIRTEL_MONEY
     */
    @NotNull(message = "Le canal de reception est obligatoire")
    private ChannelType channel;

    /**
     * Détails bancaires — requis uniquement si channel = BANK_TRANSFER.
     * Contient le nom de la banque, l'IBAN et le BIC.
     */
    private BankDetailsRequest bankDetails;

    /**
     * Détails Mobile Money — requis si channel != BANK_TRANSFER.
     * Contient uniquement le numéro de téléphone.
     */
    private MobileMoneyDetailsRequest mobileDetails;
}