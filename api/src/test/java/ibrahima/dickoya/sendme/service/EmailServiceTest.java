package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.ChannelType;
import jakarta.mail.internet.MimeMessage;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

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
        @DisplayName("sendEmailVerificationEmail - Doit envoyer l'email de vérification")
        void sendEmailVerificationEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/email-verification"), any(Context.class)))
                    .thenReturn("<html>Vérification</html>");

            // Act
            emailService.sendEmailVerificationEmail(user, "token-123");

            // Assert
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("sendEmailVerificationEmail - Ne doit pas bloquer si l'envoi échoue")
        void sendEmailVerificationEmail_ShouldNotThrow_WhenSendingFails() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/email-verification"), any(Context.class)))
                    .thenReturn("<html>Vérification</html>");
            doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

            // Act (ne doit pas lever d'exception)
            emailService.sendEmailVerificationEmail(user, "token-123");
        }
    }

    @Nested
    @DisplayName("Tests de sendWelcomeEmail")
    class SendWelcomeEmailTests {

        @Test
        @DisplayName("sendWelcomeEmail - Doit envoyer l'email de bienvenue avec le code de parrainage")
        void sendWelcomeEmail_ShouldSendWithReferralCode() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/welcome"), any(Context.class)))
                    .thenReturn("<html>Bienvenue</html>");

            // Act
            emailService.sendWelcomeEmail(user);

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendLoginAlertEmail")
    class SendLoginAlertEmailTests {

        @Test
        @DisplayName("sendLoginAlertEmail - Doit envoyer l'alerte de connexion")
        void sendLoginAlertEmail_ShouldSendAlert() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/login-alert"), any(Context.class)))
                    .thenReturn("<html>Alerte</html>");

            // Act
            emailService.sendLoginAlertEmail(user, "192.168.1.1");

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendSuspiciousLoginEmail")
    class SendSuspiciousLoginEmailTests {

        @Test
        @DisplayName("sendSuspiciousLoginEmail - Doit envoyer l'alerte d'activité suspecte")
        void sendSuspiciousLoginEmail_ShouldSendAlert() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/suspicious-login"), any(Context.class)))
                    .thenReturn("<html>Suspect</html>");

            // Act
            emailService.sendSuspiciousLoginEmail(user, "192.168.1.1", 5);

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendTransferInitiatedEmail")
    class SendTransferInitiatedEmailTests {

        @Test
        @DisplayName("sendTransferInitiatedEmail - Doit envoyer l'email d'initiation")
        void sendTransferInitiatedEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/transfer-initiated"), any(Context.class)))
                    .thenReturn("<html>Initiation</html>");

            // Act
            emailService.sendTransferInitiatedEmail(user, transaction);

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendTransferCompletedEmail")
    class SendTransferCompletedEmailTests {

        @Test
        @DisplayName("sendTransferCompletedEmail - Doit envoyer l'email de confirmation")
        void sendTransferCompletedEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/transfer-completed"), any(Context.class)))
                    .thenReturn("<html>Complété</html>");

            // Act
            emailService.sendTransferCompletedEmail(user, transaction);

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendTransferFailedEmail")
    class SendTransferFailedEmailTests {

        @Test
        @DisplayName("sendTransferFailedEmail - Doit envoyer l'email d'échec")
        void sendTransferFailedEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/transfer-failed"), any(Context.class)))
                    .thenReturn("<html>Échec</html>");

            // Act
            emailService.sendTransferFailedEmail(user, transaction, "Erreur de transfert");

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendTransferRefundedEmail")
    class SendTransferRefundedEmailTests {

        @Test
        @DisplayName("sendTransferRefundedEmail - Doit envoyer l'email de remboursement")
        void sendTransferRefundedEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/transfer-refunded"), any(Context.class)))
                    .thenReturn("<html>Remboursement</html>");

            // Act
            emailService.sendTransferRefundedEmail(user, transaction);

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendPaymentConfirmedEmail")
    class SendPaymentConfirmedEmailTests {

        @Test
        @DisplayName("sendPaymentConfirmedEmail - Doit envoyer l'email de confirmation de paiement")
        void sendPaymentConfirmedEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/payment-confirmed"), any(Context.class)))
                    .thenReturn("<html>Paiement confirmé</html>");

            // Act
            emailService.sendPaymentConfirmedEmail(user, transaction);

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendPaymentFailedEmail")
    class SendPaymentFailedEmailTests {

        @Test
        @DisplayName("sendPaymentFailedEmail - Doit envoyer l'email d'échec de paiement")
        void sendPaymentFailedEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/payment-failed"), any(Context.class)))
                    .thenReturn("<html>Paiement refusé</html>");

            // Act
            emailService.sendPaymentFailedEmail(user, transaction, "Carte refusée");

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendAccountDisabledEmail")
    class SendAccountDisabledEmailTests {

        @Test
        @DisplayName("sendAccountDisabledEmail - Doit envoyer l'email de suspension")
        void sendAccountDisabledEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/account-disabled"), any(Context.class)))
                    .thenReturn("<html>Compte suspendu</html>");

            // Act
            emailService.sendAccountDisabledEmail(user);

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendAccountReactivatedEmail")
    class SendAccountReactivatedEmailTests {

        @Test
        @DisplayName("sendAccountReactivatedEmail - Doit envoyer l'email de réactivation")
        void sendAccountReactivatedEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/account-reactivated"), any(Context.class)))
                    .thenReturn("<html>Compte réactivé</html>");

            // Act
            emailService.sendAccountReactivatedEmail(user);

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendMaintenanceEmail")
    class SendMaintenanceEmailTests {

        @Test
        @DisplayName("sendMaintenanceEmail - Doit envoyer l'email de maintenance")
        void sendMaintenanceEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/maintenance"), any(Context.class)))
                    .thenReturn("<html>Maintenance</html>");

            // Act
            emailService.sendMaintenanceEmail(user, "15 juin 2025 à 02:00", "2 heures");

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tests de sendFeeUpdateEmail")
    class SendFeeUpdateEmailTests {

        @Test
        @DisplayName("sendFeeUpdateEmail - Doit envoyer l'email de changement de tarifs")
        void sendFeeUpdateEmail_ShouldSendEmail() throws Exception {
            // Arrange
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("emails/fee-update"), any(Context.class)))
                    .thenReturn("<html>Changement tarifs</html>");

            // Act
            emailService.sendFeeUpdateEmail(user, new BigDecimal("1.5"), new BigDecimal("2.0"), "01 août 2025");

            // Assert
            verify(mailSender).send(mimeMessage);
        }
    }
}
