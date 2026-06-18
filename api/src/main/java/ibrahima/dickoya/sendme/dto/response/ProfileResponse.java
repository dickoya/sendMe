package ibrahima.dickoya.sendme.dto.response;

import ibrahima.dickoya.sendme.enums.Role;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour le profil utilisateur.
 * Retourné par GET /api/profile et PUT /api/profile.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
