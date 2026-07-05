package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.exception.RateLimitExceededException;
import ibrahima.dickoya.sendme.security.RateLimitingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour IpBlockingService")
class IpBlockingServiceTest {

    @Mock
    private RateLimitingFilter rateLimitingFilter;

    @InjectMocks
    private IpBlockingService ipBlockingService;

    private final String IP_ADDRESS = "192.168.1.100";

    @Nested
    @DisplayName("Tests de applyBackoff")
    class ApplyBackoffTests {

        @Test
        @DisplayName("applyBackoff - Doit passer sans exception quand le seuil de 3 n'est pas atteint")
        void applyBackoff_ShouldPass_WhenUnderThreshold() {
            // Arrange
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);

            // Act & Assert (ne doit pas lever d'exception)
            ipBlockingService.applyBackoff(IP_ADDRESS);
        }

        @Test
        @DisplayName("applyBackoff - Doit lever RateLimitExceededException quand le backoff exponentiel est actif")
        void applyBackoff_ShouldThrow_WhenExponentialBackoffActive() {
            // Arrange - 3 échecs pour déclencher le backoff
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);

            // Act & Assert
            assertThatThrownBy(() -> ipBlockingService.applyBackoff(IP_ADDRESS))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("Trop de tentatives");
        }

        @Test
        @DisplayName("applyBackoff - Doit passer après que le délai exponentiel est écoulé")
        void applyBackoff_ShouldPass_AfterDelayExpired() throws InterruptedException {
            // Arrange - 3 échecs pour déclencher le backoff (délai = 2^(3-3) = 1 seconde)
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);

            // Vérifier que le backoff est actif
            assertThatThrownBy(() -> ipBlockingService.applyBackoff(IP_ADDRESS))
                    .isInstanceOf(RateLimitExceededException.class);

            // Attendre que le délai d'1 seconde soit écoulé
            Thread.sleep(1100);

            // Act & Assert - doit maintenant passer
            ipBlockingService.applyBackoff(IP_ADDRESS);
        }

        @Test
        @DisplayName("applyBackoff - Doit passer sans exception quand aucune tentative n'a eu lieu")
        void applyBackoff_ShouldPass_WhenNoAttempts() {
            // Act & Assert (ne doit pas lever d'exception)
            ipBlockingService.applyBackoff(IP_ADDRESS);
        }

        @Test
        @DisplayName("applyBackoff - Doit augmenter le délai exponentiellement avec plus d'échecs")
        void applyBackoff_ShouldIncreaseDelayExponentially() {
            // Arrange - 5 échecs (délai = 2^(5-3) = 4 secondes)
            for (int i = 0; i < 5; i++) {
                ipBlockingService.recordFailure(IP_ADDRESS);
            }

            // Act & Assert
            assertThatThrownBy(() -> ipBlockingService.applyBackoff(IP_ADDRESS))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("Trop de tentatives");
        }

        @Test
        @DisplayName("applyBackoff - Doit plafonner le délai à 8 secondes maximum")
        void applyBackoff_ShouldCapDelayAt8Seconds() {
            // Arrange - 10 échecs (délai = min(2^(10-3), 8) = min(128, 8) = 8 secondes)
            for (int i = 0; i < 10; i++) {
                ipBlockingService.recordFailure(IP_ADDRESS);
            }

            // Act & Assert
            assertThatThrownBy(() -> ipBlockingService.applyBackoff(IP_ADDRESS))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("Trop de tentatives");
        }
    }

    @Nested
    @DisplayName("Tests de recordFailure")
    class RecordFailureTests {

        @Test
        @DisplayName("recordFailure - Doit incrémenter le compteur d'échecs IP")
        void recordFailure_ShouldIncrementCounter() {
            // Act
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);

            // Assert - 3 échecs doivent déclencher le backoff
            ipBlockingService.recordFailure(IP_ADDRESS);
            assertThatThrownBy(() -> ipBlockingService.applyBackoff(IP_ADDRESS))
                    .isInstanceOf(RateLimitExceededException.class);
        }

        @Test
        @DisplayName("recordFailure - Doit déléguer à RateLimitingFilter pour le blocage persistant")
        void recordFailure_ShouldDelegateToRateLimitingFilter() {
            // Act
            ipBlockingService.recordFailure(IP_ADDRESS);

            // Assert
            verify(rateLimitingFilter).recordFailedAttempt(IP_ADDRESS);
        }

        @Test
        @DisplayName("recordFailure - Doit tracker séparément différentes adresses IP")
        void recordFailure_ShouldTrackDifferentIpsSeparately() {
            // Arrange
            String ip2 = "10.0.0.1";

            // Act
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(ip2);

            // Assert - IP1 a 2 échecs (pas de backoff), IP2 a 1 échec (pas de backoff)
            ipBlockingService.applyBackoff(IP_ADDRESS); // ne doit pas lever
            ipBlockingService.applyBackoff(ip2); // ne doit pas lever

            // 3ème échec pour IP1 déclenche le backoff
            ipBlockingService.recordFailure(IP_ADDRESS);
            assertThatThrownBy(() -> ipBlockingService.applyBackoff(IP_ADDRESS))
                    .isInstanceOf(RateLimitExceededException.class);
        }
    }

    @Nested
    @DisplayName("Tests de resetForIp")
    class ResetForIpTests {

        @Test
        @DisplayName("resetForIp - Doit réinitialiser les compteurs après connexion réussie")
        void resetForIp_ShouldClearCounters() {
            // Arrange
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);
            ipBlockingService.recordFailure(IP_ADDRESS);

            // Vérifier que le backoff est actif
            assertThatThrownBy(() -> ipBlockingService.applyBackoff(IP_ADDRESS))
                    .isInstanceOf(RateLimitExceededException.class);

            // Act
            ipBlockingService.resetForIp(IP_ADDRESS);

            // Assert - le backoff doit être réinitialisé
            ipBlockingService.applyBackoff(IP_ADDRESS); // ne doit pas lever
        }

        @Test
        @DisplayName("resetForIp - Ne doit pas planter si l'IP n'existe pas dans le cache")
        void resetForIp_ShouldNotThrow_WhenIpNotInCache() {
            // Act (ne doit pas lever d'exception)
            ipBlockingService.resetForIp("10.0.0.99");
        }
    }
}
