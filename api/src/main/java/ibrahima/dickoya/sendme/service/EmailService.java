package ibrahima.dickoya.sendme.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.entity.Transaction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service central gérant l'envoi de tous les emails transactionnels SendMe.
 *
 * Architecture :
 * - Une méthode privée centrale : sendHtmlEmail()
 * - Des méthodes publiques par type d'email (ajoutées phase par phase)
 * - Toutes les méthodes publiques sont @Async pour ne pas bloquer les requêtes HTTP
 *
 * Stack technique :
 * - JavaMailSender    : client SMTP fourni par Spring Boot
 * - TemplateEngine    : moteur Thymeleaf pour générer le HTML des emails
 * - MimeMessageHelper : helper Spring pour construire les emails HTML
 *
 * En sandbox Mailtrap :
 * Aucun vrai email n'est envoyé — tout est intercepté et visible
 * dans la boîte Mailtrap pour vérification et débogage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    /**
     * Client SMTP injecté par Spring Boot.
     * Configuré automatiquement via les propriétés spring.mail.*
     * dans application-local.properties.
     */
    private final JavaMailSender mailSender;

    /**
     * Moteur de templates Thymeleaf.
     * Transforme les fichiers HTML (templates/emails/*.html)
     * en emails HTML personnalisés avec les variables de contexte.
     */
    private final TemplateEngine templateEngine;

    /**
     * Adresse email de l'expéditeur.
     * Injectée depuis application-local.properties.
     * Exemple : noreply@sendme.com
     */
    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Nom affiché de l'expéditeur dans les emails.
     * Injectée depuis application-local.properties.
     * Affiché dans le client email comme : "SendMe <noreply@sendme.com>"
     */
    @Value("${app.mail.name}")
    private String fromName;

    /**
     * Format de date utilisé dans tous les emails.
     * Exemple : "09 juin 2025 à 14:32"
     */
    public static final DateTimeFormatter EMAIL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm");

    /**
     * Méthode de test — envoie un email simple pour vérifier
     * que la configuration SMTP fonctionne correctement.
     *
     * À appeler UNE SEULE FOIS après la configuration
     * pour valider la connexion Mailtrap.
     *
     * @param toEmail adresse email de destination pour le test
     */
    @Async
    public void sendTestEmail(String toEmail) {
        try {
            log.info("[EMAIL] Envoi de l'email de test vers : {}", toEmail);

            // Préparer le contexte Thymeleaf avec les variables du template
            Context context = new Context();
            context.setVariable("testDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));
            context.setVariable("toEmail", toEmail);

            // Générer le HTML à partir du template Thymeleaf
            String htmlContent = templateEngine.process(
                    "emails/test", context);

            // Envoyer l'email
            sendHtmlEmail(
                    toEmail,
                    "✅ SendMe — Test de configuration email",
                    htmlContent
            );

            log.info("[EMAIL] Email de test envoyé avec succès vers : {}", toEmail);

        } catch (Exception e) {
            log.error("[EMAIL] Échec envoi email de test : {}", e.getMessage());
            throw new RuntimeException("Échec envoi email de test : " + e.getMessage());
        }
    }
    /**
     * Envoie l'email de bienvenue après l'inscription.
     *
     * Déclenché automatiquement par AuthService.register()
     * après la sauvegarde du compte en base de données.
     *
     * Variables Thymeleaf utilisées dans welcome.html :
     * - firstName  : prénom seul pour le titre de bienvenue
     * - fullName   : prénom + nom pour la salutation
     * - email      : email de connexion du nouvel utilisateur
     *
     * @param user l'utilisateur qui vient de s'inscrire
     */
    @Async
    public void sendWelcomeEmail(User user) {
        try {
            log.info("[EMAIL] Envoi email de bienvenue à : {}", user.getEmail());

            // Préparer le contexte avec les variables du template
            Context context = new Context();
            context.setVariable("firstName", user.getFirstName());
            context.setVariable("fullName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("email", user.getEmail());

            // Générer le HTML depuis le template Thymeleaf
            String htmlContent = templateEngine.process(
                    "emails/welcome", context);

            // Envoyer l'email
            sendHtmlEmail(
                    user.getEmail(),
                    "🎉 Bienvenue sur SendMe — Votre compte est prêt !",
                    htmlContent
            );

            log.info("[EMAIL] Email de bienvenue envoyé à : {}", user.getEmail());

        } catch (Exception e) {
            // On ne bloque jamais l'inscription si l'email échoue
            log.error("[EMAIL] Échec email bienvenue pour {} : {}",
                    user.getEmail(), e.getMessage());
        }
    }

    /**
     * Envoie l'email de notification de suspension de compte.
     *
     * Déclenché par AdminService.toggleUserStatus()
     * quand l'admin désactive un compte utilisateur.
     *
     * Variables Thymeleaf utilisées dans account-disabled.html :
     * - fullName     : prénom + nom pour la salutation
     * - email        : email du compte suspendu
     * - disabledDate : date et heure de la suspension
     *
     * @param user l'utilisateur dont le compte vient d'être suspendu
     */
    @Async
    public void sendAccountDisabledEmail(User user) {
        try {
            log.info("[EMAIL] Envoi email suspension compte à : {}", user.getEmail());

            Context context = new Context();
            context.setVariable("fullName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("email", user.getEmail());
            context.setVariable("disabledDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));

            String htmlContent = templateEngine.process(
                    "emails/account-disabled", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "⚠️ Votre compte SendMe a été suspendu",
                    htmlContent
            );

            log.info("[EMAIL] Email suspension compte envoyé à : {}", user.getEmail());

        } catch (Exception e) {
            log.error("[EMAIL] Échec email suspension pour {} : {}",
                    user.getEmail(), e.getMessage());
        }
    }

    /**
     * Envoie l'email de notification de réactivation de compte.
     *
     * Déclenché par AdminService.toggleUserStatus()
     * quand l'admin réactive un compte utilisateur préalablement suspendu.
     *
     * Variables Thymeleaf utilisées dans account-reactivated.html :
     * - fullName         : prénom + nom pour la salutation
     * - email            : email du compte réactivé
     * - reactivatedDate  : date et heure de la réactivation
     *
     * @param user l'utilisateur dont le compte vient d'être réactivé
     */
    @Async
    public void sendAccountReactivatedEmail(User user) {
        try {
            log.info("[EMAIL] Envoi email réactivation compte à : {}", user.getEmail());

            Context context = new Context();
            context.setVariable("fullName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("email", user.getEmail());
            context.setVariable("reactivatedDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));

            String htmlContent = templateEngine.process(
                    "emails/account-reactivated", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "✅ Votre compte SendMe a été réactivé",
                    htmlContent
            );

            log.info("[EMAIL] Email réactivation compte envoyé à : {}", user.getEmail());

        } catch (Exception e) {
            log.error("[EMAIL] Échec email réactivation pour {} : {}",
                    user.getEmail(), e.getMessage());
        }
    }

    /**
     * Méthode centrale privée pour envoyer un email HTML.
     *
     * Toutes les méthodes publiques de ce service appellent
     * cette méthode pour l'envoi effectif via SMTP.
     *
     * Paramètres MimeMessageHelper :
     * - multipart = true  : nécessaire pour les emails HTML avec pièces jointes
     * - encoding = UTF-8  : support des caractères accentués (français)
     *
     * @param to          adresse email du destinataire
     * @param subject     objet de l'email affiché dans le client email
     * @param htmlContent contenu HTML généré par Thymeleaf
     * @throws Exception  si l'envoi SMTP échoue
     */
    protected void sendHtmlEmail(
            String to,
            String subject,
            String htmlContent) throws Exception {

        // Créer le message MIME (format standard des emails HTML)
        MimeMessage message = mailSender.createMimeMessage();

        // Helper Spring pour construire facilement le message
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                true,      // multipart = true pour supporter HTML + pièces jointes
                "UTF-8"    // encodage pour les accents et caractères spéciaux
        );

        // Expéditeur affiché : "SendMe <noreply@sendme.com>"
        helper.setFrom(fromEmail, fromName);

        // Destinataire
        helper.setTo(to);

        // Objet de l'email
        helper.setSubject(subject);

        // Corps de l'email en HTML (true = HTML, false = texte brut)
        helper.setText(htmlContent, true);

        // Envoyer via SMTP (Mailtrap en sandbox)
        mailSender.send(message);

        log.debug("[EMAIL] Email envoyé → {} | Objet : {}", to, subject);
    }
    /**
     * Envoie un email d'alerte de nouvelle connexion.
     *
     * Déclenché à chaque connexion réussie via POST /api/auth/login.
     * Permet à l'utilisateur de détecter une connexion non autorisée
     * et de sécuriser son compte rapidement.
     *
     * Variables Thymeleaf utilisées dans login-alert.html :
     * - fullName  : prénom + nom pour la salutation
     * - email     : email du compte connecté
     * - ipAddress : adresse IP de la connexion
     * - loginDate : date et heure de la connexion
     *
     * @param user      l'utilisateur qui vient de se connecter
     * @param ipAddress l'adresse IP depuis laquelle la connexion a eu lieu
     */
    @Async
    public void sendLoginAlertEmail(User user, String ipAddress) {
        try {
            log.info("[EMAIL] Envoi alerte connexion à : {} depuis IP : {}",
                    user.getEmail(), ipAddress);

            Context context = new Context();
            context.setVariable("fullName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("email", user.getEmail());
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("loginDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));

            String htmlContent = templateEngine.process(
                    "emails/login-alert", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "🔒 Nouvelle connexion détectée sur votre compte SendMe",
                    htmlContent
            );

            log.info("[EMAIL] Alerte connexion envoyée à : {}", user.getEmail());

        } catch (Exception e) {
            // Ne jamais bloquer le login si l'email échoue
            log.error("[EMAIL] Échec alerte connexion pour {} : {}",
                    user.getEmail(), e.getMessage());
        }
    }

    /**
     * Envoie un email d'alerte de tentatives de connexion suspectes.
     *
     * Déclenché dans AuthService.login() quand le nombre de tentatives
     * échouées consécutives atteint le seuil défini (5 par défaut).
     *
     * Variables Thymeleaf utilisées dans suspicious-login.html :
     * - fullName        : prénom + nom pour la salutation
     * - email           : email du compte ciblé
     * - ipAddress       : adresse IP des tentatives
     * - attempts        : nombre de tentatives échouées
     * - lastAttemptDate : date et heure de la dernière tentative
     *
     * @param user      l'utilisateur dont le compte est ciblé
     * @param ipAddress l'adresse IP depuis laquelle viennent les tentatives
     * @param attempts  le nombre de tentatives échouées consécutives
     */
    @Async
    public void sendSuspiciousLoginEmail(User user, String ipAddress, int attempts) {
        try {
            log.warn("[EMAIL] Envoi alerte activité suspecte à : {} — {} tentatives",
                    user.getEmail(), attempts);

            Context context = new Context();
            context.setVariable("fullName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("email", user.getEmail());
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("attempts", attempts);
            context.setVariable("lastAttemptDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));

            String htmlContent = templateEngine.process(
                    "emails/suspicious-login", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "🚨 Alerte sécurité — Activité suspecte sur votre compte SendMe",
                    htmlContent
            );

            log.warn("[EMAIL] Alerte activité suspecte envoyée à : {}", user.getEmail());

        } catch (Exception e) {
            log.error("[EMAIL] Échec alerte suspecte pour {} : {}",
                    user.getEmail(), e.getMessage());
        }
    }
    /**
     * Envoie l'email de confirmation d'initiation de transfert.
     *
     * Déclenché dans TransferService.processTransfer()
     * juste après la création de la transaction en base (statut PENDING).
     * Permet à l'utilisateur de suivre son transfert dès le début.
     *
     * Variables du template transfer-initiated.html :
     * - senderName      : prénom + nom de l'expéditeur
     * - reference       : référence unique de la transaction
     * - amountSent      : montant envoyé
     * - currencySent    : devise d'envoi
     * - fees            : frais prélevés
     * - totalDebited    : total débité (montant + frais)
     * - beneficiaryName : prénom + nom du bénéficiaire
     * - channel         : canal de réception formaté
     * - initiatedDate   : date et heure d'initiation
     *
     * @param user        l'expéditeur du transfert
     * @param transaction la transaction créée
     */
    @Async
    public void sendTransferInitiatedEmail(User user, Transaction transaction) {
        try {
            log.info("[EMAIL] Envoi email transfert initié pour : {}",
                    transaction.getReference());

            // Calculer le total débité
            java.math.BigDecimal totalDebited = transaction.getAmountSent()
                    .add(transaction.getFees());

            // Formater le nom du canal
            String channelDisplay = transaction.getChannel()
                    .name().replace("_", " ");

            // Nom du bénéficiaire
            String beneficiaryName = transaction.getBeneficiary().getFirstName()
                    + " " + transaction.getBeneficiary().getLastName();

            Context context = new Context();
            context.setVariable("senderName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("reference", transaction.getReference());
            context.setVariable("amountSent", transaction.getAmountSent());
            context.setVariable("currencySent", transaction.getCurrencySent());
            context.setVariable("fees", transaction.getFees());
            context.setVariable("totalDebited", totalDebited);
            context.setVariable("beneficiaryName", beneficiaryName);
            context.setVariable("channel", channelDisplay);
            context.setVariable("initiatedDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));

            String htmlContent = templateEngine.process(
                    "emails/transfer-initiated", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "⏳ Transfert " + transaction.getReference() +
                            " initié — Traitement en cours",
                    htmlContent
            );

            log.info("[EMAIL] Email transfert initié envoyé pour : {}",
                    transaction.getReference());

        } catch (Exception e) {
            log.error("[EMAIL] Échec email transfert initié {} : {}",
                    transaction.getReference(), e.getMessage());
        }
    }

    /**
     * Envoie l'email de confirmation de transfert complété.
     *
     * Déclenché dans TransferService.processTransfer()
     * après que la stratégie confirme le succès du transfert
     * et que le statut passe à COMPLETED.
     *
     * Variables du template transfer-completed.html :
     * - senderName      : prénom + nom de l'expéditeur
     * - reference       : référence unique
     * - amountSent      : montant envoyé
     * - currencySent    : devise d'envoi
     * - fees            : frais prélevés
     * - totalDebited    : total débité
     * - exchangeRate    : taux de change appliqué
     * - amountReceived  : montant reçu par le bénéficiaire
     * - currencyReceived: devise de réception
     * - beneficiaryName : prénom + nom du bénéficiaire
     * - channel         : canal de réception formaté
     * - stripeRef       : identifiant PaymentIntent Stripe
     * - completedDate   : date et heure de complétion
     * - receiptUrl      : URL pour télécharger le reçu PDF
     *
     * @param user        l'expéditeur du transfert
     * @param transaction la transaction complétée
     */
    @Async
    public void sendTransferCompletedEmail(User user, Transaction transaction) {
        try {
            log.info("[EMAIL] Envoi email transfert complété pour : {}",
                    transaction.getReference());

            java.math.BigDecimal totalDebited = transaction.getAmountSent()
                    .add(transaction.getFees());

            String channelDisplay = transaction.getChannel()
                    .name().replace("_", " ");

            String beneficiaryName = transaction.getBeneficiary().getFirstName()
                    + " " + transaction.getBeneficiary().getLastName();

            Context context = new Context();
            context.setVariable("senderName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("reference", transaction.getReference());
            context.setVariable("amountSent", transaction.getAmountSent());
            context.setVariable("currencySent", transaction.getCurrencySent());
            context.setVariable("fees", transaction.getFees());
            context.setVariable("totalDebited", totalDebited);
            context.setVariable("exchangeRate", transaction.getExchangeRate());
            context.setVariable("amountReceived", transaction.getAmountReceived());
            context.setVariable("currencyReceived", transaction.getCurrencyReceived());
            context.setVariable("beneficiaryName", beneficiaryName);
            context.setVariable("channel", channelDisplay);
            context.setVariable("stripeRef", transaction.getStripePaymentIntentId());
            context.setVariable("completedDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));
            // URL du reçu PDF — à adapter avec l'URL frontend en production
            context.setVariable("receiptUrl",
                    "http://localhost:3000/transactions/" +
                            transaction.getId() + "/receipt");

            String htmlContent = templateEngine.process(
                    "emails/transfer-completed", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "✅ Transfert " + transaction.getReference() +
                            " complété — " + beneficiaryName +
                            " a reçu " + transaction.getAmountReceived() +
                            " " + transaction.getCurrencyReceived(),
                    htmlContent
            );

            log.info("[EMAIL] Email transfert complété envoyé pour : {}",
                    transaction.getReference());

        } catch (Exception e) {
            log.error("[EMAIL] Échec email transfert complété {} : {}",
                    transaction.getReference(), e.getMessage());
        }
    }

    /**
     * Envoie l'email de notification d'échec de transfert.
     *
     * Déclenché dans TransferService.refundStripeAndFail()
     * quand la stratégie échoue ET que le remboursement Stripe
     * lui-même échoue (statut FAILED).
     *
     * Variables du template transfer-failed.html :
     * - senderName     : prénom + nom de l'expéditeur
     * - reference      : référence unique
     * - totalDebited   : montant remboursé
     * - currencySent   : devise
     * - failureReason  : raison de l'échec
     * - beneficiaryName: prénom + nom du bénéficiaire
     * - failedDate     : date et heure de l'échec
     *
     * @param user          l'expéditeur du transfert
     * @param transaction   la transaction échouée
     * @param failureReason la raison de l'échec
     */
    @Async
    public void sendTransferFailedEmail(
            User user, Transaction transaction, String failureReason) {
        try {
            log.error("[EMAIL] Envoi email transfert échoué pour : {}",
                    transaction.getReference());

            java.math.BigDecimal totalDebited = transaction.getAmountSent()
                    .add(transaction.getFees());

            String beneficiaryName = transaction.getBeneficiary().getFirstName()
                    + " " + transaction.getBeneficiary().getLastName();

            Context context = new Context();
            context.setVariable("senderName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("reference", transaction.getReference());
            context.setVariable("totalDebited", totalDebited);
            context.setVariable("currencySent", transaction.getCurrencySent());
            context.setVariable("failureReason", failureReason);
            context.setVariable("beneficiaryName", beneficiaryName);
            context.setVariable("failedDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));

            String htmlContent = templateEngine.process(
                    "emails/transfer-failed", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "❌ Transfert " + transaction.getReference() +
                            " échoué — Remboursement initié",
                    htmlContent
            );

            log.info("[EMAIL] Email transfert échoué envoyé pour : {}",
                    transaction.getReference());

        } catch (Exception e) {
            log.error("[EMAIL] Échec email transfert échoué {} : {}",
                    transaction.getReference(), e.getMessage());
        }
    }

    /**
     * Envoie l'email de confirmation de remboursement.
     *
     * Déclenché dans TransferService.refundStripeAndFail()
     * après que Stripe confirme le remboursement (statut REFUNDED).
     * Rassure l'utilisateur que son argent lui sera rendu.
     *
     * Variables du template transfer-refunded.html :
     * - senderName  : prénom + nom de l'expéditeur
     * - reference   : référence unique
     * - totalDebited: montant remboursé
     * - currencySent: devise
     * - stripeRef   : identifiant PaymentIntent Stripe
     * - refundDate  : date et heure du remboursement
     *
     * @param user        l'expéditeur du transfert
     * @param transaction la transaction remboursée
     */
    @Async
    public void sendTransferRefundedEmail(User user, Transaction transaction) {
        try {
            log.info("[EMAIL] Envoi email remboursement pour : {}",
                    transaction.getReference());

            java.math.BigDecimal totalDebited = transaction.getAmountSent()
                    .add(transaction.getFees());

            Context context = new Context();
            context.setVariable("senderName",
                    user.getFirstName() + " " + user.getLastName());
            context.setVariable("reference", transaction.getReference());
            context.setVariable("totalDebited", totalDebited);
            context.setVariable("currencySent", transaction.getCurrencySent());
            context.setVariable("stripeRef",
                    transaction.getStripePaymentIntentId());
            context.setVariable("refundDate",
                    LocalDateTime.now().format(EMAIL_DATE_FORMAT));

            String htmlContent = templateEngine.process(
                    "emails/transfer-refunded", context);

            sendHtmlEmail(
                    user.getEmail(),
                    "↩️ Remboursement " + transaction.getReference() +
                            " confirmé — " + totalDebited +
                            " " + transaction.getCurrencySent() + " en cours",
                    htmlContent
            );

            log.info("[EMAIL] Email remboursement envoyé pour : {}",
                    transaction.getReference());

        } catch (Exception e) {
            log.error("[EMAIL] Échec email remboursement {} : {}",
                    transaction.getReference(), e.getMessage());
        }
    }
}