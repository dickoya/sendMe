package ibrahima.dickoya.sendme.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour StripePaymentService")
class StripePaymentServiceTest {

    private StripePaymentService stripePaymentService;

    @Captor
    private ArgumentCaptor<PaymentIntentCreateParams> createParamsCaptor;

    @Captor
    private ArgumentCaptor<RefundCreateParams> refundParamsCaptor;

    @BeforeEach
    void setUp() {
        stripePaymentService = new StripePaymentService();
        ReflectionTestUtils.setField(stripePaymentService, "defaultCurrency", "EUR");
        ReflectionTestUtils.setField(stripePaymentService, "returnUrl", "http://localhost:3000/payment/callback");
    }

    @Nested
    @DisplayName("Tests de createAndConfirmPayment")
    class CreateAndConfirmPaymentTests {

        private final BigDecimal AMOUNT = new BigDecimal("203.00");
        private final String PAYMENT_METHOD_ID = "pm_test_123456";
        private final String IDEMPOTENCY_KEY = "idemp_abc123";

        @Test
        @DisplayName("createAndConfirmPayment - Doit créer un PaymentIntent avec le bon montant en centimes")
        void createAndConfirmPayment_ShouldCreatePaymentIntent_WithCorrectAmount() throws StripeException {
            // Arrange
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_test_123");
            when(mockIntent.getStatus()).thenReturn("succeeded");

            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class), any()))
                        .thenReturn(mockIntent);

                // Act
                PaymentIntent result = stripePaymentService.createAndConfirmPayment(
                        AMOUNT, PAYMENT_METHOD_ID, IDEMPOTENCY_KEY);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo("pi_test_123");
                assertThat(result.getStatus()).isEqualTo("succeeded");
            }
        }

        @Test
        @DisplayName("createAndConfirmPayment - Doit convertir 203.00 EUR en 20300 centimes")
        void createAndConfirmPayment_ShouldConvertEurToCents() throws StripeException {
            // Arrange
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class), any()))
                        .thenReturn(mockIntent);

                // Act
                stripePaymentService.createAndConfirmPayment(
                        AMOUNT, PAYMENT_METHOD_ID, IDEMPOTENCY_KEY);

                // Assert - vérifier que le montant en centimes est correct
                mockedPaymentIntent.verify(() -> PaymentIntent.create(
                        createParamsCaptor.capture(), any()));
                assertThat(createParamsCaptor.getValue().getAmount()).isEqualTo(20300L);
            }
        }

        @Test
        @DisplayName("createAndConfirmPayment - Doit utiliser la devise par défaut EUR")
        void createAndConfirmPayment_ShouldUseDefaultCurrency() throws StripeException {
            // Arrange
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class), any()))
                        .thenReturn(mockIntent);

                // Act
                stripePaymentService.createAndConfirmPayment(
                        AMOUNT, PAYMENT_METHOD_ID, IDEMPOTENCY_KEY);

                // Assert
                mockedPaymentIntent.verify(() -> PaymentIntent.create(
                        createParamsCaptor.capture(), any()));
                assertThat(createParamsCaptor.getValue().getCurrency()).isEqualTo("eur");
            }
        }

        @Test
        @DisplayName("createAndConfirmPayment - Doit utiliser la devise spécifiée")
        void createAndConfirmPayment_ShouldUseSpecifiedCurrency() throws StripeException {
            // Arrange
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class), any()))
                        .thenReturn(mockIntent);

                // Act
                stripePaymentService.createAndConfirmPayment(
                        AMOUNT, PAYMENT_METHOD_ID, IDEMPOTENCY_KEY, "USD");

                // Assert
                mockedPaymentIntent.verify(() -> PaymentIntent.create(
                        createParamsCaptor.capture(), any()));
                assertThat(createParamsCaptor.getValue().getCurrency()).isEqualTo("usd");
            }
        }

        @Test
        @DisplayName("createAndConfirmPayment - Doit propager l'exception Stripe")
        void createAndConfirmPayment_ShouldThrow_WhenStripeFails() {
            // Arrange
            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                StripeException stripeException = mock(StripeException.class);
                when(stripeException.getMessage()).thenReturn("Carte refusée");
                mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class), any()))
                        .thenThrow(stripeException);

                // Act & Assert
                assertThatThrownBy(() -> stripePaymentService.createAndConfirmPayment(
                        AMOUNT, PAYMENT_METHOD_ID, IDEMPOTENCY_KEY))
                        .isInstanceOf(StripeException.class)
                        .hasMessageContaining("Carte refusée");
            }
        }

        @Test
        @DisplayName("createAndConfirmPayment - Doit inclure l'idempotency key dans les options")
        void createAndConfirmPayment_ShouldIncludeIdempotencyKey() throws StripeException {
            // Arrange
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class), any()))
                        .thenReturn(mockIntent);

                // Act
                stripePaymentService.createAndConfirmPayment(
                        AMOUNT, PAYMENT_METHOD_ID, IDEMPOTENCY_KEY);

                // Assert
                mockedPaymentIntent.verify(() -> PaymentIntent.create(
                        any(PaymentIntentCreateParams.class),
                        argThat(options -> IDEMPOTENCY_KEY.equals(options.getIdempotencyKey()))));
            }
        }

        @Test
        @DisplayName("createAndConfirmPayment - Doit inclure les métadonnées")
        void createAndConfirmPayment_ShouldIncludeMetadata() throws StripeException {
            // Arrange
            PaymentIntent mockIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
                mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class), any()))
                        .thenReturn(mockIntent);

                // Act
                stripePaymentService.createAndConfirmPayment(
                        AMOUNT, PAYMENT_METHOD_ID, IDEMPOTENCY_KEY);

                // Assert
                mockedPaymentIntent.verify(() -> PaymentIntent.create(
                        createParamsCaptor.capture(), any()));
                assertThat(createParamsCaptor.getValue().getMetadata())
                        .containsEntry("idempotency_key", IDEMPOTENCY_KEY)
                        .containsEntry("platform", "sendme");
            }
        }
    }

    @Nested
    @DisplayName("Tests de refundPayment")
    class RefundPaymentTests {

        private final String PAYMENT_INTENT_ID = "pi_test_123";

        @Test
        @DisplayName("refundPayment - Doit créer un remboursement avec le bon PaymentIntent")
        void refundPayment_ShouldCreateRefund_WithCorrectPaymentIntent() throws StripeException {
            // Arrange
            Refund mockRefund = mock(Refund.class);
            when(mockRefund.getId()).thenReturn("re_test_123");
            when(mockRefund.getStatus()).thenReturn("succeeded");

            try (MockedStatic<Refund> mockedRefund = mockStatic(Refund.class)) {
                mockedRefund.when(() -> Refund.create(any(RefundCreateParams.class)))
                        .thenReturn(mockRefund);

                // Act
                Refund result = stripePaymentService.refundPayment(PAYMENT_INTENT_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo("re_test_123");
                assertThat(result.getStatus()).isEqualTo("succeeded");
            }
        }

        @Test
        @DisplayName("refundPayment - Doit utiliser la raison REQUESTED_BY_CUSTOMER")
        void refundPayment_ShouldUseRequestedByCustomerReason() throws StripeException {
            // Arrange
            Refund mockRefund = mock(Refund.class);

            try (MockedStatic<Refund> mockedRefund = mockStatic(Refund.class)) {
                mockedRefund.when(() -> Refund.create(any(RefundCreateParams.class)))
                        .thenReturn(mockRefund);

                // Act
                stripePaymentService.refundPayment(PAYMENT_INTENT_ID);

                // Assert
                mockedRefund.verify(() -> Refund.create(refundParamsCaptor.capture()));
                assertThat(refundParamsCaptor.getValue().getPaymentIntent())
                        .isEqualTo(PAYMENT_INTENT_ID);
                assertThat(refundParamsCaptor.getValue().getReason())
                        .isEqualTo(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
            }
        }

        @Test
        @DisplayName("refundPayment - Doit propager l'exception Stripe")
        void refundPayment_ShouldThrow_WhenStripeFails() {
            // Arrange
            try (MockedStatic<Refund> mockedRefund = mockStatic(Refund.class)) {
                StripeException stripeException = mock(StripeException.class);
                when(stripeException.getMessage()).thenReturn("Remboursement échoué");
                mockedRefund.when(() -> Refund.create(any(RefundCreateParams.class)))
                        .thenThrow(stripeException);

                // Act & Assert
                assertThatThrownBy(() -> stripePaymentService.refundPayment(PAYMENT_INTENT_ID))
                        .isInstanceOf(StripeException.class)
                        .hasMessageContaining("Remboursement échoué");
            }
        }
    }

    @Nested
    @DisplayName("Tests de isPaymentSucceeded")
    class IsPaymentSucceededTests {

        @Test
        @DisplayName("isPaymentSucceeded - Doit retourner true si le statut est 'succeeded'")
        void isPaymentSucceeded_ShouldReturnTrue_WhenStatusIsSucceeded() {
            // Arrange
            PaymentIntent paymentIntent = mock(PaymentIntent.class);
            when(paymentIntent.getStatus()).thenReturn("succeeded");

            // Act
            boolean result = stripePaymentService.isPaymentSucceeded(paymentIntent);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isPaymentSucceeded - Doit retourner false si le statut est 'requires_action'")
        void isPaymentSucceeded_ShouldReturnFalse_WhenStatusIsRequiresAction() {
            // Arrange
            PaymentIntent paymentIntent = mock(PaymentIntent.class);
            when(paymentIntent.getStatus()).thenReturn("requires_action");

            // Act
            boolean result = stripePaymentService.isPaymentSucceeded(paymentIntent);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isPaymentSucceeded - Doit retourner false si le statut est 'requires_payment_method'")
        void isPaymentSucceeded_ShouldReturnFalse_WhenStatusIsRequiresPaymentMethod() {
            // Arrange
            PaymentIntent paymentIntent = mock(PaymentIntent.class);
            when(paymentIntent.getStatus()).thenReturn("requires_payment_method");

            // Act
            boolean result = stripePaymentService.isPaymentSucceeded(paymentIntent);

            // Assert
            assertThat(result).isFalse();
        }
    }
}
