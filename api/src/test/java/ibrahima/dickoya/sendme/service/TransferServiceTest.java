package ibrahima.dickoya.sendme.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import ibrahima.dickoya.sendme.dto.request.TransferRequest;
import ibrahima.dickoya.sendme.dto.response.SimulateResponse;
import ibrahima.dickoya.sendme.dto.response.TransactionResponse;
import ibrahima.dickoya.sendme.entity.*;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.exception.TransferException;
import ibrahima.dickoya.sendme.repository.BeneficiaryRepository;
import ibrahima.dickoya.sendme.repository.TransactionRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.strategy.TransferResult;
import ibrahima.dickoya.sendme.strategy.TransferStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour TransferService")
class TransferServiceTest {

        @Mock
        private TransactionRepository transactionRepository;

        @Mock
        private BeneficiaryRepository beneficiaryRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SimulatorService simulatorService;

        @Mock
        private StripePaymentService stripePaymentService;

        @Mock
        private NotificationService notificationService;

        @Mock
        private EmailService emailService;

        @Mock
        private Map<String, TransferStrategy> strategyMap;

        @Mock
        private TransferStrategy bankTransferStrategy;

        @Mock
        private PaymentIntent paymentIntent;

        @Mock
        private StripeException stripeException;

        @InjectMocks
        private TransferService transferService;

        private User user;
        private Beneficiary beneficiary;
        private TransferRequest transferRequest;
        private SimulateResponse simulation;
        private Transaction transaction;
        private final String USER_EMAIL = "ibrahima@sendme.com";
        private final String IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";
        private final String PAYMENT_METHOD_ID = "pm_123456789";
        private final String PAYMENT_INTENT_ID = "pi_987654321";

        @BeforeEach
        void setUp() {
                user = User.builder()
                                .id(1L)
                                .firstName("Ibrahim")
                                .lastName("Dickoya")
                                .email(USER_EMAIL)
                                .isActive(true)
                                .build();

                BankDetails bankDetails = BankDetails.builder()
                                .id(1L)
                                .bankName("Societe Generale")
                                .iban("FR7630006008011234567890189")
                                .bic("BNPAFRPPXXX")
                                .build();

                beneficiary = Beneficiary.builder()
                                .id(100L)
                                .firstName("Fatou")
                                .lastName("Kone")
                                .channel(ChannelType.BANK_TRANSFER)
                                .user(user)
                                .bankDetails(bankDetails)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                bankDetails.setBeneficiary(beneficiary);

                transferRequest = TransferRequest.builder()
                                .amount(new BigDecimal("200.00"))
                                .currency("EUR")
                                .beneficiaryId(100L)
                                .paymentMethodId(PAYMENT_METHOD_ID)
                                .idempotencyKey(IDEMPOTENCY_KEY)
                                .build();

                simulation = SimulateResponse.builder()
                                .amountSent(new BigDecimal("200.00"))
                                .currencyFrom("EUR")
                                .fees(new BigDecimal("3.00"))
                                .feeRate(new BigDecimal("1.5"))
                                .totalDebited(new BigDecimal("203.00"))
                                .exchangeRate(new BigDecimal("1.00"))
                                .currencyTo("EUR")
                                .amountReceived(new BigDecimal("200.00"))
                                .build();

                transaction = Transaction.builder()
                                .id(1L)
                                .reference("TXN-20250609-0001")
                                .amountSent(new BigDecimal("200.00"))
                                .currencySent("EUR")
                                .amountReceived(new BigDecimal("200.00"))
                                .currencyReceived("EUR")
                                .exchangeRate(new BigDecimal("1.00"))
                                .fees(new BigDecimal("3.00"))
                                .status(TransactionStatus.PENDING)
                                .channel(ChannelType.BANK_TRANSFER)
                                .idempotencyKey(IDEMPOTENCY_KEY)
                                .stripePaymentIntentId(PAYMENT_INTENT_ID)
                                .stripePaymentStatus("succeeded")
                                .sender(user)
                                .beneficiary(beneficiary)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }

