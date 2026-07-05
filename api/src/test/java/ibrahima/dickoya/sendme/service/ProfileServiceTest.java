package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.ChangePasswordRequest;
import ibrahima.dickoya.sendme.dto.request.DeleteAccountRequest;
import ibrahima.dickoya.sendme.dto.request.UpdateProfileRequest;
import ibrahima.dickoya.sendme.dto.response.LoginHistoryResponse;
import ibrahima.dickoya.sendme.dto.response.ProfileResponse;
import ibrahima.dickoya.sendme.entity.LoginHistory;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.Role;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.LoginHistoryRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour ProfileService")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;

    private User user;
    private final String USER_EMAIL = "ibrahima@sendme.com";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Ibrahima")
                .lastName("Dickoya")
                .email(USER_EMAIL)
                .phoneNumber("+224621000000")
                .passwordHash("encoded-password")
                .role(Role.ROLE_USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Tests de getProfile")
    class GetProfileTests {

        @Test
        @DisplayName("getProfile - Doit retourner le profil de l'utilisateur")
        void getProfile_ShouldReturnProfile() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            // Act
            ProfileResponse response = profileService.getProfile(USER_EMAIL);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getEmail()).isEqualTo(USER_EMAIL);
            assertThat(response.getFirstName()).isEqualTo("Ibrahima");
            assertThat(response.getLastName()).isEqualTo("Dickoya");
            assertThat(response.getPhoneNumber()).isEqualTo("+224621000000");
            assertThat(response.getRole()).isEqualTo(Role.ROLE_USER);
            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("getProfile - Doit lever ResourceNotFoundException si l'utilisateur n'existe pas")
        void getProfile_ShouldThrow_WhenUserNotFound() {
            // Arrange
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> profileService.getProfile("inconnu@email.com"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Utilisateur introuvable");
        }
    }

    @Nested
    @DisplayName("Tests de updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("updateProfile - Doit mettre à jour le profil")
        void updateProfile_ShouldUpdateProfile() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Nouveau")
                    .lastName("Nom")
                    .email("nouveau@email.com")
                    .phoneNumber("+33600000000")
                    .build();

            when(userRepository.existsByEmail("nouveau@email.com")).thenReturn(false);

            // Act
            ProfileResponse response = profileService.updateProfile(USER_EMAIL, request);

            // Assert
            assertThat(response.getFirstName()).isEqualTo("Nouveau");
            assertThat(response.getLastName()).isEqualTo("Nom");
            assertThat(response.getEmail()).isEqualTo("nouveau@email.com");
            assertThat(response.getPhoneNumber()).isEqualTo("+33600000000");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("updateProfile - Doit lever une exception si le nouvel email est déjà pris")
        void updateProfile_ShouldThrow_WhenEmailAlreadyExists() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Ibrahima")
                    .lastName("Dickoya")
                    .email("existant@email.com")
                    .phoneNumber("+224621000000")
                    .build();

            when(userRepository.existsByEmail("existant@email.com")).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> profileService.updateProfile(USER_EMAIL, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Un compte existe deja");
        }

        @Test
        @DisplayName("updateProfile - Ne doit pas vérifier l'unicité si l'email n'a pas changé")
        void updateProfile_ShouldNotCheckUniqueness_WhenEmailUnchanged() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Ibrahima")
                    .lastName("Dickoya")
                    .email(USER_EMAIL) // même email
                    .phoneNumber("+224621000000")
                    .build();

            // Act
            profileService.updateProfile(USER_EMAIL, request);

            // Assert
            verify(userRepository, never()).existsByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("Tests de changePassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("changePassword - Doit changer le mot de passe")
        void changePassword_ShouldChangePassword() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("ancien-mot-de-passe", "encoded-password")).thenReturn(true);
            when(passwordEncoder.encode("nouveau-mot-de-passe")).thenReturn("nouvel-encoded");

            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("ancien-mot-de-passe")
                    .newPassword("nouveau-mot-de-passe")
                    .confirmPassword("nouveau-mot-de-passe")
                    .build();

            // Act
            profileService.changePassword(USER_EMAIL, request);

            // Assert
            assertThat(user.getPasswordHash()).isEqualTo("nouvel-encoded");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("changePassword - Doit lever une exception si l'ancien mot de passe est incorrect")
        void changePassword_ShouldThrow_WhenCurrentPasswordIsWrong() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("mauvais-mot-de-passe", "encoded-password")).thenReturn(false);

            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("mauvais-mot-de-passe")
                    .newPassword("nouveau-mot-de-passe")
                    .confirmPassword("nouveau-mot-de-passe")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> profileService.changePassword(USER_EMAIL, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mot de passe actuel est incorrect");
        }

        @Test
        @DisplayName("changePassword - Doit lever une exception si les nouveaux mots de passe ne correspondent pas")
        void changePassword_ShouldThrow_WhenPasswordsDoNotMatch() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("ancien-mot-de-passe", "encoded-password")).thenReturn(true);

            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("ancien-mot-de-passe")
                    .newPassword("nouveau-mot-de-passe")
                    .confirmPassword("different")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> profileService.changePassword(USER_EMAIL, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ne correspondent pas");
        }

        @Test
        @DisplayName("changePassword - Doit lever une exception si le nouveau mot de passe est identique à l'ancien")
        void changePassword_ShouldThrow_WhenNewPasswordSameAsOld() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("ancien-mot-de-passe", "encoded-password")).thenReturn(true);

            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("ancien-mot-de-passe")
                    .newPassword("ancien-mot-de-passe")
                    .confirmPassword("ancien-mot-de-passe")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> profileService.changePassword(USER_EMAIL, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("doit être différent");
        }
    }

    @Nested
    @DisplayName("Tests de deleteAccount")
    class DeleteAccountTests {

        @Test
        @DisplayName("deleteAccount - Doit désactiver le compte")
        void deleteAccount_ShouldDeactivateAccount() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("mon-mot-de-passe", "encoded-password")).thenReturn(true);

            DeleteAccountRequest request = DeleteAccountRequest.builder()
                    .password("mon-mot-de-passe")
                    .build();

            // Act
            profileService.deleteAccount(USER_EMAIL, request);

            // Assert
            assertThat(user.getIsActive()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("deleteAccount - Doit lever une exception si le mot de passe est incorrect")
        void deleteAccount_ShouldThrow_WhenPasswordIsWrong() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("mauvais-mot-de-passe", "encoded-password")).thenReturn(false);

            DeleteAccountRequest request = DeleteAccountRequest.builder()
                    .password("mauvais-mot-de-passe")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> profileService.deleteAccount(USER_EMAIL, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Mot de passe incorrect");
        }
    }

    @Nested
    @DisplayName("Tests de getLoginHistory")
    class GetLoginHistoryTests {

        @Test
        @DisplayName("getLoginHistory - Doit retourner l'historique des connexions")
        void getLoginHistory_ShouldReturnLoginHistory() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

            LoginHistory history = LoginHistory.builder()
                    .id(1L)
                    .ipAddress("192.168.1.1")
                    .userAgent("Mozilla/5.0")
                    .success(true)
                    .loginTime(LocalDateTime.now())
                    .build();

            when(loginHistoryRepository.findTop10ByUserIdOrderByLoginTimeDesc(user.getId()))
                    .thenReturn(List.of(history));

            // Act
            List<LoginHistoryResponse> result = profileService.getLoginHistory(USER_EMAIL);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIpAddress()).isEqualTo("192.168.1.1");
            assertThat(result.get(0).getUserAgent()).isEqualTo("Mozilla/5.0");
            assertThat(result.get(0).isSuccess()).isTrue();
        }

        @Test
        @DisplayName("getLoginHistory - Doit retourner une liste vide si aucun historique")
        void getLoginHistory_ShouldReturnEmptyList_WhenNoHistory() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(loginHistoryRepository.findTop10ByUserIdOrderByLoginTimeDesc(user.getId()))
                    .thenReturn(List.of());

            // Act
            List<LoginHistoryResponse> result = profileService.getLoginHistory(USER_EMAIL);

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
