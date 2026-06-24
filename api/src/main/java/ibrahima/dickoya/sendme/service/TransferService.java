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
import ibrahima.dickoya.sendme.utils.CurrencyByCountry;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ibrahima.dickoya.sendme.enums.NotificationType.TRANSFER_FAILED;

/**
 * Service principal orchestrant le cycle de vie complet d'un transfert
 * avec intégration Stripe et notifications SSE en temps réel.
 *
 * Flux complet :
 * 1. Vérification idempotence (RG-06)
 * 2. Validation bénéficiaire
 * 3. Simulation frais + taux de change
 * 4. Débit carte via Stripe API (RG-01)
 * 5. Création transaction PENDING + notification TRANSFER_INITIATED
 * 6. Exécution de la stratégie (Strategy Pattern)
 * 7a. Succès → COMPLETED + notification TRANSFER_COMPLETED
 * 7b. Échec → remboursement Stripe + notification TRANSFER_FAILED (RG-04)
 */
@Service
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
         * Clé : nom du canal (ex: "ORANGE_MONEY")
         * Valeur : instance de la stratégie correspondante
         */
        private final Map<String, TransferStrategy> strategyMap;

        /** Compteur atomique pour générer les références uniques (RG-05). */
        private final AtomicInteger counter = new AtomicInteger(1);

        /**
         * Initialise le compteur de références à partir de la base de données.
         * Évite les conflits de duplication après un redémarrage du serveur.
         * Exécuté automatiquement après l'injection des dépendances.
         */
        @PostConstruct
        public void initCounter() {
                Long maxSeq = transactionRepository.findMaxReferenceSequence();
                if (maxSeq != null) {
                        counter.set(maxSeq.intValue() + 1);
                        log.info("Compteur de references initialise a {}", counter.get());
                } else {
                        log.info("Aucune transaction existante, compteur demarre a 1");
                }
        }

        /**
         * Service email pour notifier l'utilisateur
         * à chaque étape du cycle de vie du transfert.
         */
        private final EmailService emailService;

        /**
         * Service de parrainage pour générer les coupons de réduction
         * lors du premier transfert d'un filleul.
         */
        private final ReferralService referralService;

        /**
         * Service de persistance des transactions en échec.
         * Séparé de ce service pour éviter le problème de self-invocation
         * des proxies AOP Spring avec @Transactional(REQUIRES_NEW).
         */
        private final TransactionPersistenceService transactionPersistenceService;

        /**
         * TransactionTemplate pour gérer les transactions programmatiquement.
         * Évite les deadlocks MySQL causés par REQUIRES_NEW avec une transaction
         * parente qui détient des locks (gap locks InnoDB).
         */
        private final TransactionTemplate transactionTemplate;

        /**
         * Constructeur pour initialiser TransactionTemplate avec le transaction
         * manager.
         */
        public TransferService(TransactionRepository transactionRepository,
                        BeneficiaryRepository beneficiaryRepository,
                        UserRepository userRepository,
                        SimulatorService simulatorService,
                        StripePaymentService stripePaymentService,
                        NotificationService notificationService,
                        Map<String, TransferStrategy> strategyMap,
                        EmailService emailService,
                        ReferralService referralService,
                        TransactionPersistenceService transactionPersistenceService,
                        PlatformTransactionManager transactionManager) {
                this.transactionRepository = transactionRepository;
                this.beneficiaryRepository = beneficiaryRepository;
                this.userRepository = userRepository;
                this.simulatorService = simulatorService;
                this.stripePaymentService = stripePaymentService;
                this.notificationService = notificationService;
                this.strategyMap = strategyMap;
                this.emailService = emailService;
                this.referralService = referralService;
                this.transactionPersistenceService = transactionPersistenceService;
                this.transactionTemplate = new TransactionTemplate(transactionManager);
        }

        /**
         * Orchestre le transfert complet avec paiement Stripe et notifications.
         *
         * Les opérations BDD sont gérées via TransactionTemplate pour éviter
         * les deadlocks MySQL. Les échecs Stripe sont persistés dans une
         * transaction séparée (REQUIRES_NEW) sans conflit de lock.
         *
         * @param request   données du transfert avec paymentMethodId Stripe
         * @param userEmail email de l'utilisateur connecté (extrait du JWT)
         * @return TransactionResponse résultat complet de la transaction
         */
        public TransactionResponse processTransfer(TransferRequest request, String userEmail) {

                /**
                 * Service email pour notifier l'utilisateur
                 * à chaque étape du cycle de vie du transfert.
                 */
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

                // ── ÉTAPE 3 : Déterminer le canal de réception ─────────────────────
                // Si un channelId est fourni, on utilise ce canal spécifique.
                // Sinon, on utilise le canal principal (isPrimary=true) ou le premier.
                BeneficiaryChannel selectedChannel;
                if (request.getChannelId() != null) {
                        selectedChannel = beneficiary.getChannels().stream()
                                        .filter(ch -> ch.getId().equals(request.getChannelId()))
                                        .findFirst()
                                        .orElseThrow(() -> new TransferException(
                                                        "Canal de reception non trouve pour ce beneficiaire"));
                } else {
                        selectedChannel = beneficiary.getChannels().stream()
                                        .filter(BeneficiaryChannel::getIsPrimary)
                                        .findFirst()
                                        .orElse(beneficiary.getChannels().get(0));
                }

                String currencyTo = getCurrencyForChannel(selectedChannel.getChannel().name());

                // La simulation prend en compte l'ID utilisateur pour appliquer
                // automatiquement la réduction de parrainage si un coupon valide existe
                SimulateResponse simulation = simulatorService.simulate(
                                SimulateRequest.builder()
                                                .amount(request.getAmount())
                                                .currencyFrom(request.getCurrency())
                                                .currencyTo(currencyTo)
                                                .channel(selectedChannel.getChannel())
                                                .build(),
                                user.getId());

                // Montant total à débiter = montant envoyé + frais - réduction (si applicable)
                // Exemple : 200 EUR + 3 EUR (1.5%) - 5€ (réduction) = 198 EUR
                var totalToDebit = simulation.getTotalDebited();

                // ── ÉTAPE 4 : Créer la transaction en BDD avec statut PENDING ───────
                // Créée AVANT le débit Stripe pour que même les échecs soient tracés
                Transaction transaction = Transaction.builder()
                                .reference(generateReference())
                                .amountSent(request.getAmount())
                                .currencySent(request.getCurrency())
                                .amountReceived(simulation.getAmountReceived())
                                .currencyReceived(currencyTo)
                                .exchangeRate(simulation.getExchangeRate())
                                .fees(simulation.getFees())
                                .discount(simulation.getDiscount() != null ? simulation.getDiscount() : BigDecimal.ZERO)
                                .status(TransactionStatus.PENDING)
                                .channel(selectedChannel.getChannel())
                                .idempotencyKey(request.getIdempotencyKey())
                                .sender(user)
                                .beneficiary(beneficiary)
                                .build();

                transaction = transactionRepository.save(transaction);
                log.info("Transaction sauvegardee : {}", transaction.getReference());

                // ── ÉTAPE 5 : Débiter la carte via Stripe ───────────────────────────
                log.info("[STRIPE] Debit de {} {} sur la carte", totalToDebit, request.getCurrency());

                PaymentIntent paymentIntent;
                try {
                        paymentIntent = stripePaymentService.createAndConfirmPayment(
                                        totalToDebit,
                                        request.getPaymentMethodId(),
                                        request.getIdempotencyKey());
                } catch (StripeException e) {
                        // Paiement refusé par Stripe (carte invalide, fonds insuffisants...)
                        log.error("[STRIPE] Paiement refuse : {}", e.getMessage());
                        // Marquer la transaction comme FAILED dans une transaction séparée
                        // pour éviter le rollback de @Transactional
                        // La méthode retourne la nouvelle entité FAILED persistée
                        Transaction failedTransaction = transactionPersistenceService
                                        .saveFailedTransaction(transaction, "failed", e.getMessage());

                        // Notifier l'utilisateur
                        notificationService.createAndSend(
                                        user,
                                        String.format(
                                                        "Transfert %s echoue — paiement refuse par la banque : %s",
                                                        transaction.getReference(),
                                                        e.getMessage()),
                                        NotificationType.TRANSFER_FAILED,
                                        failedTransaction);
                        emailService.sendTransferFailedEmail(user, failedTransaction, e.getMessage());
                        return toResponse(failedTransaction);
                }

                // Vérifier que Stripe a bien confirmé le paiement
                if (!stripePaymentService.isPaymentSucceeded(paymentIntent)) {
                        log.error("[STRIPE] Statut inattendu : {}", paymentIntent.getStatus());
                        // Marquer la transaction comme FAILED dans une transaction séparée
                        Transaction failedTransaction = transactionPersistenceService
                                        .saveFailedTransaction(transaction, paymentIntent.getId(),
                                                        paymentIntent.getStatus());

                        // Notifier l'utilisateur
                        notificationService.createAndSend(
                                        user,
                                        String.format(
                                                        "Transfert %s echoue — statut Stripe inattendu : %s",
                                                        transaction.getReference(),
                                                        paymentIntent.getStatus()),
                                        NotificationType.TRANSFER_FAILED,
                                        failedTransaction);
                        emailService.sendTransferFailedEmail(user, failedTransaction,
                                        "Paiement non confirme par Stripe. Statut : " + paymentIntent.getStatus());
                        return toResponse(failedTransaction);

                }

                log.info("[STRIPE] Carte debitee avec succes. PaymentIntent : {}",
                                paymentIntent.getId());

                // Mettre à jour la transaction avec les infos Stripe
                transaction.setStripePaymentIntentId(paymentIntent.getId());
                transaction.setStripePaymentStatus(paymentIntent.getStatus());
                transactionRepository.save(transaction);

                // ── NOTIFICATION 1 : Transfert initié ───────────────────────────────
                // Envoyée dès que la transaction est créée et le paiement confirmé
                // L'utilisateur voit immédiatement que son transfert est en cours
                notificationService.createAndSend(
                                user,
                                String.format(
                                                "Transfert %s initie — traitement en cours vers %s %s...",
                                                transaction.getReference(),
                                                beneficiary.getFirstName(),
                                                beneficiary.getLastName()),
                                NotificationType.TRANSFER_INITIATED,
                                transaction);
                // Email de confirmation d'initiation en arrière-plan
                emailService.sendTransferInitiatedEmail(user, transaction);

                // ── ÉTAPE 6 : Passer à PROCESSING et exécuter la stratégie ──────────
                transaction.setStatus(TransactionStatus.PROCESSING);
                transactionRepository.save(transaction);

                // Sélectionner la bonne stratégie via la Map (Strategy Pattern)
                // Spring IoC a injecté toutes les stratégies automatiquement
                // On utilise le canal sélectionné (pas forcément le canal par défaut)
                String channelName = selectedChannel.getChannel().name();
                TransferStrategy strategy = strategyMap.get(channelName);

                if (strategy == null) {
                        // Aucune stratégie trouvée pour ce canal — ne devrait jamais arriver
                        return refundStripeAndFail(
                                        paymentIntent.getId(),
                                        transaction,
                                        user,
                                        "Canal non supporte : " + channelName);
                }

                log.info("Execution de la strategie : {} pour le canal {}", channelName, selectedChannel.getId());
                TransferResult result = strategy.execute(transaction, beneficiary);

                // ── ÉTAPE 7 : Traiter le résultat de la stratégie ───────────────────
                if (result.isSuccess()) {

                        // Transfert réussi → statut COMPLETED
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
                                                        transaction.getCurrencyReceived()),
                                        NotificationType.TRANSFER_COMPLETED,
                                        transaction);
                        // Email de confirmation de complétion en arrière-plan
                        emailService.sendTransferCompletedEmail(user, transaction);

                        // ── PARRAINAGE : Marquer le coupon comme utilisé ──────────────
                        // Si une réduction a été appliquée via un coupon, on le marque utilisé
                        useReferralCouponIfApplicable(user, request.getCurrency(), transaction, simulation);

                        // ── PARRAINAGE : Récompenser le premier transfert du filleul ──
                        // Si l'utilisateur a été parrainé, des coupons de réduction
                        // sont générés pour lui et son parrain
                        try {
                                referralService.rewardFirstTransfer(user, transaction);
                        } catch (Exception e) {
                                log.error("[PARRAINAGE] Erreur lors de la récompense : {}", e.getMessage());
                        }

                } else {

                        // Transfert échoué → Rembourser Stripe + notifier (RG-04)
                        log.error("Strategie echouee : {}. Remboursement en cours...",
                                        result.getMessage());

                        // On passe l'utilisateur à refundStripeAndFail pour la notification
                        return refundStripeAndFail(
                                        paymentIntent.getId(),
                                        transaction,
                                        user,
                                        result.getMessage());
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
         * @param paymentIntentId l'ID Stripe à rembourser
         * @param transaction     la transaction à marquer REFUNDED ou FAILED
         * @param user            l'utilisateur à notifier
         * @param reason          la raison de l'échec (affichée dans la notification)
         * @return TransactionResponse la transaction mise à jour
         */
        private TransactionResponse refundStripeAndFail(
                        String paymentIntentId,
                        Transaction transaction,
                        User user,
                        String reason) {

                try {
                        stripePaymentService.refundPayment(paymentIntentId);

                        transaction.setStatus(TransactionStatus.REFUNDED);
                        transactionRepository.save(transaction);

                        // Notification SSE : remboursement réussi
                        notificationService.createAndSend(
                                        user,
                                        String.format(
                                                        "Transfert %s echoue : %s. Le montant de %s %s a ete rembourse sur votre carte.",
                                                        transaction.getReference(),
                                                        reason,
                                                        transaction.getAmountSent().add(transaction.getFees()),
                                                        transaction.getCurrencySent()),
                                        TRANSFER_FAILED,
                                        transaction);

                        // Email remboursement confirmé
                        emailService.sendTransferRefundedEmail(user, transaction);

                } catch (com.stripe.exception.StripeException e) {
                        transaction.setStatus(TransactionStatus.FAILED);
                        transactionRepository.save(transaction);

                        // Notification SSE : échec du remboursement
                        notificationService.createAndSend(
                                        user,
                                        String.format(
                                                        "Transfert %s echoue : %s. Attention : le remboursement automatique a echoue. Veuillez contacter le support.",
                                                        transaction.getReference(),
                                                        reason),
                                        TRANSFER_FAILED,
                                        transaction);

                        // Email transfert échoué sans remboursement
                        emailService.sendTransferFailedEmail(user, transaction, reason);
                }

                return toResponse(transaction);
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
         * Utilise CurrencyByCountry pour un mapping flexible et évolutif.
         *
         * @param channelName le nom du canal (ex: "ORANGE_MONEY")
         * @return code devise ISO 4217
         */
        private String getCurrencyForChannel(String channelName) {
                return CurrencyByCountry.getDefaultCurrencyForChannel(channelName);
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
         * Utilise le coupon de réduction de parrainage si une réduction a été appliquée
         * lors de la simulation du transfert.
         *
         * @param user        l'utilisateur qui a bénéficié de la réduction
         * @param currency    la devise du transfert
         * @param transaction la transaction associée
         * @param simulation  la simulation contenant le montant de la réduction
         */
        private void useReferralCouponIfApplicable(User user, String currency, Transaction transaction,
                        SimulateResponse simulation) {
                BigDecimal discount = simulation.getDiscount();
                if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                        try {
                                referralService.getValidCoupon(user.getId(), currency)
                                                .ifPresent(coupon -> {
                                                        referralService.useCoupon(coupon.getId(), transaction);
                                                });
                        } catch (Exception e) {
                                log.error("[PARRAINAGE] Erreur lors de l'utilisation du coupon : {}",
                                                e.getMessage());
                        }
                }
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