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

@DisplayName("Tests unitaires pour OrangeMoneyStrategy")
class OrangeMoneyStrategyTest {

    private OrangeMoneyStrategy strategy;
    private Transaction transaction;
    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        strategy = new OrangeMoneyStrategy();

        User user = User.builder()
                .id(1L)
                .firstName("Ibrahim")
                .lastName("Dickoya")
                .email("ibrahima@sendme.com")
                .build();

        BeneficiaryChannel channel = BeneficiaryChannel.builder()
                .id(1L)
                .channel(ChannelType.ORANGE_MONEY)
                .phoneNumber("+224620000001")
                .isPrimary(true)
                .build();

        beneficiary = Beneficiary.builder()
                .id(1L)
                .firstName("Amadou")
                .lastName("Diallo")
                .channel(ChannelType.ORANGE_MONEY)
                .user(user)
                .channels(List.of(channel))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        channel.setBeneficiary(beneficiary);

        transaction = Transaction.builder()
                .id(1L)
                .reference("TXN-20250609-0001")
                .amountSent(new BigDecimal("50.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("500000.00"))
                .currencyReceived("GNF")
                .exchangeRate(new BigDecimal("10000.00"))
                .fees(new BigDecimal("0.75"))
                .status(TransactionStatus.PROCESSING)
                .channel(ChannelType.ORANGE_MONEY)
                .sender(user)
                .beneficiary(beneficiary)
                .build();
    }

    @Nested
    @DisplayName("Tests d'exécution du transfert Orange Money")
    class ExecuteTests {

        @Test
        @DisplayName("Doit retourner un succès avec une référence externe")
        void execute_ShouldReturnSuccess_WhenOrangeMoneyAccepts() {
            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Transfert effectue avec succes");
            assertThat(result.getExternalRef()).startsWith("OM-");
        }

        @Test
        @DisplayName("La référence externe doit avoir le préfixe OM- en cas de succès")
        void execute_ExternalRef_ShouldStartWithOM() {
            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            if (result.isSuccess()) {
                assertThat(result.getExternalRef()).startsWith("OM-");
            } else {
                assertThat(result.getExternalRef()).isNull();
            }
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
            assertThat(result.getMessage()).isEqualTo("Interruption du traitement Orange Money");

            // Nettoyer l'état d'interruption
            Thread.interrupted();
        }
    }
}
