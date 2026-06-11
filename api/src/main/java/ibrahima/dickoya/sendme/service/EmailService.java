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
}