package ibrahima.dickoya.sendme.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour un log d'audit centralisé.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;
    private String category;
    private String eventType;
    private String userEmail;
    private Long userId;
    private Long resourceId;
    private String resourceType;
    private String status;
    private String message;
    private String details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
