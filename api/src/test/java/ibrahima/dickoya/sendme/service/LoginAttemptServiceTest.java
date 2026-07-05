package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour LoginAttemptService")
class LoginAttemptServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private final String EMAIL = "ibrahima@sendme.com";
    private final String IP_ADDRESS = "192.168.1.100";
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Ibrahim")
                .lastName("Dickoya")
                .email(EMAIL)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Tests de recordFailedAttempt")
    class RecordFailedAttemptTests {

        @Test
        @DisplayName("recordFailedAttempt - Doit incrémenter le compteur à chaque tentative")
        void recordFailedAttempt_ShouldIncrementCounter() {
            // Act
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);

            // Assert
            int attempts = loginAttemptService.getCurrentAttempts(EMAIL);
            assertThat(attempts).isEqualTo(2);

            verify(emailService, never()).sendSuspiciousLoginEmail(any(), anyString(), anyInt());
        }

        @Test
        @DisplayName("recordFailedAttempt - Doit envoyer une alerte email quand le seuil de 5 est atteint")
        void recordFailedAttempt_ShouldSendAlert_WhenThresholdReached() {
            // Arrange
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            // Act - 5 tentatives pour atteindre le seuil
            for (int i = 1; i <= 5; i++) {
                loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            }

            // Assert
            verify(emailService).sendSuspiciousLoginEmail(user, IP_ADDRESS, 5);
            // Le compteur doit être remis à 0 après l'alerte
            assertThat(loginAttemptService.getCurrentAttempts(EMAIL)).isZero();
        }

        @Test
        @DisplayName("recordFailedAttempt - Doit réinitialiser le compteur après avoir envoyé l'alerte")
        void recordFailedAttempt_ShouldResetCounter_AfterAlert() {
            // Arrange
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            // Act - 5 tentatives pour déclencher l'alerte
            for (int i = 1; i <= 5; i++) {
                loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            }

            // Assert - compteur à 0 après alerte
            assertThat(loginAttemptService.getCurrentAttempts(EMAIL)).isZero();

            // Une nouvelle tentative doit repartir de 1
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            assertThat(loginAttemptService.getCurrentAttempts(EMAIL)).isEqualTo(1);
        }

        @Test
        @DisplayName("recordFailedAttempt - Doit gérer l'échec de l'envoi d'email sans planter")
        void recordFailedAttempt_ShouldHandleEmailServiceFailure() {
            // Arrange
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            doThrow(new RuntimeException("SMTP indisponible"))
                    .when(emailService).sendSuspiciousLoginEmail(any(), anyString(), anyInt());

            // Act - 5 tentatives pour déclencher l'alerte (qui va échouer)
            for (int i = 1; i <= 5; i++) {
                loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            }

            // Assert - le service ne doit pas planter malgré l'erreur email
            assertThat(loginAttemptService.getCurrentAttempts(EMAIL)).isZero();
            verify(emailService).sendSuspiciousLoginEmail(user, IP_ADDRESS, 5);
        }

        @Test
        @DisplayName("recordFailedAttempt - Doit gérer l'absence d'utilisateur en base")
        void recordFailedAttempt_ShouldHandleUserNotFound() {
            // Arrange
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            // Act - 5 tentatives pour déclencher l'alerte
            for (int i = 1; i <= 5; i++) {
                loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            }

            // Assert - pas d'appel à emailService si l'utilisateur n'existe pas
            verify(emailService, never()).sendSuspiciousLoginEmail(any(), anyString(), anyInt());
            assertThat(loginAttemptService.getCurrentAttempts(EMAIL)).isZero();
        }

        @Test
        @DisplayName("recordFailedAttempt - Doit tracker séparément différents emails")
        void recordFailedAttempt_ShouldTrackDifferentEmailsSeparately() {
            // Arrange
            String email2 = "autre@email.com";

            // Act
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            loginAttemptService.recordFailedAttempt(email2, IP_ADDRESS);

            // Assert
            assertThat(loginAttemptService.getCurrentAttempts(EMAIL)).isEqualTo(2);
            assertThat(loginAttemptService.getCurrentAttempts(email2)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Tests de resetAttempts")
    class ResetAttemptsTests {

        @Test
        @DisplayName("resetAttempts - Doit réinitialiser le compteur après connexion réussie")
        void resetAttempts_ShouldRemoveEmail() {
            // Arrange
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            assertThat(loginAttemptService.getCurrentAttempts(EMAIL)).isEqualTo(2);

            // Act
            loginAttemptService.resetAttempts(EMAIL);

            // Assert
            assertThat(loginAttemptService.getCurrentAttempts(EMAIL)).isZero();
        }

        @Test
        @DisplayName("resetAttempts - Ne doit pas planter si l'email n'existe pas dans le cache")
        void resetAttempts_ShouldNotThrow_WhenEmailNotInCache() {
            // Act (ne doit pas lever d'exception)
            loginAttemptService.resetAttempts("inconnu@email.com");

            // Assert
            assertThat(loginAttemptService.getCurrentAttempts("inconnu@email.com")).isZero();
        }
    }

    @Nested
    @DisplayName("Tests de getCurrentAttempts")
    class GetCurrentAttemptsTests {

        @Test
        @DisplayName("getCurrentAttempts - Doit retourner 0 quand aucune tentative n'a eu lieu")
        void getCurrentAttempts_ShouldReturnZero_WhenNoAttempts() {
            // Act
            int attempts = loginAttemptService.getCurrentAttempts(EMAIL);

            // Assert
            assertThat(attempts).isZero();
        }

        @Test
        @DisplayName("getCurrentAttempts - Doit retourner le nombre exact de tentatives")
        void getCurrentAttempts_ShouldReturnExactCount() {
            // Arrange
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);
            loginAttemptService.recordFailedAttempt(EMAIL, IP_ADDRESS);

            // Act
            int attempts = loginAttemptService.getCurrentAttempts(EMAIL);

            // Assert
            assertThat(attempts).isEqualTo(3);
        }
    }
}
