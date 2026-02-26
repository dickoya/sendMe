package dickoya.sendMe.DTO;

import dickoya.sendMe.models.Role;
import dickoya.sendMe.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    public static UserDTO fromUser(User user) {
        return new UserDTO(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole()
        );
    }
}