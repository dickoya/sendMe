package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour RefreshTokenService")
class RefreshTokenServiceTest {

    private RefreshTokenService refreshTokenService;

    private final String EMAIL = "ibrahima@sendme.com";

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService();
    }

    @Nested
    @DisplayName("Tests de checkRateLimit")
    class CheckRateLimitTests {

        @Test
        @DisplayName("checkRateLimit - Doit passer quand le nombre de requêtes est sous la limite de 5")
        void checkRateLimit_ShouldPass_WhenUnderLimit() {
            // Act - 5 requêtes (limite = 5, donc 5 est autorisé)
            for (int i = 0; i < 5; i++) {
                refreshTokenService.checkRateLimit(EMAIL);
            }

            // Assert - aucune exception ne doit être levée
        }

        @Test
        @DisplayName("checkRateLimit - Doit lever RateLimitExceededException quand la limite de 5 est dépassée")
        void checkRateLimit_ShouldThrow_WhenLimitExceeded() {
            // Act - 5 requêtes autorisées
            for (int i = 0; i < 5; i++) {
                refreshTokenService.checkRateLimit(EMAIL);
            }

            // Act & Assert - la 6ème doit échouer
            assertThatThrownBy(() -> refreshTokenService.checkRateLimit(EMAIL))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("Trop de tentatives de rafraîchissement");
        }

        @Test
        @DisplayName("checkRateLimit - Doit réinitialiser la fenêtre après 60 secondes")
        void checkRateLimit_ShouldResetWindow_AfterOneMinute() throws InterruptedException {
            // Arrange - 5 requêtes pour saturer la fenêtre
            for (int i = 0; i < 5; i++) {
                refreshTokenService.checkRateLimit(EMAIL);
            }

            // Vérifier que la 6ème échoue
            assertThatThrownBy(() -> refreshTokenService.checkRateLimit(EMAIL))
                    .isInstanceOf(RateLimitExceededException.class);

            // Attendre 60 secondes pour que la fenêtre expire
            Thread.sleep(60100);

            // Act - une nouvelle requête doit passer
            refreshTokenService.checkRateLimit(EMAIL);
        }

        @Test
        @DisplayName("checkRateLimit - Doit tracker séparément différents emails")
        void checkRateLimit_ShouldTrackDifferentEmailsSeparately() {
            // Arrange
            String email2 = "autre@email.com";

            // Act - 5 requêtes pour email1, 1 pour email2
            for (int i = 0; i < 5; i++) {
                refreshTokenService.checkRateLimit(EMAIL);
            }
            refreshTokenService.checkRateLimit(email2);

            // Assert - email1 doit être bloqué, email2 doit passer
            assertThatThrownBy(() -> refreshTokenService.checkRateLimit(EMAIL))
                    .isInstanceOf(RateLimitExceededException.class);

            // email2 peut encore faire 4 requêtes
            for (int i = 0; i < 4; i++) {
                refreshTokenService.checkRateLimit(email2);
            }

            // La 6ème pour email2 doit échouer
            assertThatThrownBy(() -> refreshTokenService.checkRateLimit(email2))
                    .isInstanceOf(RateLimitExceededException.class);
        }

        @Test
        @DisplayName("checkRateLimit - Doit retourner le temps d'attente dans le message d'erreur")
        void checkRateLimit_ShouldIncludeRetryTimeInErrorMessage() {
            // Arrange - saturer la limite
            for (int i = 0; i < 5; i++) {
                refreshTokenService.checkRateLimit(EMAIL);
            }

            // Act & Assert
            assertThatThrownBy(() -> refreshTokenService.checkRateLimit(EMAIL))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("secondes");
        }
    }

    @Nested
    @DisplayName("Tests de resetRateLimit")
    class ResetRateLimitTests {

        @Test
        @DisplayName("resetRateLimit - Doit réinitialiser le compteur pour un email")
        void resetRateLimit_ShouldClearCounters() {
            // Arrange - saturer la limite
            for (int i = 0; i < 5; i++) {
                refreshTokenService.checkRateLimit(EMAIL);
            }

            // Vérifier que le rate limit est actif
            assertThatThrownBy(() -> refreshTokenService.checkRateLimit(EMAIL))
                    .isInstanceOf(RateLimitExceededException.class);

            // Act
            refreshTokenService.resetRateLimit(EMAIL);

            // Assert - le compteur doit être réinitialisé
            for (int i = 0; i < 5; i++) {
                refreshTokenService.checkRateLimit(EMAIL);
            }
        }

        @Test
        @DisplayName("resetRateLimit - Ne doit pas planter si l'email n'existe pas dans le cache")
        void resetRateLimit_ShouldNotThrow_WhenEmailNotInCache() {
            // Act (ne doit pas lever d'exception)
            refreshTokenService.resetRateLimit("inconnu@email.com");
        }
    }
}
