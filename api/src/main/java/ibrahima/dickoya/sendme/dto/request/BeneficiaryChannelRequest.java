package ibrahima.dickoya.sendme.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import ibrahima.dickoya.sendme.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * DTO pour un canal de réception d'un bénéficiaire.
 * 
 * Un bénéficiaire peut avoir plusieurs canaux.
 * - BANK_TRANSFER : bankName, iban, bic obligatoires
 * - Mobile Money : phoneNumber, countryCode obligatoires
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryChannelRequest {

    @NotNull(message = "Le canal est obligatoire")
    private ChannelType channel;

    /**
     * Numéro de téléphone au format international.
     * Pattern : doit commencer par + suivi de 7 à 15 chiffres.
     * Les espaces de groupement sont acceptés (ex : +225 07 00 00 00 00)
     * et normalisés (espaces retirés) avant persistance.
     * Exemples valides : +33612345678, +2250712345678, +14155551234
     */
    @Pattern(regexp = "^\\+[1-9][0-9 ]{6,16}$", message = "Le numero de telephone doit etre au format international (+XXX...)")
    private String phoneNumber;

    private String countryCode;

    private String bankName;

    /**
     * IBAN (International Bank Account Number).
     * Pattern : 2 lettres (pays) + 2 chiffres (clé) + jusqu'à 30 caractères
     * alphanumériques.
     * Exemples : FR7630006000011234567890189, DE89370400440532013000
     */
    @Pattern(regexp = "^[A-Za-z]{2}\\d{2}[A-Za-z0-9 ]{1,32}$", message = "L'IBAN doit respecter le format international (ex: FR76...)")
    private String iban;

    /**
     * BIC/SWIFT (Bank Identifier Code).
     * Pattern : 8 ou 11 caractères alphanumériques.
     * Format : 4 lettres (banque) + 2 lettres (pays) + 2 caractères (localisation)
     * + 3 caractères optionnels (branche)
     * Exemples : BNPAFRPP, BNPAFRPPXXX
     */
    @Pattern(regexp = "^[A-Za-z]{4}[A-Za-z]{2}[A-Za-z0-9 ]{2}([A-Za-z0-9 ]{3})?$", message = "Le BIC doit contenir 8 ou 11 caracteres (ex: BNPAFRPP)")
    private String bic;

    @JsonProperty("isPrimary")
    @Builder.Default
    private boolean isPrimary = false;
}
