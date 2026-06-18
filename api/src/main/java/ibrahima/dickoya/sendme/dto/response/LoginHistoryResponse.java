package ibrahima.dickoya.sendme.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour l'historique de connexion.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistoryResponse {

    private Long id;
    private String ipAddress;
    private String userAgent;
    private boolean success;
    private LocalDateTime loginTime;
}
