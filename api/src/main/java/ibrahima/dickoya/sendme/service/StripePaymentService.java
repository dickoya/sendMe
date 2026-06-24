package ibrahima.dickoya.sendme.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import ibrahima.dickoya.sendme.config.CurrencyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service gérant toutes les interactions avec l'API Stripe.
 *
 * Responsabilités :
 * 1. Créer un PaymentIntent (réservation du paiement)
 * 2. Confirmer le PaymentIntent (débit effectif de la carte)
 * 3. Rembourser un paiement en cas d'échec du transfert (RG-04)
 *
 * Stripe utilise les centimes pour les montants.
 * Exemple : 200 EUR = 20000 centimes dans l'API Stripe.
 *
 * En mode sandbox (sk_test_...) :
 * - Aucun vrai débit n'est effectué
 * - Cartes de test disponibles sur https://stripe.com/docs/testing
 */
@Service
@Slf4j
public class StripePaymentService {

        /**
         * Devise par défaut pour les paiements Stripe.
         * Injectée depuis application.properties.
         */
        @Value("${stripe.currency}")
        private String defaultCurrency;

        /**
         * URL de retour après paiement Stripe (pour 3D Secure).
         * Configurable via application.properties.
         * Exemple dev : http://localhost:3000/payment/callback
         * Exemple prod : https://sendme.com/payment/callback
         */
        @Value("${stripe.return-url:http://localhost:3000/payment/callback}")
        private String returnUrl;

        /**
         * Crée et confirme un PaymentIntent Stripe en une seule étape.
         *
         * Un PaymentIntent représente une tentative de collecte de paiement.
         * Il trace le cycle de vie complet : création → confirmation → succès/échec.
         *
         * Le montant total débité = montant envoyé + frais de la plateforme.
         *
         * @param amount          montant total à débiter
         * @param paymentMethodId identifiant du moyen de paiement (généré par
         *                        Stripe.js)
         * @param idempotencyKey  clé unique pour éviter les doubles débits
         * @return PaymentIntent l'objet Stripe avec l'ID et le statut
         * @throws StripeException si le paiement est refusé ou invalide
         */
        public PaymentIntent createAndConfirmPayment(
                        BigDecimal amount,
                        String paymentMethodId,
                        String idempotencyKey) throws StripeException {
                return createAndConfirmPayment(amount, paymentMethodId, idempotencyKey, defaultCurrency);
        }

        /**
         * Crée et confirme un PaymentIntent Stripe en une seule étape,
         * avec une devise spécifiée dynamiquement.
         *
         * @param amount          montant total à débiter
         * @param paymentMethodId identifiant du moyen de paiement (généré par
         *                        Stripe.js)
         * @param idempotencyKey  clé unique pour éviter les doubles débits
         * @param currency        code devise ISO 4217 (ex: EUR, USD, GBP)
         * @return PaymentIntent l'objet Stripe avec l'ID et le statut
         * @throws StripeException si le paiement est refusé ou invalide
         */
        public PaymentIntent createAndConfirmPayment(
                        BigDecimal amount,
                        String paymentMethodId,
                        String idempotencyKey,
                        String currency) throws StripeException {

                // Stripe travaille en centimes — convertir selon le nombre de décimales de la
                // devise
                // Exemple : 203.00 EUR → 20300 centimes (2 décimales)
                // Exemple : 1000 JPY → 1000 centimes (0 décimale)
                int decimals = CurrencyConfig.getDecimals(currency);
                BigDecimal multiplier = BigDecimal.TEN.pow(decimals);
                long amountInCents = amount.multiply(multiplier)
                                .setScale(0, RoundingMode.HALF_UP)
                                .longValue();

                log.info("[STRIPE] Creation du PaymentIntent : {} {} ({} centimes), methode : {}",
                                amount, currency, amountInCents, paymentMethodId);

                // Construire les paramètres du PaymentIntent
                PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                                // Montant en centimes
                                .setAmount(amountInCents)
                                // Devise (dynamique selon la sélection utilisateur)
                                .setCurrency(currency.toLowerCase())
                                // Moyen de paiement tokenisé par Stripe.js
                                .setPaymentMethod(paymentMethodId)
                                // Confirmer immédiatement sans étape supplémentaire
                                .setConfirm(true)
                                // Description visible dans le dashboard Stripe
                                .setDescription("SendMe - Transfert d'argent international")
                                // URL de retour configurée via application.properties
                                .setReturnUrl(returnUrl)
                                // Metadata utiles pour le support client
                                .putMetadata("idempotency_key", idempotencyKey)
                                .putMetadata("platform", "sendme")
                                .build();

                // Appel à l'API Stripe avec clé d'idempotence pour éviter les doublons
                PaymentIntent paymentIntent = PaymentIntent.create(
                                createParams,
                                com.stripe.net.RequestOptions.builder()
                                                .setIdempotencyKey(idempotencyKey)
                                                .build());

                log.info("[STRIPE] PaymentIntent cree : id={}, statut={}, devise={}",
                                paymentIntent.getId(), paymentIntent.getStatus(), currency);

                return paymentIntent;
        }

        /**
         * Rembourse un paiement Stripe en cas d'échec du transfert (RG-04).
         *
         * Appelé automatiquement par le TransferService si la stratégie
         * de transfert (Orange Money, banque, etc.) échoue après que
         * la carte a été débitée.
         *
         * En mode sandbox, le remboursement est instantané.
         * En production, il peut prendre 5 à 10 jours ouvrés.
         *
         * @param paymentIntentId l'ID du PaymentIntent à rembourser
         * @return Refund l'objet remboursement Stripe
         * @throws StripeException si le remboursement échoue
         */
        public Refund refundPayment(String paymentIntentId) throws StripeException {

                log.info("[STRIPE] Remboursement du PaymentIntent : {}", paymentIntentId);

                // Construire la demande de remboursement
                // REQUESTED_BY_CUSTOMER est approprié car le remboursement fait suite
                // à un échec de transfert (opérateur mobile/banque), pas une fraude
                RefundCreateParams refundParams = RefundCreateParams.builder()
                                .setPaymentIntent(paymentIntentId) // référence le paiement original
                                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                                .build();

                Refund refund = Refund.create(refundParams);

                log.info("[STRIPE] Remboursement effectue : id={}, statut={}",
                                refund.getId(), refund.getStatus());

                return refund;
        }

        /**
         * Vérifie si un PaymentIntent Stripe a bien réussi.
         *
         * @param paymentIntent le PaymentIntent à vérifier
         * @return true si le statut est "succeeded"
         */
        public boolean isPaymentSucceeded(PaymentIntent paymentIntent) {
                return "succeeded".equals(paymentIntent.getStatus());
        }
}