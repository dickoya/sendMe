package ibrahima.dickoya.sendme.dto.request;

import ibrahima.dickoya.sendme.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO de requête pour changer le rôle d'un utilisateur (ROLE_USER / ROLE_ADMIN).
 * Utilisé par l'administrateur pour promouvoir ou rétrograder un compte.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleUpdateRequest {

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;
}
