package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCodeResponse {

    private String referralCode;
    private String shareLink;
    private long totalReferred;
    private long totalCouponsEarned;
    private long availableCoupons;
}
