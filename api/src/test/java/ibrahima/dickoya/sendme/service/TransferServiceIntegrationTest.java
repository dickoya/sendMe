package ibrahima.dickoya.sendme.service;

import com.stripe.model.PaymentIntent;
import ibrahima.dickoya.sendme.config.TestH2Config;
import ibrahima.dickoya.sendme.dto.request.*;
import ibrahima.dickoya.sendme.dto.response.BeneficiaryResponse;
import ibrahima.dickoya.sendme.dto.response.SimulateResponse;
import ibrahima.dickoya.sendme.dto.response.TransactionResponse;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test d'intégration du service de transfert avec base H2.
 *
 * Couvre le flux complet :
 * 1. Création bénéficiaire → simulation → transfert → historique
 * 2. Cas d'erreur : bénéficiaire inexistant, clé d'idempotence dupliquée
 *
 * Les dépendances externes (Stripe, Email, ExchangeRate) sont mockées.
 * Les stratégies de transfert fonctionnent en mode simulation.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestH2Config.class)
class TransferServiceIntegrationTest {

        @Autowired
        private TransferService transferService;

        @Autowired
        private BeneficiaryService beneficiaryService;

        @Autowired
        private SimulatorService simulatorService;

        @Autowired
        private UserRepository userRepository;

        @MockBean
        private StripePaymentService stripePaymentService;

        @MockBean
        private EmailService emailService;

        @MockBean
        private ExchangeRateService exchangeRateService;

        @MockBean
        private NotificationService notificationService;

        private User testUser;
        private String userEmail = "test.transfer@email.com";
        private BeneficiaryResponse testBeneficiary;

        @BeforeEach
        void setUp() throws Exception {
                // Enregistrer un utilisateur via le repository directement
                // (on évite AuthService pour ne pas dépendre de ses mocks)
                testUser = User.builder()
                                .referralCode("SENDME-TESTTRANSFER")
                                .firstName("Test")
                                .lastName("Transfer")
                                .email(userEmail)
                                .passwordHash("$2a$10$dummyhash") // hash BCrypt valide
                                .phoneNumber("+33612345678")
                                .isActive(true)
                                .emailVerified(true)
                                .build();
                testUser = userRepository.save(testUser);

                // Mock du taux de change (1 EUR = 655.957 XOF)
                when(exchangeRateService.getRate("EUR", "XOF"))
                                .thenReturn(new BigDecimal("655.957"));

                // Mock du service Stripe
                PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
                when(mockPaymentIntent.getId()).thenReturn("pi_test_mock_123456789");
                when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
                when(stripePaymentService.createAndConfirmPayment(
                                any(BigDecimal.class), anyString(), anyString()))
                                .thenReturn(mockPaymentIntent);
                when(stripePaymentService.createAndConfirmPayment(
                                any(BigDecimal.class), anyString(), anyString(), anyString()))
                                .thenReturn(mockPaymentIntent);
                when(stripePaymentService.isPaymentSucceeded(any(PaymentIntent.class)))
                                .thenReturn(true);

                // Mock du service de notification
                when(notificationService.createAndSend(
                                any(User.class), anyString(), any(), any()))
                                .thenReturn(null);

                // Créer un bénéficiaire de test
                testBeneficiary = createTestBeneficiary();
        }

