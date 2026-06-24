package ibrahima.dickoya.sendme.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.*;

/**
 * DTO de réponse pour un canal de réception d'un bénéficiaire.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryChannelResponse {

    private Long id;

    private ChannelType channel;

    private String phoneNumber;

    private String countryCode;

    private String bankName;

    /** IBAN masqué pour la sécurité (ex: FR76 **** **** 0189) */
    private String iban;

    private String bic;

    @JsonProperty("isPrimary")
    private boolean isPrimary;

    /** Devise déterminée par le pays (ex: GIN → GNF, SEN → XOF, FRA → EUR) */
    private String currency;
}
