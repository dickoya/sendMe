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
import ibrahima.dickoya.sendme.exception.MissingStrategyException;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.exception.TransferException;
import ibrahima.dickoya.sendme.repository.*;
import ibrahima.dickoya.sendme.strategy.TransferResult;
import ibrahima.dickoya.sendme.strategy.TransferStrategy;
import ibrahima.dickoya.sendme.utils.CurrencyByCountry;
import ibrahima.dickoya.sendme.utils.MessageUtils;
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
 * 4. Débit carte via Stripe API (RG-01) — HORS transaction BDD
 * 5. Création transaction PENDING + notification TRANSFER_INITIATED
 * 6. Exécution de la stratégie (Strategy Pattern)
 * 7a. Succès → COMPLETED + notification TRANSFER_COMPLETED
 * 7b. Échec → remboursement Stripe + notification TRANSFER_FAILED (RG-04)
 *
 * ⚠️ Les appels Stripe sont effectués EN DEHORS des transactions BDD
 * pour éviter les deadlocks MySQL (gap locks InnoDB) causés par
 * des transactions longues contenant des appels réseau.
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
         * Utilitaire de messages internationalisés.
         * Remplace les chaînes en dur par des messages externalisés
         * dans messages_fr.properties / messages_en.properties.
         */
        private final MessageUtils messageUtils;

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
                        PlatformTransactionManager transactionManager,
                        MessageUtils messageUtils) {
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
                this.messageUtils = messageUtils;
        }

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
         * Vérifie au démarrage que tous les types de canaux (ChannelType) ont
         * une stratégie de transfert associée dans la map.
         * 
         * Si un canal est ajouté en base de données mais qu'aucune stratégie
         * n'a été implémentée, l'application log une erreur au démarrage
         * pour éviter des échecs silencieux à l'exécution.
         */
        @PostConstruct
        public void validateStrategies() {
                for (ibrahima.dickoya.sendme.enums.ChannelType channelType : ibrahima.dickoya.sendme.enums.ChannelType
                                .values()) {
                        String channelName = channelType.name();
                        if (!strategyMap.containsKey(channelName)) {
                                throw new MissingStrategyException(
                                                "Aucune stratégie trouvée pour le canal : " + channelName +
                                                                ". Ajoutez une implémentation de TransferStrategy " +
                                                                "avec @Component(\"" + channelName + "\")");
                        }
                        log.debug("Strategie trouvee pour le canal : {}", channelName);
                }
        }

        /**
         * Orchestre le transfert complet avec paiement Stripe et notifications.
         *
         * ⚠️ PAS de @Transactional sur cette méthode :
         * Les appels Stripe (réseau) sont effectués EN DEHORS des transactions BDD.
         * Les opérations BDD sont gérées via TransactionTemplate programmatique
         * pour éviter les deadlocks MySQL (gap locks InnoDB).
         *
         * @param request   données du transfert avec paymentMethodId Stripe
         * @param userEmail email de l'utilisateur connecté (extrait du JWT)
         * @return TransactionResponse résultat complet de la transaction
         */
        public TransactionResponse processTransfer(TransferRequest request, String userEmail) {

                log.info("=== DEBUT TRANSFERT pour {} ===", userEmail);

                // ── ÉTAPE 1 : Vérification idempotence ──────────────────────────────
                var existing = checkIdempotency(request.getIdempotencyKey());
                if (existing != null) {
                        return existing;
                }

                // ── ÉTAPE 2 : Récupérer l'utilisateur et le bénéficiaire ────────────
                User user = loadUser(userEmail);
                Beneficiary beneficiary = loadBeneficiary(request.getBeneficiaryId(), user);

                // ── ÉTAPE 3 : Déterminer le canal et simuler le transfert ───────────
                BeneficiaryChannel selectedChannel = selectChannel(beneficiary, request.getChannelId());
                SimulateResponse simulation = simulateTransfer(user, selectedChannel, request);
                var totalToDebit = simulation.getTotalDebited();

                // ── ÉTAPE 4 : Créer la transaction en BDD avec statut PENDING ───────
                Transaction transaction = createPendingTransaction(request, user, beneficiary, selectedChannel,
                                simulation);

                // ── ÉTAPE 5 : Débiter la carte via Stripe ───────────────────────────
                PaymentIntent paymentIntent = debitCard(totalToDebit, request, transaction, user);
                if (paymentIntent == null) {
                        // Le paiement a échoué, la transaction FAILED a déjà été retournée
                        return null; // Ne devrait pas arriver car debitCard retourne via return
                }

                // ── NOTIFICATION 1 : Transfert initié ───────────────────────────────
                notifyTransferInitiated(transaction, user, beneficiary);

                // ── ÉTAPE 6 : Passer à PROCESSING et exécuter la stratégie ──────────
                transaction.setStatus(TransactionStatus.PROCESSING);
                transactionRepository.save(transaction);

                TransferStrategy strategy = resolveStrategy(selectedChannel);
                if (strategy == null) {
                        return refundStripeAndFail(
                                        paymentIntent.getId(),
                                        transaction,
                                        user,
                                        messageUtils.get("error.transfer.channel.unsupported",
                                                        selectedChannel.getChannel().name()));
                }

                log.info("Execution de la strategie : {} pour le canal {}",
                                selectedChannel.getChannel().name(), selectedChannel.getId());
                TransferResult result = strategy.execute(transaction, beneficiary);

                // ── ÉTAPE 7 : Traiter le résultat de la stratégie ───────────────────
                if (result.isSuccess()) {
                        return handleSuccessfulStrategy(transaction, user, simulation, request);
                } else {
                        log.error("Strategie echouee : {}. Remboursement en cours...", result.getMessage());
                        return refundStripeAndFail(paymentIntent.getId(), transaction, user, result.getMessage());
                }
        }

        // ── Méthodes privées extraites de processTransfer ──────────────────────

        /**
         * Vérifie l'idempotence : si une transaction avec cette clé existe déjà,
         * retourne la réponse correspondante sans re-débiter.
         *
         * @param idempotencyKey la clé d'idempotence à vérifier
         * @return TransactionResponse si la transaction existe, null sinon
         */
        private TransactionResponse checkIdempotency(String idempotencyKey) {
                var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
                if (existing.isPresent()) {
                        log.warn("Transaction dupliquee detectee. Retour de l'existante.");
                        return toResponse(existing.get());
                }
                return null;
        }

        /**
         * Charge l'utilisateur par email.
         *
         * @param userEmail l'email de l'utilisateur
         * @return l'entité User
         * @throws ResourceNotFoundException si l'utilisateur n'existe pas
         */
        private User loadUser(String userEmail) {
                return userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.get("error.user.notfound", userEmail)));
        }

        /**
         * Charge le bénéficiaire par ID et vérifie qu'il appartient à l'utilisateur.
         *
         * @param beneficiaryId l'ID du bénéficiaire
         * @param user          l'utilisateur connecté
         * @return l'entité Beneficiary
         * @throws ResourceNotFoundException si le bénéficiaire n'existe pas
         * @throws TransferException         si le bénéficiaire ne appartient pas à
         *                                   l'utilisateur
         */
        private Beneficiary loadBeneficiary(Long beneficiaryId, User user) {
                Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.get("error.beneficiary.notfound", beneficiaryId)));

                if (!beneficiary.getUser().getId().equals(user.getId())) {
                        throw new TransferException(
                                        messageUtils.get("error.beneficiary.access.denied"));
                }
                return beneficiary;
        }

        /**
         * Sélectionne le canal de réception du bénéficiaire.
         * Si un channelId est fourni, utilise ce canal spécifique.
         * Sinon, utilise le canal principal (isPrimary=true) ou le premier.
         *
         * @param beneficiary le bénéficiaire
         * @param channelId   l'ID du canal (optionnel)
         * @return le canal sélectionné
         * @throws TransferException si le channelId fourni n'existe pas
         */
        private BeneficiaryChannel selectChannel(Beneficiary beneficiary, Long channelId) {
                if (channelId != null) {
                        return beneficiary.getChannels().stream()
                                        .filter(ch -> ch.getId().equals(channelId))
                                        .findFirst()
                                        .orElseThrow(() -> new TransferException(
                                                        messageUtils.get("error.channel.notfound")));
                }
                return beneficiary.getChannels().stream()
                                .filter(BeneficiaryChannel::getIsPrimary)
                                .findFirst()
                                .orElse(beneficiary.getChannels().get(0));
        }

        /**
         * Simule le transfert (frais, taux de change, réduction parrainage).
         *
         * @param user            l'utilisateur
         * @param selectedChannel le canal sélectionné
         * @param request         la requête de transfert
         * @return le résultat de la simulation
         */
        private SimulateResponse simulateTransfer(User user, BeneficiaryChannel selectedChannel,
                        TransferRequest request) {
                String currencyTo = getCurrencyForChannel(selectedChannel.getChannel().name());
                return simulatorService.simulate(
                                SimulateRequest.builder()
                                                .amount(request.getAmount())
                                                .currencyFrom(request.getCurrency())
                                                .currencyTo(currencyTo)
                                                .channel(selectedChannel.getChannel())
                                                .build(),
                                user.getId());
        }

        /**
         * Crée une transaction en base de données avec le statut PENDING.
         * Créée AVANT le débit Stripe pour tracer même les échecs.
         *
         * @param request         la requête de transfert
         * @param user            l'utilisateur émetteur
         * @param beneficiary     le bénéficiaire
         * @param selectedChannel le canal sélectionné
         * @param simulation      le résultat de la simulation
         * @return la transaction sauvegardée
         */
        private Transaction createPendingTransaction(TransferRequest request, User user, Beneficiary beneficiary,
                        BeneficiaryChannel selectedChannel, SimulateResponse simulation) {
                String currencyTo = getCurrencyForChannel(selectedChannel.getChannel().name());
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
                return transaction;
        }

        /**
         * Débite la carte via Stripe.
         * En cas d'échec, marque la transaction comme FAILED, notifie l'utilisateur
         * et retourne null.
         *
         * @param totalToDebit le montant total à débiter
         * @param request      la requête de transfert
         * @param transaction  la transaction associée
         * @param user         l'utilisateur à notifier en cas d'échec
         * @return le PaymentIntent si réussi, null si échec (transaction déjà gérée)
         */
        private PaymentIntent debitCard(BigDecimal totalToDebit, TransferRequest request,
                        Transaction transaction, User user) {
                log.info("[STRIPE] Debit de {} {} sur la carte", totalToDebit, request.getCurrency());

                try {
                        PaymentIntent paymentIntent = stripePaymentService.createAndConfirmPayment(
                                        totalToDebit,
                                        request.getPaymentMethodId(),
                                        request.getIdempotencyKey());

                        if (!stripePaymentService.isPaymentSucceeded(paymentIntent)) {
                                log.error("[STRIPE] Statut inattendu : {}", paymentIntent.getStatus());
                                Transaction failedTransaction = transactionPersistenceService
                                                .saveFailedTransaction(transaction, paymentIntent.getId(),
                                                                paymentIntent.getStatus());

                                notificationService.createAndSend(
                                                user,
                                                messageUtils.get("notification.transfer.failed.stripe",
                                                                transaction.getReference(), paymentIntent.getStatus()),
                                                NotificationType.TRANSFER_FAILED,
                                                failedTransaction);
                                emailService.sendTransferFailedEmail(user, failedTransaction,
                                                "Paiement non confirme par Stripe. Statut : "
                                                                + paymentIntent.getStatus());
                                return null;
                        }

                        log.info("[STRIPE] Carte debitee avec succes. PaymentIntent : {}", paymentIntent.getId());

                        // Mettre à jour la transaction avec les infos Stripe
                        transaction.setStripePaymentIntentId(paymentIntent.getId());
                        transaction.setStripePaymentStatus(paymentIntent.getStatus());
                        transactionRepository.save(transaction);

                        return paymentIntent;

                } catch (StripeException e) {
                        log.error("[STRIPE] Paiement refuse : {}", e.getMessage());
                        Transaction failedTransaction = transactionPersistenceService
                                        .saveFailedTransaction(transaction, "failed", e.getMessage());

                        notificationService.createAndSend(
                                        user,
                                        messageUtils.get("notification.transfer.failed.payment",
                                                        transaction.getReference(), e.getMessage()),
                                        NotificationType.TRANSFER_FAILED,
                                        failedTransaction);
                        emailService.sendTransferFailedEmail(user, failedTransaction, e.getMessage());
                        return null;
                }
        }

        /**
         * Envoie la notification de transfert initié (SSE + email).
         *
         * @param transaction la transaction
         * @param user        l'utilisateur
         * @param beneficiary le bénéficiaire
         */
        private void notifyTransferInitiated(Transaction transaction, User user, Beneficiary beneficiary) {
                notificationService.createAndSend(
                                user,
                                messageUtils.get("notification.transfer.initiated",
                                                transaction.getReference(),
                                                beneficiary.getFirstName(),
                                                beneficiary.getLastName()),
                                NotificationType.TRANSFER_INITIATED,
                                transaction);
                emailService.sendTransferInitiatedEmail(user, transaction);
        }

        /**
         * Résout la stratégie de transfert pour le canal sélectionné.
         *
         * @param selectedChannel le canal sélectionné
         * @return la stratégie correspondante, ou null si aucune stratégie trouvée
         */
        private TransferStrategy resolveStrategy(BeneficiaryChannel selectedChannel) {
                return strategyMap.get(selectedChannel.getChannel().name());
        }

        /**
         * Traite le succès de la stratégie : marque COMPLETED, notifie,
         * gère le parrainage (coupon + récompense premier transfert).
         *
         * @param transaction la transaction
         * @param user        l'utilisateur
         * @param simulation  la simulation (pour le coupon)
         * @param request     la requête (pour la devise)
         * @return TransactionResponse la réponse formatée
         */
        private TransactionResponse handleSuccessfulStrategy(Transaction transaction, User user,
                        SimulateResponse simulation, TransferRequest request) {
                transaction.setStatus(TransactionStatus.COMPLETED);
                transactionRepository.save(transaction);
                log.info("=== TRANSFERT COMPLETE : {} ===", transaction.getReference());

                // Notification TRANSFER_COMPLETED
                notificationService.createAndSend(
                                user,
                                messageUtils.get("notification.transfer.completed",
                                                transaction.getReference(),
                                                transaction.getAmountReceived(),
                                                transaction.getCurrencyReceived()),
                                NotificationType.TRANSFER_COMPLETED,
                                transaction);
                emailService.sendTransferCompletedEmail(user, transaction);

                // Parrainage : marquer le coupon comme utilisé
                useReferralCouponIfApplicable(user, request.getCurrency(), transaction, simulation);

                // Parrainage : récompenser le premier transfert du filleul
                try {
                        referralService.rewardFirstTransfer(user, transaction);
                } catch (Exception e) {
                        log.error("[PARRAINAGE] Erreur lors de la récompense du premier transfert", e);
                        throw new ibrahima.dickoya.sendme.exception.ReferralException(
                                        "Erreur lors de la récompense de parrainage", e);
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
         * PAS de @Transactional : les opérations BDD sont gérées
         * via TransactionTemplate pour éviter les deadlocks.
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
                                        messageUtils.get("notification.transfer.refunded.success",
                                                        transaction.getReference(),
                                                        reason,
                                                        transaction.getAmountSent().add(transaction.getFees()),
                                                        transaction.getCurrencySent()),
                                        TRANSFER_FAILED,
                                        transaction);

                        emailService.sendTransferRefundedEmail(user, transaction);

                } catch (com.stripe.exception.StripeException e) {
                        transaction.setStatus(TransactionStatus.FAILED);
                        transactionRepository.save(transaction);

                        // Notification SSE : échec du remboursement
                        notificationService.createAndSend(
                                        user,
                                        messageUtils.get("notification.transfer.refunded.failed",
                                                        transaction.getReference(),
                                                        reason),
                                        TRANSFER_FAILED,
                                        transaction);

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
                                                messageUtils.get("error.user.notfound", userEmail)));

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
                                                messageUtils.get("error.user.notfound", userEmail)));

                Transaction transaction = transactionRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.get("error.transaction.notfound", id)));

                // Sécurité : vérifier que la transaction appartient à l'utilisateur
                if (!transaction.getSender().getId().equals(user.getId())) {
                        throw new TransferException(
                                        messageUtils.get("error.transaction.access.denied"));
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
                                                messageUtils.get("error.user.notfound", userEmail)));

                Transaction transaction = transactionRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.get("error.transaction.notfound", id)));

                if (!transaction.getSender().getId().equals(user.getId())) {
                        throw new TransferException(
                                        messageUtils.get("error.transaction.access.denied"));
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
                                                messageUtils.get("error.user.notfound", userEmail)));

                Transaction transaction = transactionRepository
                                .findByReference(reference)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                messageUtils.get("error.transaction.notfound", reference)));

                // Sécurité : vérifier que la transaction appartient à l'utilisateur
                if (!transaction.getSender().getId().equals(user.getId())) {
                        throw new TransferException(
                                        messageUtils.get("error.transaction.access.denied"));
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
