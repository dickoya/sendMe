package ibrahima.dickoya.sendme.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralCouponResponse {

    private Long id;
    private String couponType;
    private BigDecimal discountAmount;
    private String currency;
    private boolean isUsed;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private String referredUserName;
    private Long transactionId;
    private LocalDateTime createdAt;
}
