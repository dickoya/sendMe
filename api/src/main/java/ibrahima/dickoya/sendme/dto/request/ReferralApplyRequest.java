package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralApplyRequest {

    @NotBlank(message = "Le code de parrainage est obligatoire")
    private String referralCode;
}
