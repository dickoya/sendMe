package ibrahima.dickoya.sendme.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import ibrahima.dickoya.sendme.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur recevant les événements Stripe via Webhook.
 *
 * Stripe appelle cet endpoint pour notifier notre backend
 * des événements de paiement (confirmation, échec, remboursement).
 *
 * Sécurité :
 * Chaque requête Stripe est signée avec notre webhook secret.
 * On vérifie cette signature AVANT de traiter l'événement
 * pour éviter les faux événements malveillants.
 *
 * Événements traités :
 * - payment_intent.succeeded    → paiement confirmé
 * - payment_intent.failed       → paiement échoué
 * - charge.refunded             → remboursement confirmé
 *
 * IMPORTANT : Cette route doit être exclue de Spring Security
 * car Stripe n'envoie pas de JWT. La sécurité est assurée
 * par la vérification de signature.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    /** Service qui traite la logique métier des webhooks. */
    private final StripeWebhookService stripeWebhookService;

    /**
     * Secret du webhook Stripe — injecté depuis application.properties.
     * Utilisé pour vérifier l'authenticité de chaque requête Stripe.
     */
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Reçoit et traite les événements Stripe.
     *
     * @param payload   le corps brut de la requête (JSON Stripe)
     * @param sigHeader l'en-tête de signature Stripe (Stripe-Signature)
     * @return 200 OK si traité, 400 si signature invalide
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Vérifier la signature Stripe pour authentifier la requête
            // Cette étape est CRITIQUE pour la sécurité
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("[WEBHOOK] Événement Stripe reçu : {}", event.getType());

        } catch (SignatureVerificationException e) {
            // Signature invalide — possible tentative de fraude
            log.error("[WEBHOOK] Signature invalide : {}", e.getMessage());
            return ResponseEntity.badRequest().body("Signature invalide");
        }

        // Traiter l'événement selon son type
        switch (event.getType()) {

            case "payment_intent.succeeded":
                // Paiement confirmé par Stripe
                PaymentIntent succeededIntent = (PaymentIntent)
                        event.getDataObjectDeserializer()
                                .getObject().orElseThrow();
                stripeWebhookService.handlePaymentSucceeded(succeededIntent);
                break;

            case "payment_intent.payment_failed":
                // Paiement échoué côté Stripe
                PaymentIntent failedIntent = (PaymentIntent)
                        event.getDataObjectDeserializer()
                                .getObject().orElseThrow();
                stripeWebhookService.handlePaymentFailed(failedIntent);
                break;

            case "charge.refunded":
                // Remboursement confirmé
                Charge charge = (Charge)
                        event.getDataObjectDeserializer()
                                .getObject().orElseThrow();
                stripeWebhookService.handleChargeRefunded(charge);
                break;

            default:
                // Événement non géré — on ignore
                log.debug("[WEBHOOK] Événement ignoré : {}", event.getType());
        }

        // Toujours retourner 200 pour que Stripe ne réessaie pas
        return ResponseEntity.ok("Webhook traité");
    }
}