package ibrahima.dickoya.sendme.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour EmailService.
 *
 * Note : EmailService envoie désormais les emails via l'API REST Brevo (HTTPS,
 * port 443) et non plus via SMTP. Ces tests ne font AUCUN appel réseau : la clé
 * BREVO_API_KEY n'étant pas configurée dans le test, la méthode interne
 * sendHtmlEmail() court-circuite l'envoi (exception attrapée par les méthodes
 * publiques). On vérifie donc que le bon template Thymeleaf est bien généré.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour EmailService")
class EmailServiceTest {

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EmailService emailService;

    private User user;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Ibrahima")
                .lastName("Dickoya")
                .email("ibrahima@sendme.com")
                .referralCode("SENDME-ABC123")
                .build();

        Beneficiary beneficiary = Beneficiary.builder()
                .id(10L)
                .firstName("Mariama")
                .lastName("Diallo")
                .build();

        transaction = Transaction.builder()
                .id(100L)
                .reference("SENDME-REF-001")
                .amountSent(new BigDecimal("200.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("130000.00"))
                .currencyReceived("XOF")
                .exchangeRate(new BigDecimal("650.000000"))
                .fees(new BigDecimal("3.00"))
                .channel(ChannelType.ORANGE_MONEY)
                .beneficiary(beneficiary)
                .stripePaymentIntentId("pi_test_123")
                .build();

        // Injecter les valeurs @Value via ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@sendme.com");
        ReflectionTestUtils.setField(emailService, "fromName", "SendMe");
    }

    @Nested
    @DisplayName("Tests de sendEmailVerificationEmail")
    class SendEmailVerificationEmailTests {

        @Test
        @DisplayName("Doit générer le template email-verification")
        void shouldProcessEmailVerificationTemplate() {
            when(templateEngine.process(eq("emails/email-verification"), any(Context.class)))
                    .thenReturn("<html>Vérification</html>");

            emailService.sendEmailVerificationEmail(user, "token-123");

            verify(templateEngine).process(eq("emails/email-verification"), any(Context.class));
        }

        @Test
        @DisplayName("Ne doit pas lever d'exception si l'envoi échoue (pas de BREVO_API_KEY)")
        void shouldNotThrowWhenSendingFails() {
            when(templateEngine.process(eq("emails/email-verification"), any(Context.class)))
                    .thenReturn("<html>Vérification</html>");

            // Aucune BREVO_API_KEY configurée : sendHtmlEmail() lève une exception,
            // mais la méthode publique ne doit pas la propager.
            emailService.sendEmailVerificationEmail(user, "token-123");

            verify(templateEngine).process(eq("emails/email-verification"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendWelcomeEmail")
    class SendWelcomeEmailTests {

        @Test
        @DisplayName("Doit générer le template welcome")
        void shouldProcessWelcomeTemplate() {
            when(templateEngine.process(eq("emails/welcome"), any(Context.class)))
                    .thenReturn("<html>Bienvenue</html>");

            emailService.sendWelcomeEmail(user);

            verify(templateEngine).process(eq("emails/welcome"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendLoginAlertEmail")
    class SendLoginAlertEmailTests {

        @Test
        @DisplayName("Doit générer le template login-alert")
        void shouldProcessLoginAlertTemplate() {
            when(templateEngine.process(eq("emails/login-alert"), any(Context.class)))
                    .thenReturn("<html>Alerte</html>");

            emailService.sendLoginAlertEmail(user, "192.168.1.1");

            verify(templateEngine).process(eq("emails/login-alert"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendSuspiciousLoginEmail")
    class SendSuspiciousLoginEmailTests {

        @Test
        @DisplayName("Doit générer le template suspicious-login")
        void shouldProcessSuspiciousLoginTemplate() {
            when(templateEngine.process(eq("emails/suspicious-login"), any(Context.class)))
                    .thenReturn("<html>Suspect</html>");

            emailService.sendSuspiciousLoginEmail(user, "192.168.1.1", 5);

            verify(templateEngine).process(eq("emails/suspicious-login"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendTransferInitiatedEmail")
    class SendTransferInitiatedEmailTests {

        @Test
        @DisplayName("Doit générer le template transfer-initiated")
        void shouldProcessTransferInitiatedTemplate() {
            when(templateEngine.process(eq("emails/transfer-initiated"), any(Context.class)))
                    .thenReturn("<html>Initiation</html>");

            emailService.sendTransferInitiatedEmail(user, transaction);

            verify(templateEngine).process(eq("emails/transfer-initiated"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendTransferCompletedEmail")
    class SendTransferCompletedEmailTests {

        @Test
        @DisplayName("Doit générer le template transfer-completed")
        void shouldProcessTransferCompletedTemplate() {
            when(templateEngine.process(eq("emails/transfer-completed"), any(Context.class)))
                    .thenReturn("<html>Complété</html>");

            emailService.sendTransferCompletedEmail(user, transaction);

            verify(templateEngine).process(eq("emails/transfer-completed"), any(Context.class));
        }
    }


    @Nested
    @DisplayName("Tests de sendTransferFailedEmail")
    class SendTransferFailedEmailTests {

        @Test
        @DisplayName("Doit générer le template transfer-failed")
        void shouldProcessTransferFailedTemplate() {
            when(templateEngine.process(eq("emails/transfer-failed"), any(Context.class)))
                    .thenReturn("<html>Échec</html>");

            emailService.sendTransferFailedEmail(user, transaction, "Erreur de transfert");

            verify(templateEngine).process(eq("emails/transfer-failed"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendTransferRefundedEmail")
    class SendTransferRefundedEmailTests {

        @Test
        @DisplayName("Doit générer le template transfer-refunded")
        void shouldProcessTransferRefundedTemplate() {
            when(templateEngine.process(eq("emails/transfer-refunded"), any(Context.class)))
                    .thenReturn("<html>Remboursement</html>");

            emailService.sendTransferRefundedEmail(user, transaction);

            verify(templateEngine).process(eq("emails/transfer-refunded"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendPaymentConfirmedEmail")
    class SendPaymentConfirmedEmailTests {

        @Test
        @DisplayName("Doit générer le template payment-confirmed")
        void shouldProcessPaymentConfirmedTemplate() {
            when(templateEngine.process(eq("emails/payment-confirmed"), any(Context.class)))
                    .thenReturn("<html>Paiement confirmé</html>");

            emailService.sendPaymentConfirmedEmail(user, transaction);

            verify(templateEngine).process(eq("emails/payment-confirmed"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendPaymentFailedEmail")
    class SendPaymentFailedEmailTests {

        @Test
        @DisplayName("Doit générer le template payment-failed")
        void shouldProcessPaymentFailedTemplate() {
            when(templateEngine.process(eq("emails/payment-failed"), any(Context.class)))
                    .thenReturn("<html>Paiement refusé</html>");

            emailService.sendPaymentFailedEmail(user, transaction, "Carte refusée");

            verify(templateEngine).process(eq("emails/payment-failed"), any(Context.class));
        }
    }


    @Nested
    @DisplayName("Tests de sendAccountDisabledEmail")
    class SendAccountDisabledEmailTests {

        @Test
        @DisplayName("Doit générer le template account-disabled")
        void shouldProcessAccountDisabledTemplate() {
            when(templateEngine.process(eq("emails/account-disabled"), any(Context.class)))
                    .thenReturn("<html>Compte suspendu</html>");

            emailService.sendAccountDisabledEmail(user);

            verify(templateEngine).process(eq("emails/account-disabled"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendAccountReactivatedEmail")
    class SendAccountReactivatedEmailTests {

        @Test
        @DisplayName("Doit générer le template account-reactivated")
        void shouldProcessAccountReactivatedTemplate() {
            when(templateEngine.process(eq("emails/account-reactivated"), any(Context.class)))
                    .thenReturn("<html>Compte réactivé</html>");

            emailService.sendAccountReactivatedEmail(user);

            verify(templateEngine).process(eq("emails/account-reactivated"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendMaintenanceEmail")
    class SendMaintenanceEmailTests {

        @Test
        @DisplayName("Doit générer le template maintenance")
        void shouldProcessMaintenanceTemplate() {
            when(templateEngine.process(eq("emails/maintenance"), any(Context.class)))
                    .thenReturn("<html>Maintenance</html>");

            emailService.sendMaintenanceEmail(user, "15 juin 2025 à 02:00", "2 heures");

            verify(templateEngine).process(eq("emails/maintenance"), any(Context.class));
        }
    }

    @Nested
    @DisplayName("Tests de sendFeeUpdateEmail")
    class SendFeeUpdateEmailTests {

        @Test
        @DisplayName("Doit générer le template fee-update")
        void shouldProcessFeeUpdateTemplate() {
            when(templateEngine.process(eq("emails/fee-update"), any(Context.class)))
                    .thenReturn("<html>Changement tarifs</html>");

            emailService.sendFeeUpdateEmail(user, new BigDecimal("1.5"), new BigDecimal("2.0"), "01 août 2025");

            verify(templateEngine).process(eq("emails/fee-update"), any(Context.class));
        }
    }
}

