package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour TransactionPersistenceService")
class TransactionPersistenceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionPersistenceService persistenceService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private Transaction originalTransaction;
    private User sender;
    private Beneficiary beneficiary;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .email("ibrahima@sendme.com")
                .build();

        beneficiary = Beneficiary.builder()
                .id(10L)
                .firstName("Mariama")
                .lastName("Diallo")
                .build();

        originalTransaction = Transaction.builder()
                .id(100L)
                .reference("SENDME-REF-001")
                .amountSent(new BigDecimal("200.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("130000.00"))
                .currencyReceived("XOF")
                .exchangeRate(new BigDecimal("650.000000"))
                .fees(new BigDecimal("3.00"))
                .status(TransactionStatus.PENDING)
                .channel(ChannelType.ORANGE_MONEY)
                .idempotencyKey("idemp-12345")
                .sender(sender)
                .beneficiary(beneficiary)
                .stripePaymentIntentId("pi_original")
                .stripePaymentStatus("succeeded")
                .build();
    }

    @Nested
    @DisplayName("Tests de saveFailedTransaction")
    class SaveFailedTransactionTests {

        @Test
        @DisplayName("saveFailedTransaction - Doit créer une nouvelle transaction FAILED")
        void saveFailedTransaction_ShouldCreateFailedTransaction() {
            // Arrange
            Transaction savedFailed = Transaction.builder()
                    .id(200L)
                    .reference("SENDME-REF-001-F-12345")
                    .status(TransactionStatus.FAILED)
                    .stripePaymentIntentId("pi_failed")
                    .stripePaymentStatus("payment_failed")
                    .build();

            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedFailed);

            // Act
            Transaction result = persistenceService.saveFailedTransaction(
                    originalTransaction, "pi_failed", "payment_failed");

            // Assert
            assertThat(result.getStatus()).isEqualTo(TransactionStatus.FAILED);
            assertThat(result.getStripePaymentIntentId()).isEqualTo("pi_failed");
            assertThat(result.getStripePaymentStatus()).isEqualTo("payment_failed");

            verify(transactionRepository).save(transactionCaptor.capture());
            Transaction captured = transactionCaptor.getValue();

            // Vérifier que c'est une NOUVELLE entité (pas la même référence)
            assertThat(captured.getId()).isNull();
            assertThat(captured.getReference()).startsWith("SENDME-REF-001-F-");
            assertThat(captured.getIdempotencyKey()).startsWith("idemp-12345-F-");
            assertThat(captured.getStatus()).isEqualTo(TransactionStatus.FAILED);

            // Vérifier que les champs sont copiés de l'originale
            assertThat(captured.getAmountSent()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(captured.getCurrencySent()).isEqualTo("EUR");
            assertThat(captured.getAmountReceived()).isEqualByComparingTo(new BigDecimal("130000.00"));
            assertThat(captured.getCurrencyReceived()).isEqualTo("XOF");
            assertThat(captured.getExchangeRate()).isEqualByComparingTo(new BigDecimal("650.000000"));
            assertThat(captured.getFees()).isEqualByComparingTo(new BigDecimal("3.00"));
            assertThat(captured.getChannel()).isEqualTo(ChannelType.ORANGE_MONEY);
            assertThat(captured.getSender()).isEqualTo(sender);
            assertThat(captured.getBeneficiary()).isEqualTo(beneficiary);
        }

        @Test
        @DisplayName("saveFailedTransaction - Doit tronquer stripePaymentStatus à 490 caractères")
        void saveFailedTransaction_ShouldTruncateLongStatus() {
            // Arrange
            String longStatus = "x".repeat(1000);
            when(transactionRepository.save(any(Transaction.class)))
                    .thenReturn(Transaction.builder().build());

            // Act
            persistenceService.saveFailedTransaction(
                    originalTransaction, "pi_failed", longStatus);

            // Assert
            verify(transactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getStripePaymentStatus()).hasSize(490);
        }

        @Test
        @DisplayName("saveFailedTransaction - Doit gérer stripePaymentStatus null")
        void saveFailedTransaction_ShouldHandleNullStatus() {
            // Arrange
            when(transactionRepository.save(any(Transaction.class)))
                    .thenReturn(Transaction.builder().build());

            // Act
            persistenceService.saveFailedTransaction(
                    originalTransaction, "pi_failed", null);

            // Assert
            verify(transactionRepository).save(transactionCaptor.capture());
            assertThat(transactionCaptor.getValue().getStripePaymentStatus()).isNull();
        }

        @Test
        @DisplayName("saveFailedTransaction - Doit générer un suffixe aléatoire différent")
        void saveFailedTransaction_ShouldGenerateRandomSuffix() {
            // Arrange
            when(transactionRepository.save(any(Transaction.class)))
                    .thenReturn(Transaction.builder().build());

            // Act - appeler deux fois
            persistenceService.saveFailedTransaction(originalTransaction, "pi_1", "failed");
            persistenceService.saveFailedTransaction(originalTransaction, "pi_2", "failed");

            // Assert
            verify(transactionRepository, times(2)).save(transactionCaptor.capture());
            var allValues = transactionCaptor.getAllValues();
            String ref1 = allValues.get(0).getReference();
            String ref2 = allValues.get(1).getReference();
            // Les références doivent être différentes (suffixe aléatoire)
            assertThat(ref1).isNotEqualTo(ref2);
        }
    }
}
