package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * Contrôleur de test pour vérifier la configuration SMTP Brevo.
 * 
 * Endpoints :
 * - POST /api/test/email → envoie un email de test via la méthode @Async
 * - POST /api/test/email/diagnostic → envoie SYNC avec le message d'erreur
 * détaillé
 * 
 * À utiliser UNE SEULE FOIS pour valider que Brevo fonctionne.
 * Peut être supprimé après validation.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;
    private final JavaMailSender mailSender;

    /**
     * Adresse email de l'expéditeur, injectée depuis la configuration
     * (application-local.properties / variable d'environnement APP_MAIL_FROM).
     */
    @Value("${app.mail.from}")
    private String fromEmail;


    /**
     * Envoie un email de test pour vérifier la configuration SMTP Brevo.
     * 
     * @param request corps JSON contenant le champ "toEmail"
     * @return message de confirmation ou d'erreur
     */
    @PostMapping("/email")
    public ResponseEntity<Map<String, String>> sendTestEmail(
            @RequestBody Map<String, String> request) {

        String toEmail = request.get("toEmail");

        if (toEmail == null || toEmail.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'toEmail' est requis"));
        }

        try {
            emailService.sendTestEmail(toEmail);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "Email de test envoyé à " + toEmail + ". Vérifiez votre boîte de réception (ou les logs Brevo)."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Échec de l'envoi : " + e.getMessage()));
        }
    }

    /**
     * Diagnostic SYNCHRONE — envoie un email simple sans passer par @Async
     * pour capturer l'erreur SMTP réelle.
     */
    @PostMapping("/email/diagnostic")
    public ResponseEntity<Map<String, String>> diagnostic(
            @RequestBody Map<String, String> request) {

        String toEmail = request.get("toEmail");

        if (toEmail == null || toEmail.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'toEmail' est requis"));
        }

        try {
            // Construire et envoyer un email simple de manière synchrone
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "SendMe Diagnostic");

            helper.setTo(toEmail);
            helper.setSubject("🔧 SendMe — Test de diagnostic Brevo");
            helper.setText("<h1>Test Brevo</h1><p>Si vous voyez cet email, Brevo fonctionne correctement !</p>", true);

            // Envoi SYNCHRONE — bloquant, capture l'erreur réelle
            mailSender.send(message);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Email de diagnostic envoyé avec succès à " + toEmail));

        } catch (Exception e) {
            // Message d'erreur détaillé pour le diagnostic
            String errorMsg = e.getMessage();
            String causeMsg = (e.getCause() != null) ? " | Cause: " + e.getCause().getMessage() : "";

            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "❌ Échec de l'envoi",
                            "details", errorMsg + causeMsg,
                            "exceptionType", e.getClass().getName()));
        }
    }
}