        @Nested
        @DisplayName("Tests de processTransfer - Flux complet")
        class ProcessTransferTests {

                @Test
                @DisplayName("Transfert réussi - Doit retourner TransactionResponse avec statut COMPLETED")
                void processTransfer_ShouldReturnCompletedTransaction_WhenSuccessful() throws StripeException {
                        // Arrange
                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(100L)).thenReturn(Optional.of(beneficiary));
                        when(simulatorService.simulate(any())).thenReturn(simulation);
                        when(stripePaymentService.createAndConfirmPayment(
                                        new BigDecimal("203.00"), PAYMENT_METHOD_ID, IDEMPOTENCY_KEY))
                                        .thenReturn(paymentIntent);
                        when(stripePaymentService.isPaymentSucceeded(paymentIntent)).thenReturn(true);
                        when(paymentIntent.getId()).thenReturn(PAYMENT_INTENT_ID);
                        when(paymentIntent.getStatus()).thenReturn("succeeded");
                        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
                        when(strategyMap.get("BANK_TRANSFER")).thenReturn(bankTransferStrategy);
                        when(bankTransferStrategy.execute(any(Transaction.class), any(Beneficiary.class)))
                                        .thenReturn(TransferResult.success("BANK-REF123"));

                        // Act
                        TransactionResponse response = transferService.processTransfer(transferRequest, USER_EMAIL);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getAmountSent()).isEqualByComparingTo(new BigDecimal("200.00"));
                        assertThat(response.getCurrencySent()).isEqualTo("EUR");
                        assertThat(response.getFees()).isEqualByComparingTo(new BigDecimal("3.00"));

