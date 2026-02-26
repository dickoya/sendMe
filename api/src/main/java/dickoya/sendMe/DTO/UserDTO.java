package dickoya.sendMe.DTO;

import dickoya.sendMe.models.Role;
import dickoya.sendMe.models.User;
import java.math.BigDecimal;
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
    private String phone;
    private BigDecimal balance;
    private String profileImage;

    public static UserDTO fromUser(User user) {
        return new UserDTO(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getPhone(),
            user.getBalance(),
            user.getProfileImage()
        );
    }
}
