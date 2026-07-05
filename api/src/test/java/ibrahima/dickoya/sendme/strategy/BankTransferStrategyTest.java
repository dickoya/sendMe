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

@DisplayName("Tests unitaires pour BankTransferStrategy")
class BankTransferStrategyTest {

    private BankTransferStrategy strategy;
    private Transaction transaction;
    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        strategy = new BankTransferStrategy();

        User user = User.builder()
                .id(1L)
                .firstName("Ibrahim")
                .lastName("Dickoya")
                .email("ibrahima@sendme.com")
                .build();

        BeneficiaryChannel channel = BeneficiaryChannel.builder()
                .id(1L)
                .channel(ChannelType.BANK_TRANSFER)
                .bankName("Societe Generale")
                .iban("FR7630006008011234567890189")
                .bic("BNPAFRPPXXX")
                .isPrimary(true)
                .build();

        beneficiary = Beneficiary.builder()
                .id(1L)
                .firstName("Fatou")
                .lastName("Kone")
                .user(user)
                .channels(List.of(channel))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        channel.setBeneficiary(beneficiary);

        transaction = Transaction.builder()
                .id(1L)
                .reference("TXN-20250609-0001")
                .amountSent(new BigDecimal("200.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("200.00"))
                .currencyReceived("EUR")
                .exchangeRate(new BigDecimal("1.00"))
                .fees(new BigDecimal("3.00"))
                .status(TransactionStatus.PROCESSING)
                .channel(ChannelType.BANK_TRANSFER)
                .sender(user)
                .beneficiary(beneficiary)
                .build();
    }

    @Nested
    @DisplayName("Tests d'exécution du virement bancaire")
    class ExecuteTests {

        @Test
        @DisplayName("Doit retourner un succès avec une référence externe")
        void execute_ShouldReturnSuccess_WhenBankAcceptsTransfer() {
            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("Transfert effectue avec succes");
            assertThat(result.getExternalRef()).startsWith("BANK-");
        }

        @Test
        @DisplayName("La référence externe doit avoir le préfixe BANK- (ou échouer aléatoirement)")
        void execute_ExternalRef_ShouldStartWithBANK() {
            // Act
            TransferResult result = strategy.execute(transaction, beneficiary);

            // Assert
            // La stratégie a 5% de chance d'échec aléatoire (simulation de rejet bancaire)
            // Si succès, la référence doit commencer par BANK-
            if (result.isSuccess()) {
                assertThat(result.getExternalRef()).startsWith("BANK-");
            } else {
                assertThat(result.getMessage()).isEqualTo("Virement rejete par la banque destinataire");
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
            assertThat(result.getMessage()).isEqualTo("Interruption du traitement bancaire");

            // Nettoyer l'état d'interruption pour les autres tests
            Thread.interrupted();
        }
    }
}