                        verify(transactionRepository).findByIdempotencyKey(IDEMPOTENCY_KEY);
                        verify(userRepository).findByEmail(USER_EMAIL);
                        verify(beneficiaryRepository).findById(100L);
                        verify(simulatorService).simulate(any());
                        verify(stripePaymentService).createAndConfirmPayment(
                                        new BigDecimal("203.00"), PAYMENT_METHOD_ID, IDEMPOTENCY_KEY);
                        verify(stripePaymentService).isPaymentSucceeded(paymentIntent);
                        verify(strategyMap).get("BANK_TRANSFER");
                        verify(bankTransferStrategy).execute(any(Transaction.class), any(Beneficiary.class));
                        verify(notificationService, atLeastOnce()).createAndSend(any(), anyString(), any(), any());
                        verify(emailService).sendTransferInitiatedEmail(any(), any());
                        verify(emailService).sendTransferCompletedEmail(any(), any());
                }

                @Test
                @DisplayName("Transaction dupliquée - Doit retourner la transaction existante sans re-débiter")
                void processTransfer_ShouldReturnExistingTransaction_WhenIdempotencyKeyExists() throws StripeException {
                        // Arrange
                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY))
                                        .thenReturn(Optional.of(transaction));

                        // Act
                        TransactionResponse response = transferService.processTransfer(transferRequest, USER_EMAIL);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getReference()).isEqualTo("TXN-20250609-0001");

                        verify(transactionRepository).findByIdempotencyKey(IDEMPOTENCY_KEY);
                        verify(userRepository, never()).findByEmail(any());
                        verify(stripePaymentService, never()).createAndConfirmPayment(any(), any(), any());
                }

                @Test
                @DisplayName("Bénéficiaire inexistant - Doit lancer ResourceNotFoundException")
                void processTransfer_ShouldThrowResourceNotFoundException_WhenBeneficiaryNotFound()
                                throws StripeException {
                        // Arrange
                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(100L)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.processTransfer(transferRequest, USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessage("Beneficiaire non trouve : 100");

                        verify(stripePaymentService, never()).createAndConfirmPayment(any(), any(), any());
                }

                @Test
                @DisplayName("Bénéficiaire d'un autre utilisateur - Doit lancer TransferException")
                void processTransfer_ShouldThrowTransferException_WhenBeneficiaryBelongsToAnotherUser()
                                throws StripeException {
                        // Arrange
                        User otherUser = User.builder().id(999L).email("other@email.com").build();
                        beneficiary.setUser(otherUser);

                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(100L)).thenReturn(Optional.of(beneficiary));

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.processTransfer(transferRequest, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessage("Ce beneficiaire ne vous appartient pas");

                        verify(stripePaymentService, never()).createAndConfirmPayment(any(), any(), any());
                }

                @Test
                @DisplayName("Paiement Stripe refusé - Doit lancer TransferException")
                void processTransfer_ShouldThrowTransferException_WhenStripePaymentFails() throws StripeException {
                        // Arrange
                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(100L)).thenReturn(Optional.of(beneficiary));
                        when(simulatorService.simulate(any())).thenReturn(simulation);
                        when(stripePaymentService.createAndConfirmPayment(any(), any(), any()))
                                        .thenThrow(stripeException);

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.processTransfer(transferRequest, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessageContaining("Paiement refuse par la banque");

                        verify(transactionRepository, never()).save(any(Transaction.class));
                }

                @Test
                @DisplayName("Paiement Stripe non confirmé - Doit lancer TransferException")
                void processTransfer_ShouldThrowTransferException_WhenStripePaymentNotSucceeded()
                                throws StripeException {
                        // Arrange
                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(100L)).thenReturn(Optional.of(beneficiary));
                        when(simulatorService.simulate(any())).thenReturn(simulation);
                        when(stripePaymentService.createAndConfirmPayment(any(), any(), any()))
                                        .thenReturn(paymentIntent);
                        when(stripePaymentService.isPaymentSucceeded(paymentIntent)).thenReturn(false);
                        when(paymentIntent.getStatus()).thenReturn("requires_action");

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.processTransfer(transferRequest, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessageContaining("Paiement non confirme par Stripe");

                        verify(transactionRepository, never()).save(any(Transaction.class));
                }

                @Test
                @DisplayName("Échec de la stratégie - Doit rembourser Stripe et lancer TransferException")
                void processTransfer_ShouldRefundAndThrowException_WhenStrategyFails() throws StripeException {
                        // Arrange
                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(100L)).thenReturn(Optional.of(beneficiary));
                        when(simulatorService.simulate(any())).thenReturn(simulation);
                        when(stripePaymentService.createAndConfirmPayment(any(), any(), any()))
                                        .thenReturn(paymentIntent);
                        when(stripePaymentService.isPaymentSucceeded(paymentIntent)).thenReturn(true);
                        when(paymentIntent.getId()).thenReturn(PAYMENT_INTENT_ID);
                        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
                        when(strategyMap.get("BANK_TRANSFER")).thenReturn(bankTransferStrategy);
                        when(bankTransferStrategy.execute(any(Transaction.class), any(Beneficiary.class)))
                                        .thenReturn(TransferResult
                                                        .failure("Virement rejete par la banque destinataire"));

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.processTransfer(transferRequest, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessageContaining("Transfert echoue et carte remboursee");

                        verify(stripePaymentService).refundPayment(PAYMENT_INTENT_ID);
                        verify(notificationService, atLeastOnce()).createAndSend(any(), anyString(), any(), any());
                }

                @Test
                @DisplayName("Échec de la stratégie ET échec du remboursement Stripe - Doit lancer TransferException")
                void processTransfer_ShouldHandleRefundFailure_WhenStrategyFailsAndRefundFails()
                                throws StripeException {
                        // Arrange
                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(100L)).thenReturn(Optional.of(beneficiary));
                        when(simulatorService.simulate(any())).thenReturn(simulation);
                        when(stripePaymentService.createAndConfirmPayment(any(), any(), any()))
                                        .thenReturn(paymentIntent);
                        when(stripePaymentService.isPaymentSucceeded(paymentIntent)).thenReturn(true);
                        when(paymentIntent.getId()).thenReturn(PAYMENT_INTENT_ID);
                        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
                        when(strategyMap.get("BANK_TRANSFER")).thenReturn(bankTransferStrategy);
                        when(bankTransferStrategy.execute(any(Transaction.class), any(Beneficiary.class)))
                                        .thenReturn(TransferResult.failure("Erreur reseau"));
                        doThrow(stripeException)
                                        .when(stripePaymentService).refundPayment(PAYMENT_INTENT_ID);

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.processTransfer(transferRequest, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessageContaining("Transfert echoue et carte remboursee");

                        verify(stripePaymentService).refundPayment(PAYMENT_INTENT_ID);
                        verify(notificationService, atLeastOnce()).createAndSend(any(), anyString(), any(), any());
                }

                @Test
                @DisplayName("Stratégie non trouvée pour le canal - Doit rembourser Stripe")
                void processTransfer_ShouldRefund_WhenStrategyNotFound() throws StripeException {
                        // Arrange
                        when(transactionRepository.findByIdempotencyKey(IDEMPOTENCY_KEY)).thenReturn(Optional.empty());
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(beneficiaryRepository.findById(100L)).thenReturn(Optional.of(beneficiary));
                        when(simulatorService.simulate(any())).thenReturn(simulation);
                        when(stripePaymentService.createAndConfirmPayment(any(), any(), any()))
                                        .thenReturn(paymentIntent);
                        when(stripePaymentService.isPaymentSucceeded(paymentIntent)).thenReturn(true);
                        when(paymentIntent.getId()).thenReturn(PAYMENT_INTENT_ID);
                        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
                        when(strategyMap.get("BANK_TRANSFER")).thenReturn(null);

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.processTransfer(transferRequest, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessageContaining("Transfert echoue et carte remboursee");

                        verify(stripePaymentService).refundPayment(PAYMENT_INTENT_ID);
                }
        }

        @Nested
        @DisplayName("Tests de récupération d'historique")
        class GetHistoryTests {

                @Test
                @DisplayName("Récupérer l'historique paginé - Succès")
                void getHistory_ShouldReturnPagedTransactions_WhenSuccessful() {
                        // Arrange
                        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
                        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction));

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(transactionRepository.findBySenderIdOrderByCreatedAtDesc(user.getId(), pageable))
                                        .thenReturn(transactionPage);

                        // Act
                        Page<TransactionResponse> result = transferService.getHistory(USER_EMAIL, pageable);

                        // Assert
                        assertThat(result).isNotEmpty();
                        assertThat(result.getContent()).hasSize(1);
                        assertThat(result.getContent().get(0).getReference()).isEqualTo("TXN-20250609-0001");

                        verify(userRepository).findByEmail(USER_EMAIL);
                        verify(transactionRepository).findBySenderIdOrderByCreatedAtDesc(user.getId(), pageable);
                }

                @Test
                @DisplayName("Récupérer l'historique - Utilisateur inexistant")
                void getHistory_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.getHistory(USER_EMAIL, PageRequest.of(0, 10)))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessage("Utilisateur non trouve : " + USER_EMAIL);
                }
        }

        @Nested
        @DisplayName("Tests de récupération par ID")
        class GetByIdTests {

                @Test
                @DisplayName("Récupérer une transaction par ID - Succès")
                void getById_ShouldReturnTransaction_WhenSuccessful() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

                        // Act
                        TransactionResponse response = transferService.getById(1L, USER_EMAIL);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getId()).isEqualTo(1L);
                        assertThat(response.getReference()).isEqualTo("TXN-20250609-0001");
                }

                @Test
                @DisplayName("Récupérer une transaction inexistante - Doit lancer ResourceNotFoundException")
                void getById_ShouldThrowResourceNotFoundException_WhenTransactionNotFound() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.getById(999L, USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessage("Transaction non trouvee : 999");
                }

                @Test
                @DisplayName("Récupérer une transaction d'un autre utilisateur - Doit lancer TransferException")
                void getById_ShouldThrowTransferException_WhenTransactionBelongsToAnotherUser() {
                        // Arrange
                        User otherUser = User.builder().id(999L).email("other@email.com").build();
                        transaction.setSender(otherUser);

                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

                        // Act & Assert
                        assertThatThrownBy(() -> transferService.getById(1L, USER_EMAIL))
                                        .isInstanceOf(TransferException.class)
                                        .hasMessage("Acces refuse a cette transaction");
                }
        }
}
