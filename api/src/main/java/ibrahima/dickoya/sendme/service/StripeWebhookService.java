package ibrahima.dickoya.sendme.service;

import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import ibrahima.dickoya.sendme.enums.NotificationType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service traitant la logique métier des webhooks Stripe.
 *
 * Séparé du contrôleur pour respecter le principe de séparation
 * des responsabilités (SRP — SOLID).
 *
 * Chaque méthode correspond à un type d'événement Stripe
 * et met à jour la transaction en base + envoie une notification SSE.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    /**
     * Service email pour notifier l'utilisateur
     * des événements de paiement Stripe via webhook.
     */
    private final EmailService emailService;

    /**
     * Traite l'événement payment_intent.succeeded.
     * Envoie un email de confirmation de paiement Stripe.
     */
    @Transactional
    public void handlePaymentSucceeded(PaymentIntent paymentIntent) {

        log.info("[WEBHOOK] Paiement confirmé : {}", paymentIntent.getId());

        transactionRepository
                .findByStripePaymentIntentId(paymentIntent.getId())
                .ifPresent(transaction -> {

                    // Notification SSE déjà existante
                    notificationService.createAndSend(
                            transaction.getSender(),
                            String.format(
                                    "Paiement confirmé pour le transfert %s. " +
                                            "Envoi en cours vers %s %s...",
                                    transaction.getReference(),
                                    transaction.getBeneficiary().getFirstName(),
                                    transaction.getBeneficiary().getLastName()
                            ),
                            ibrahima.dickoya.sendme.enums.NotificationType
                                    .PAYMENT_CONFIRMED,
                            transaction
                    );

                    // Email de confirmation de paiement Stripe
                    emailService.sendPaymentConfirmedEmail(
                            transaction.getSender(), transaction);
                });
    }

    /**
     * Traite l'événement payment_intent.payment_failed.
     * Envoie un email d'alerte de refus de paiement.
     */
    @Transactional
    public void handlePaymentFailed(PaymentIntent paymentIntent) {

        log.error("[WEBHOOK] Paiement échoué : {}", paymentIntent.getId());

        transactionRepository
                .findByStripePaymentIntentId(paymentIntent.getId())
                .ifPresent(transaction -> {

                    // Mettre à jour le statut de la transaction
                    transaction.setStatus(
                            ibrahima.dickoya.sendme.enums.TransactionStatus.FAILED);
                    transaction.setStripePaymentStatus("payment_failed");
                    transactionRepository.save(transaction);

                    // Récupérer la raison du refus depuis Stripe
                    String failureReason = paymentIntent
                            .getLastPaymentError() != null
                            ? paymentIntent.getLastPaymentError().getMessage()
                            : "Paiement refusé par votre banque";

                    // Notification SSE déjà existante
                    notificationService.createAndSend(
                            transaction.getSender(),
                            String.format(
                                    "Paiement refusé pour le transfert %s. " +
                                            "Vérifiez vos informations de carte.",
                                    transaction.getReference()
                            ),
                            ibrahima.dickoya.sendme.enums.NotificationType
                                    .PAYMENT_FAILED,
                            transaction
                    );

                    // Email d'alerte paiement refusé
                    emailService.sendPaymentFailedEmail(
                            transaction.getSender(),
                            transaction,
                            failureReason);
                });
    }

    /**
     * Traite l'événement charge.refunded.
     *
     * Déclenché quand Stripe confirme qu'un remboursement a été effectué.
     * On met à jour le statut et on notifie l'utilisateur.
     *
     * @param charge l'objet Charge remboursé
     */
    @Transactional
    public void handleChargeRefunded(Charge charge) {

        log.info("[WEBHOOK] Remboursement confirmé pour charge : {}", charge.getId());

        // Retrouver la transaction via le PaymentIntent lié au Charge
        transactionRepository
                .findByStripePaymentIntentId(charge.getPaymentIntent())
                .ifPresent(transaction -> {

                    // Marquer comme remboursé
                    transaction.setStatus(TransactionStatus.REFUNDED);
                    transactionRepository.save(transaction);

                    // Notifier l'utilisateur
                    notificationService.createAndSend(
                            transaction.getSender(),
                            String.format(
                                    "Remboursement effectué pour le transfert %s. " +
                                            "Le montant sera recrédité sous 5-10 jours.",
                                    transaction.getReference()
                            ),
                            NotificationType.TRANSFER_REFUNDED,
                            transaction
                    );
                });
    }
}