        @Test
        @DisplayName("Création bénéficiaire → simulation → transfert → historique : flux complet")
        void testFullTransferFlow() throws Exception {
                // ── ÉTAPE 1 : Création du bénéficiaire ────────────────────────────────
                assertThat(testBeneficiary).isNotNull();
                assertThat(testBeneficiary.getFirstName()).isEqualTo("Amadou");
                assertThat(testBeneficiary.getLastName()).isEqualTo("Diallo");
                assertThat(testBeneficiary.getChannels()).hasSize(1);

                // ── ÉTAPE 2 : Simulation du transfert ─────────────────────────────────
                SimulateRequest simulateRequest = SimulateRequest.builder()
                                .amount(new BigDecimal("200.00"))
                                .currencyFrom("EUR")
                                .currencyTo("XOF")
                                .channel(ChannelType.ORANGE_MONEY)
                                .build();

                SimulateResponse simulation = simulatorService.simulate(simulateRequest, testUser.getId());

                assertThat(simulation).isNotNull();
                assertThat(simulation.getAmountSent()).isEqualByComparingTo(new BigDecimal("200.00"));
                assertThat(simulation.getFees()).isEqualByComparingTo(new BigDecimal("3.00")); // 1.5% de 200
                assertThat(simulation.getTotalDebited()).isEqualByComparingTo(new BigDecimal("203.00"));
                assertThat(simulation.getExchangeRate()).isNotNull();
                assertThat(simulation.getAmountReceived()).isNotNull();
                assertThat(simulation.getCurrencyTo()).isEqualTo("XOF");

                // ── ÉTAPE 3 : Exécution du transfert ──────────────────────────────────
                TransferRequest transferRequest = TransferRequest.builder()
                                .amount(new BigDecimal("200.00"))
                                .currency("EUR")
                                .beneficiaryId(testBeneficiary.getId())
                                .paymentMethodId("pm_test_mock_123456")
                                .idempotencyKey("idemp-key-" + System.currentTimeMillis())
                                .build();

                TransactionResponse transferResponse = transferService.processTransfer(transferRequest, userEmail);

                assertThat(transferResponse).isNotNull();
                assertThat(transferResponse.getReference()).startsWith("TXN-");
                assertThat(transferResponse.getAmountSent()).isEqualByComparingTo(new BigDecimal("200.00"));
                assertThat(transferResponse.getCurrencySent()).isEqualTo("EUR");
                assertThat(transferResponse.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
                assertThat(transferResponse.getStripePaymentIntentId()).isEqualTo("pi_test_mock_123456789");

                // Vérifier que Stripe a été appelé
                verify(stripePaymentService, atLeastOnce()).createAndConfirmPayment(
                                any(BigDecimal.class), anyString(), anyString());

                // ── ÉTAPE 4 : Historique des transactions ─────────────────────────────
                Page<TransactionResponse> history = transferService.getHistory(
                                userEmail, PageRequest.of(0, 10));

                assertThat(history).isNotEmpty();
                assertThat(history.getTotalElements()).isEqualTo(1);
                assertThat(history.getContent().get(0).getReference())
                                .isEqualTo(transferResponse.getReference());
        }

        @Test
        @DisplayName("Clé d'idempotence dupliquée → retourne la transaction existante")
        void testIdempotencyKeyDuplicate() throws Exception {
                // Premier transfert
                String idempotencyKey = "idemp-key-duplicate-" + System.currentTimeMillis();

                TransferRequest firstRequest = TransferRequest.builder()
                                .amount(new BigDecimal("100.00"))
                                .currency("EUR")
                                .beneficiaryId(testBeneficiary.getId())
                                .paymentMethodId("pm_test_mock_123456")
                                .idempotencyKey(idempotencyKey)
                                .build();

                TransactionResponse firstResponse = transferService.processTransfer(firstRequest, userEmail);
                assertThat(firstResponse.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

                // Second transfert avec la même clé d'idempotence
                TransferRequest secondRequest = TransferRequest.builder()
                                .amount(new BigDecimal("99999.00")) // Montant différent
                                .currency("EUR")
                                .beneficiaryId(testBeneficiary.getId())
                                .paymentMethodId("pm_test_mock_999999")
                                .idempotencyKey(idempotencyKey)
                                .build();

                TransactionResponse secondResponse = transferService.processTransfer(secondRequest, userEmail);

                // Doit retourner la même transaction que la première
                assertThat(secondResponse.getId()).isEqualTo(firstResponse.getId());
                assertThat(secondResponse.getReference()).isEqualTo(firstResponse.getReference());
                assertThat(secondResponse.getAmountSent()).isEqualByComparingTo(new BigDecimal("100.00"));

                // Stripe ne doit être appelé qu'une seule fois
                verify(stripePaymentService, times(1)).createAndConfirmPayment(
                                any(BigDecimal.class), anyString(), anyString());
        }

        @Test
        @DisplayName("Transfert avec bénéficiaire inexistant → ResourceNotFoundException")
        void testTransferWithNonExistentBeneficiary() {
                TransferRequest request = TransferRequest.builder()
                                .amount(new BigDecimal("50.00"))
                                .currency("EUR")
                                .beneficiaryId(99999L) // ID inexistant
                                .paymentMethodId("pm_test_mock_123456")
                                .idempotencyKey("idemp-key-nonexistent-" + System.currentTimeMillis())
                                .build();

                assertThatThrownBy(() -> transferService.processTransfer(request, userEmail))
                                .isInstanceOf(ibrahima.dickoya.sendme.exception.ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Transfert avec email utilisateur inexistant → ResourceNotFoundException")
        void testTransferWithNonExistentUser() {
                TransferRequest request = TransferRequest.builder()
                                .amount(new BigDecimal("50.00"))
                                .currency("EUR")
                                .beneficiaryId(testBeneficiary.getId())
                                .paymentMethodId("pm_test_mock_123456")
                                .idempotencyKey("idemp-key-nonexistent-user-" + System.currentTimeMillis())
                                .build();

                assertThatThrownBy(() -> transferService.processTransfer(request, "nonexistent@email.com"))
                                .isInstanceOf(ibrahima.dickoya.sendme.exception.ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Historique vide pour un nouvel utilisateur")
        void testEmptyHistory() {
                Page<TransactionResponse> history = transferService.getHistory(
                                userEmail, PageRequest.of(0, 10));

                assertThat(history).isEmpty();
                assertThat(history.getTotalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("Simulation sans réduction (pas de coupon)")
        void testSimulationWithoutDiscount() {
                SimulateRequest simulateRequest = SimulateRequest.builder()
                                .amount(new BigDecimal("200.00"))
                                .currencyFrom("EUR")
                                .currencyTo("XOF")
                                .channel(ChannelType.ORANGE_MONEY)
                                .build();

                SimulateResponse simulation = simulatorService.simulate(simulateRequest, testUser.getId());

                assertThat(simulation.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
                assertThat(simulation.getTotalDebited()).isEqualByComparingTo(new BigDecimal("203.00"));
        }

        /**
         * Crée un bénéficiaire de test pour l'utilisateur courant.
         */
        private BeneficiaryResponse createTestBeneficiary() {
                BeneficiaryChannelRequest channelRequest = BeneficiaryChannelRequest.builder()
                                .channel(ChannelType.ORANGE_MONEY)
                                .phoneNumber("+2250701020304")
                                .countryCode("CI")
                                .isPrimary(true)
                                .build();

                BeneficiaryRequest beneficiaryRequest = BeneficiaryRequest.builder()
                                .firstName("Amadou")
                                .lastName("Diallo")
                                .channels(List.of(channelRequest))
                                .build();

                return beneficiaryService.create(beneficiaryRequest, userEmail);
        }
}
