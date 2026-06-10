package ibrahima.dickoya.sendme.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import ibrahima.dickoya.sendme.dto.request.SimulateRequest;
import ibrahima.dickoya.sendme.dto.request.TransferRequest;
import ibrahima.dickoya.sendme.dto.response.SimulateResponse;
import ibrahima.dickoya.sendme.dto.response.TransactionResponse;
import ibrahima.dickoya.sendme.entity.*;
import ibrahima.dickoya.sendme.enums.NotificationType;
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
 * avec intégration Stripe et notifications SSE en temps réel.
 *
 * Flux complet :
 * 1. Vérification idempotence              (RG-06)
 * 2. Validation bénéficiaire
 * 3. Simulation frais + taux de change
 * 4. Débit carte via Stripe API            (RG-01)
 * 5. Création transaction PENDING + notification TRANSFER_INITIATED
 * 6. Exécution de la stratégie             (Strategy Pattern)
 * 7a. Succès → COMPLETED + notification TRANSFER_COMPLETED
 * 7b. Échec  → remboursement Stripe + notification TRANSFER_FAILED (RG-04)
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
     * NotificationService injecté pour envoyer les notifications SSE.
     * À chaque changement de statut de transaction, une notification
     * est créée en base ET poussée en temps réel via SSE.
     */
    private final NotificationService notificationService;

    /**
     * Map des stratégies injectée par Spring IoC.
     * Clé   : nom du canal (ex: "ORANGE_MONEY")
     * Valeur : instance de la stratégie correspondante
     */
    private final Map<String, TransferStrategy> strategyMap;

    /** Compteur atomique pour générer les références uniques (RG-05). */
    private final AtomicInteger counter = new AtomicInteger(1);

    /**
     * Orchestre le transfert complet avec paiement Stripe et notifications.
     *
     * @Transactional garantit l'atomicité des opérations en base.
     * En cas d'exception, toutes les modifications BDD sont annulées.
     * Le remboursement Stripe et les notifications sont gérés séparément.
     *
     * @param request   données du transfert avec paymentMethodId Stripe
     * @param userEmail email de l'utilisateur connecté (extrait du JWT)
     * @return TransactionResponse résultat complet de la transaction
     */
    @Transactional
    public TransactionResponse processTransfer(TransferRequest request, String userEmail) {

        log.info("=== DEBUT TRANSFERT pour {} ===", userEmail);

        // ── ÉTAPE 1 : Vérification idempotence ──────────────────────────────
        // Si une transaction avec cette clé existe déjà, on la retourne
        // sans re-débiter (protection double clic / retry réseau)
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

        // Vérifier que le bénéficiaire appartient bien à l'utilisateur connecté
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

        // Montant total à débiter = montant envoyé + frais plateforme
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
            // Paiement refusé par Stripe (carte invalide, fonds insuffisants...)
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

        // ── ÉTAPE 5 : Créer la transaction en BDD avec statut PENDING ───────
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
                .stripePaymentIntentId(paymentIntent.getId())
                .stripePaymentStatus(paymentIntent.getStatus())
                .sender(user)
                .beneficiary(beneficiary)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction sauvegardee : {}", transaction.getReference());

        // ── NOTIFICATION 1 : Transfert initié ───────────────────────────────
        // Envoyée dès que la transaction est créée et le paiement confirmé
        // L'utilisateur voit immédiatement que son transfert est en cours
        notificationService.createAndSend(
                user,
                String.format(
                        "Transfert %s initie — traitement en cours vers %s %s...",
                        transaction.getReference(),
                        beneficiary.getFirstName(),
                        beneficiary.getLastName()
                ),
                NotificationType.TRANSFER_INITIATED,
                transaction
        );

        // ── ÉTAPE 6 : Passer à PROCESSING et exécuter la stratégie ──────────
        transaction.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        // Sélectionner la bonne stratégie via la Map (Strategy Pattern)
        // Spring IoC a injecté toutes les stratégies automatiquement
        String channelName = beneficiary.getChannel().name();
        TransferStrategy strategy = strategyMap.get(channelName);

        if (strategy == null) {
            // Aucune stratégie trouvée pour ce canal — ne devrait jamais arriver
            refundStripeAndFail(
                    paymentIntent.getId(),
                    transaction,
                    user,
                    "Canal non supporte : " + channelName
            );
        }

        log.info("Execution de la strategie : {}", channelName);
        TransferResult result = strategy.execute(transaction, beneficiary);

        // ── ÉTAPE 7 : Traiter le résultat de la stratégie ───────────────────
        if (result.isSuccess()) {

            // ✅ Transfert réussi → statut COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            log.info("=== TRANSFERT COMPLETE : {} ===", transaction.getReference());

            // ── NOTIFICATION 2 : Transfert complété ─────────────────────────
            // L'utilisateur reçoit la confirmation avec le montant exact reçu
            notificationService.createAndSend(
                    user,
                    String.format(
                            "Transfert %s complete ! %s %s a recu %s %s.",
                            transaction.getReference(),
                            beneficiary.getFirstName(),
                            beneficiary.getLastName(),
                            transaction.getAmountReceived(),
                            transaction.getCurrencyReceived()
                    ),
                    NotificationType.TRANSFER_COMPLETED,
                    transaction
            );

        } else {

            //  Transfert échoué → Rembourser Stripe + notifier (RG-04)
            log.error("Strategie echouee : {}. Remboursement en cours...",
                    result.getMessage());

            // On passe l'utilisateur à refundStripeAndFail pour la notification
            refundStripeAndFail(
                    paymentIntent.getId(),
                    transaction,
                    user,
                    result.getMessage()
            );
        }

        return toResponse(transaction);
    }

    /**
     * Rembourse le paiement Stripe, met à jour le statut et notifie l'utilisateur.
     *
     * Implémente RG-04 : si l'opérateur rejette le transfert,
     * le débit carte est automatiquement annulé/remboursé ET
     * l'utilisateur est notifié en temps réel via SSE.
     *
     * Signature mise à jour pour inclure l'utilisateur
     * nécessaire à l'envoi de la notification.
     *
     * @param paymentIntentId l'ID Stripe à rembourser
     * @param transaction     la transaction à marquer REFUNDED ou FAILED
     * @param user            l'utilisateur à notifier
     * @param reason          la raison de l'échec (affichée dans la notification)
     */
    private void refundStripeAndFail(
            String paymentIntentId,
            Transaction transaction,
            User user,
            String reason) {

        try {
            // Demander le remboursement à Stripe
            stripePaymentService.refundPayment(paymentIntentId);
            log.info("[STRIPE] Remboursement effectue pour : {}", paymentIntentId);

            // Marquer la transaction comme REFUNDED
            transaction.setStatus(TransactionStatus.REFUNDED);
            transactionRepository.save(transaction);

            // ── NOTIFICATION 3A : Transfert échoué + remboursement confirmé ─
            // L'utilisateur est informé que sa carte sera remboursée
            notificationService.createAndSend(
                    user,
                    String.format(
                            "Transfert %s echoue. Votre carte sera remboursee sous 5-10 jours. Raison : %s",
                            transaction.getReference(),
                            reason
                    ),
                    NotificationType.TRANSFER_FAILED,
                    transaction
            );

        } catch (StripeException e) {
            // Le remboursement Stripe a lui-même échoué
            log.error("[STRIPE] Echec du remboursement : {}", e.getMessage());
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            // ── NOTIFICATION 3B : Transfert échoué sans remboursement ───────
            // Cas critique — l'équipe support doit intervenir manuellement
            notificationService.createAndSend(
                    user,
                    String.format(
                            "Transfert %s echoue. Contactez notre support pour le remboursement.",
                            transaction.getReference()
                    ),
                    NotificationType.TRANSFER_FAILED,
                    transaction
            );
        }

        // Lancer l'exception pour informer le contrôleur de l'échec
        throw new TransferException(
                "Transfert echoue et carte remboursee : " + reason);
    }

    /**
     * Récupère l'historique paginé des transactions de l'utilisateur.
     *
     * @param userEmail email de l'utilisateur connecté
     * @param pageable  paramètres de pagination (page, size, sort)
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
     * Récupère le détail d'une transaction par son identifiant.
     * Vérifie que la transaction appartient bien à l'utilisateur connecté.
     *
     * @param id        identifiant de la transaction
     * @param userEmail email de l'utilisateur connecté
     * @return TransactionResponse le détail de la transaction
     */
    public TransactionResponse getById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction non trouvee : " + id));

        // Sécurité : vérifier que la transaction appartient à l'utilisateur
        if (!transaction.getSender().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse a cette transaction");
        }

        return toResponse(transaction);
    }

    /**
     * Génère une référence unique et immuable pour chaque transaction (RG-05).
     * Format : TXN-YYYYMMDD-XXXX
     * Exemple : TXN-20250609-0001
     */
    private String generateReference() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("TXN-%s-%04d", date, counter.getAndIncrement());
    }

    /**
     * Détermine la devise de réception selon le canal choisi.
     * En production, cette logique prendrait en compte le pays du bénéficiaire.
     *
     * @param channelName le nom du canal (ex: "ORANGE_MONEY")
     * @return code devise ISO 4217
     */
    private String getCurrencyForChannel(String channelName) {
        return switch (channelName) {
            case "BANK_TRANSFER" -> "EUR";
            default              -> "XOF";
        };
    }

    /**
     * Récupère l'entité Transaction brute par son identifiant.
     * Utilisé par le contrôleur pour générer le PDF
     * (le service PDF a besoin de l'entité, pas du DTO).
     *
     * @param id        identifiant de la transaction
     * @param userEmail email de l'utilisateur connecté
     * @return Transaction l'entité complète
     */
    public Transaction getEntityById(Long id, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction non trouvee : " + id));

        if (!transaction.getSender().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse a cette transaction");
        }

        return transaction;
    }
    /**
     * Récupère une transaction par sa référence unique.
     * Utilisé pour générer le reçu PDF depuis la référence
     * affichée dans l'interface utilisateur.
     *
     * @param reference référence de la transaction (TXN-YYYYMMDD-XXXX)
     * @param userEmail email de l'utilisateur connecté
     * @return TransactionResponse le détail
     */
    public Transaction getEntityByReference(String reference, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Transaction transaction = transactionRepository
                .findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction non trouvee : " + reference));

        // Sécurité : vérifier que la transaction appartient à l'utilisateur
        if (!transaction.getSender().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse a cette transaction");
        }

        return transaction;
    }

    /**
     * Convertit une entité Transaction en DTO TransactionResponse.
     * Utilisé pour toutes les réponses HTTP du service.
     *
     * @param transaction l'entité à convertir
     * @return TransactionResponse le DTO formaté
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