package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String referralCode;
    @Builder.Default
    private Boolean emailVerified = false;
}
