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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests unitaires pour AirtelMoneyStrategy")
class AirtelMoneyStrategyTest {

    private AirtelMoneyStrategy strategy;
    private Transaction transaction;
    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        strategy = new AirtelMoneyStrategy();

        User user = User.builder()
                .id(1L)
                .firstName("Ibrahim")
                .lastName("Dickoya")
                .email("ibrahima@sendme.com")
                .build();

        BeneficiaryChannel channel = BeneficiaryChannel.builder()
                .id(1L)
                .channel(ChannelType.AIRTEL_MONEY)
                .phoneNumber("+224620000005")
                .isPrimary(true)
                .build();

        beneficiary = Beneficiary.builder()
                .id(1L)
                .firstName("Kadiatou")
                .lastName("Camara")
                .channel(ChannelType.AIRTEL_MONEY)
                .user(user)
                .channels(List.of(channel))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        channel.setBeneficiary(beneficiary);

        transaction = Transaction.builder()
                .id(1L)
                .reference("TXN-20250609-0005")
                .amountSent(new BigDecimal("35.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("350000.00"))
                .currencyReceived("XOF")
                .exchangeRate(new BigDecimal("10000.00"))
                .fees(new BigDecimal("0.53"))
                .status(TransactionStatus.PROCESSING)
                .channel(ChannelType.AIRTEL_MONEY)
                .sender(user)
                .beneficiary(beneficiary)
                .build();
    }

    @Nested
    @DisplayName("Tests d'exécution du transfert Airtel Money")
    class ExecuteTests {

        @Test
        @DisplayName("Doit retourner un succès avec une référence externe")
        void execute_ShouldReturnSuccess_WhenAirtelAccepts() {
            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Transfert effectue avec succes");
            assertThat(result.getExternalRef()).startsWith("AIRTEL-");
        }

        @Test
        @DisplayName("La référence externe doit avoir le préfixe AIRTEL-")
        void execute_ExternalRef_ShouldStartWithAIRTEL() {
            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            assertThat(result.getExternalRef()).startsWith("AIRTEL-");
        }

        @Test
        @DisplayName("Doit retourner un échec en cas d'interruption du thread")
        void execute_ShouldReturnFailure_WhenThreadInterrupted() {
            // Arrange
            Thread.currentThread().interrupt();

            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getMessage()).isEqualTo("Interruption du traitement Airtel Money");

            Thread.interrupted();
        }
    }
}
