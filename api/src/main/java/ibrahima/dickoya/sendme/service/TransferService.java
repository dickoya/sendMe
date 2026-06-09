package ibrahima.dickoya.sendme.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import ibrahima.dickoya.sendme.dto.request.SimulateRequest;
import ibrahima.dickoya.sendme.dto.request.TransferRequest;
import ibrahima.dickoya.sendme.dto.response.SimulateResponse;
import ibrahima.dickoya.sendme.dto.response.TransactionResponse;
import ibrahima.dickoya.sendme.entity.*;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.exception.TransferException;
import ibrahima.dickoya.sendme.repository.*;
import ibrahima.dickoya.sendme.strategy.TransferResult;
import ibrahima.dickoya.sendme.strategy.TransferStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service principal orchestrant le cycle de vie complet d'un transfert
 * avec intégration Stripe pour le paiement par carte bancaire.
 *
 * Flux complet :
 * 1. Vérification idempotence          (RG-06)
 * 2. Validation bénéficiaire
 * 3. Simulation frais + taux de change
 * 4. Débit carte via Stripe API        (RG-01 — débit immédiat)
 * 5. Création transaction en base      (statut PENDING)
 * 6. Exécution de la stratégie         (Strategy Pattern)
 * 7a. Succès → statut COMPLETED
 * 7b. Échec  → remboursement Stripe + statut FAILED (RG-04)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final TransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final SimulatorService simulatorService;
    private final StripePaymentService stripePaymentService;

    /**
     * Map des stratégies injectée par Spring IoC.
     * Clé : nom du canal (ex: "ORANGE_MONEY")
     * Valeur : instance de la stratégie correspondante
     */
    private final Map<String, TransferStrategy> strategyMap;

    /** Compteur atomique pour les références uniques. */
    private final AtomicInteger counter = new AtomicInteger(1);

    /**
     * Orchestre le transfert complet avec paiement Stripe.
     *
     * @Transactional garantit l'atomicité des opérations en base.
     * En cas d'exception, toutes les modifications BDD sont annulées.
     * Le remboursement Stripe est géré séparément (hors transaction BDD).
     *
     * @param request   données du transfert avec paymentMethodId Stripe
     * @param userEmail email de l'utilisateur connecté
     * @return TransactionResponse résultat complet de la transaction
     */
    @Transactional
    public TransactionResponse processTransfer(TransferRequest request, String userEmail) {

        log.info("=== DEBUT TRANSFERT pour {} ===", userEmail);

        // ── ÉTAPE 1 : Vérification idempotence ──────────────────────────────
        // Protège contre les doubles soumissions (double clic, retry réseau)
        var existing = transactionRepository
                .findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            log.warn("Transaction dupliquee detectee. Retour de l'existante.");
            return toResponse(existing.get());
        }

        // ── ÉTAPE 2 : Récupérer l'utilisateur et le bénéficiaire ────────────
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Beneficiary beneficiary = beneficiaryRepository
                .findById(request.getBeneficiaryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beneficiaire non trouve : " + request.getBeneficiaryId()));

        // Vérifier que le bénéficiaire appartient à l'utilisateur
        if (!beneficiary.getUser().getId().equals(user.getId())) {
            throw new TransferException("Ce beneficiaire ne vous appartient pas");
        }

        // ── ÉTAPE 3 : Calculer frais et taux de change ──────────────────────
        String currencyTo = getCurrencyForChannel(beneficiary.getChannel().name());

        SimulateResponse simulation = simulatorService.simulate(
                SimulateRequest.builder()
                        .amount(request.getAmount())
                        .currencyFrom(request.getCurrency())
                        .currencyTo(currencyTo)
                        .channel(beneficiary.getChannel())
                        .build()
        );

        // Montant total à débiter sur la carte = montant + frais
        // Exemple : 200 EUR + 3 EUR (1.5%) = 203 EUR
        var totalToDebit = simulation.getTotalDebited();

        // ── ÉTAPE 4 : Débiter la carte via Stripe ───────────────────────────
        log.info("[STRIPE] Debit de {} {} sur la carte", totalToDebit, request.getCurrency());

        PaymentIntent paymentIntent;
        try {
            paymentIntent = stripePaymentService.createAndConfirmPayment(
                    totalToDebit,
                    request.getPaymentMethodId(),
                    request.getIdempotencyKey()
            );
        } catch (StripeException e) {
            // Le paiement a été refusé par Stripe
            // (carte invalide, fonds insuffisants, etc.)
            log.error("[STRIPE] Paiement refuse : {}", e.getMessage());
            throw new TransferException(
                    "Paiement refuse par la banque : " + e.getMessage());
        }

        // Vérifier que Stripe a bien confirmé le paiement
        if (!stripePaymentService.isPaymentSucceeded(paymentIntent)) {
            log.error("[STRIPE] Statut inattendu : {}", paymentIntent.getStatus());
            throw new TransferException(
                    "Paiement non confirme par Stripe. Statut : " +
                            paymentIntent.getStatus());
        }

        log.info("[STRIPE] Carte debitee avec succes. PaymentIntent : {}",
                paymentIntent.getId());

        // ── ÉTAPE 5 : Créer la transaction en BDD (statut PENDING) ──────────
        Transaction transaction = Transaction.builder()
                .reference(generateReference())
                .amountSent(request.getAmount())
                .currencySent(request.getCurrency())
                .amountReceived(simulation.getAmountReceived())
                .currencyReceived(currencyTo)
                .exchangeRate(simulation.getExchangeRate())
                .fees(simulation.getFees())
                .status(TransactionStatus.PENDING)
                .channel(beneficiary.getChannel())
                .idempotencyKey(request.getIdempotencyKey())
                .stripePaymentIntentId(paymentIntent.getId())    // référence Stripe
                .stripePaymentStatus(paymentIntent.getStatus())  // statut Stripe
                .sender(user)
                .beneficiary(beneficiary)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction sauvegardee : {}", transaction.getReference());

        // ── ÉTAPE 6 : Passer à PROCESSING et exécuter la stratégie ──────────
        transaction.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        // Sélectionner la bonne stratégie via la Map (Strategy Pattern)
        String channelName = beneficiary.getChannel().name();
        TransferStrategy strategy = strategyMap.get(channelName);

        if (strategy == null) {
            // Rembourser Stripe car aucune stratégie disponible
            refundStripeAndFail(paymentIntent.getId(), transaction,
                    "Canal non supporte : " + channelName);
        }

        log.info("Execution de la strategie : {}", channelName);
        TransferResult result = strategy.execute(transaction, beneficiary);

        // ── ÉTAPE 7 : Traiter le résultat de la stratégie ───────────────────
        if (result.isSuccess()) {
            //  Transfert réussi → COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            log.info("=== TRANSFERT COMPLETE : {} ===", transaction.getReference());

        } else {
            //  Transfert échoué → Rembourser Stripe + statut FAILED (RG-04)
            log.error("Strategie echouee : {}. Remboursement Stripe en cours...",
                    result.getMessage());
            refundStripeAndFail(paymentIntent.getId(), transaction, result.getMessage());
        }

        return toResponse(transaction);
    }

    /**
     * Rembourse le paiement Stripe et marque la transaction comme FAILED.
     *
     * Implémente la règle RG-04 : si l'opérateur rejette le transfert,
     * le débit carte est automatiquement annulé/remboursé.
     *
     * @param paymentIntentId l'ID Stripe à rembourser
     * @param transaction     la transaction à marquer FAILED
     * @param reason          la raison de l'échec
     */
    private void refundStripeAndFail(
            String paymentIntentId,
            Transaction transaction,
            String reason) {

        try {
            // Tenter le remboursement Stripe
            stripePaymentService.refundPayment(paymentIntentId);
            log.info("[STRIPE] Remboursement effectue pour : {}", paymentIntentId);

            // Marquer la transaction comme REFUNDED
            transaction.setStatus(TransactionStatus.REFUNDED);
            transactionRepository.save(transaction);

        } catch (com.stripe.exception.StripeException e) {
            // Le remboursement Stripe a échoué — marquer comme FAILED
            log.error("[STRIPE] Echec du remboursement : {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
        }

        // Lancer l'exception pour informer l'utilisateur
        throw new TransferException(
                "Transfert echoue et carte remboursee : " + reason);
    }

    /**
     * Récupère l'historique paginé des transactions.
     *
     * @param userEmail email de l'utilisateur
     * @param pageable  paramètres de pagination
     * @return Page de TransactionResponse
     */
    public Page<TransactionResponse> getHistory(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        return transactionRepository
                .findBySenderIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    /**
     * Récupère le détail d'une transaction par ID.
     *
     * @param id        identifiant de la transaction
     * @param userEmail email de l'utilisateur connecté
     * @return TransactionResponse le détail
     */
    public TransactionResponse getById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction non trouvee : " + id));

        // Sécurité : vérifier la propriété de la transaction
        if (!transaction.getSender().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse a cette transaction");
        }

        return toResponse(transaction);
    }

    /**
     * Génère une référence unique de transaction (RG-05).
     * Format : TXN-YYYYMMDD-XXXX
     */
    private String generateReference() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("TXN-%s-%04d", date, counter.getAndIncrement());
    }

    /**
     * Détermine la devise de réception selon le canal.
     */
    private String getCurrencyForChannel(String channelName) {
        return switch (channelName) {
            case "BANK_TRANSFER" -> "EUR";
            default              -> "XOF";
        };
    }

    /**
     * Convertit une entité Transaction en DTO de réponse.
     */
    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .amountSent(transaction.getAmountSent())
                .currencySent(transaction.getCurrencySent())
                .amountReceived(transaction.getAmountReceived())
                .currencyReceived(transaction.getCurrencyReceived())
                .exchangeRate(transaction.getExchangeRate())
                .fees(transaction.getFees())
                .status(transaction.getStatus())
                .channel(transaction.getChannel())
                .stripePaymentIntentId(transaction.getStripePaymentIntentId())
                .stripePaymentStatus(transaction.getStripePaymentStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}