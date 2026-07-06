
package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.BeneficiaryChannel;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires pour MoovMoneyStrategy")
class MoovMoneyStrategyTest {

    private MoovMoneyStrategy strategy;
    private Transaction transaction;
    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        strategy = new MoovMoneyStrategy();

        User user = User.builder()
                .id(1L)
                .firstName("Ibrahim")
                .lastName("Dickoya")
                .email("ibrahima@sendme.com")
                .build();

        BeneficiaryChannel channel = BeneficiaryChannel.builder()
                .id(1L)
                .channel(ChannelType.MOOV_MONEY)
                .phoneNumber("+224620000004")
                .isPrimary(true)
                .build();

        beneficiary = Beneficiary.builder()
                .id(1L)
                .firstName("Mamadou")
                .lastName("Barry")
                .user(user)
                .channels(List.of(channel))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        channel.setBeneficiary(beneficiary);

        transaction = Transaction.builder()
                .id(1L)
                .reference("TXN-20250609-0004")
                .amountSent(new BigDecimal("40.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("400000.00"))
                .currencyReceived("XOF")
                .exchangeRate(new BigDecimal("10000.00"))
                .fees(new BigDecimal("0.60"))
                .status(TransactionStatus.PROCESSING)
                .channel(ChannelType.MOOV_MONEY)
                .sender(user)
                .beneficiary(beneficiary)
                .build();
    }

    @Nested
    @DisplayName("Tests d'exécution du transfert Moov Money")
    class ExecuteTests {

        @Test
        @DisplayName("Doit retourner un succès avec une référence externe")
        void execute_ShouldReturnSuccess_WhenMoovAccepts() {
            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Transfert effectue avec succes");
            assertThat(result.getExternalRef()).startsWith("MOOV-");
        }

        @Test
        @DisplayName("La référence externe doit avoir le préfixe MOOV-")
        void execute_ExternalRef_ShouldStartWithMOOV() {
            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            assertThat(result.getExternalRef()).startsWith("MOOV-");
        }

        @Test
        @DisplayName("Doit retourner un échec en cas d'interruption du thread")
        void execute_ShouldReturnFailure_WhenThreadInterrupted() throws Exception {
            // Arrange
            // Activer le mode simulation via réflexion pour que simulateProcessingDelay
            // utilise un vrai délai et que CompletableFuture.get() bloque
            java.lang.reflect.Field simulationModeField = BaseTransferStrategy.class.getDeclaredField("simulationMode");
            simulationModeField.setAccessible(true);
            simulationModeField.set(strategy, true);

            // Interrompre le thread courant avant l'appel pour que
            // CompletableFuture.get() lance InterruptedException immédiatement
            Thread.currentThread().interrupt();

            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Interruption du traitement Moov Money");

            // Vérifier que le flag d'interruption a été restauré
            assertThat(Thread.interrupted()).isTrue();
        }
    }
}
