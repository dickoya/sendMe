package ibrahima.dickoya.sendme.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import ibrahima.dickoya.sendme.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private String phoneNumber;

    private String countryCode;

    private String bankName;

    private String iban;

    private String bic;

    @JsonProperty("isPrimary")
    @Builder.Default
    private boolean isPrimary = false;
}
