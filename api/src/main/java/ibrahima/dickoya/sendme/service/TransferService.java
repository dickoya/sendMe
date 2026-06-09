package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.SimulateRequest;
import ibrahima.dickoya.sendme.dto.request.TransferRequest;
import ibrahima.dickoya.sendme.dto.response.BeneficiaryResponse;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service principal gérant le cycle de vie complet des transferts.
 *
 * C'est le cœur du projet SendMe. Ce service orchestre :
 * 1. La vérification de l'idempotence (RG-06)
 * 2. La création de la transaction en base
 * 3. La sélection dynamique de la stratégie via le Strategy Pattern
 * 4. L'exécution atomique (@Transactional) du transfert
 * 5. La mise à jour du statut (COMPLETED ou FAILED)
 * 6. Le rollback automatique en cas d'échec
 *
 * Règles métier appliquées :
 * - RG-01 : Zéro solde stocké — tout débit déclenche immédiatement un envoi
 * - RG-05 : Référence unique immuable par transaction
 * - RG-06 : Atomicité via @Transactional Spring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final TransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final SimulatorService simulatorService;

    /**
     * Map injectée automatiquement par Spring IoC.
     * Clé   : nom du @Component de chaque stratégie (ex: "ORANGE_MONEY")
     * Valeur: instance de la stratégie correspondante
     *
     * Spring peuple automatiquement cette map grâce aux annotations
     * @Component("ORANGE_MONEY"), @Component("BANK_TRANSFER"), etc.
     * C'est le cœur du Strategy Pattern.
     */
    private final Map<String, TransferStrategy> strategyMap;

    /**
     * Compteur atomique pour générer les références uniques de transaction.
     * AtomicInteger garantit l'unicité même en cas de requêtes concurrentes.
     */
    private final AtomicInteger counter = new AtomicInteger(1);

    /**
     * Initie et exécute un transfert d'argent de manière atomique.
     *
     * @Transactional garantit que :
     * - Si le transfert réussit  → tout est commité en base
     * - Si le transfert échoue   → tout est annulé (rollback automatique)
     * Ceci implémente la règle RG-06 (atomicité).
     *
     * @param request   les données du transfert
     * @param userEmail l'email de l'utilisateur connecté
     * @return TransactionResponse le résultat complet de la transaction
     */
    @Transactional
    public TransactionResponse processTransfer(TransferRequest request, String userEmail) {

        log.info("Debut du traitement du transfert pour : {}", userEmail);

        // ── ÉTAPE 1 : Vérification de l'idempotence (RG-06) ─────────────────
        // Si une transaction avec cette clé existe déjà, on la retourne
        // directement sans re-débiter (protection contre les doublons)
        var existingTransaction = transactionRepository
                .findByIdempotencyKey(request.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            log.warn("Transaction dupliquee detectee. Cle : {}", request.getIdempotencyKey());
            return toResponse(existingTransaction.get());
        }

        // ── ÉTAPE 2 : Récupérer l'utilisateur et le bénéficiaire ────────────
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Beneficiary beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beneficiaire non trouve avec l'id : " + request.getBeneficiaryId()));

        // Vérifier que le bénéficiaire appartient à l'utilisateur
        if (!beneficiary.getUser().getId().equals(user.getId())) {
            throw new TransferException("Ce beneficiaire ne vous appartient pas");
        }

        // ── ÉTAPE 3 : Calculer les frais et le taux de change ───────────────
        String currencyTo = getCurrencyForChannel(beneficiary.getChannel().name());

        SimulateRequest simulateRequest = SimulateRequest.builder()
                .amount(request.getAmount())
                .currencyFrom(request.getCurrency())
                .currencyTo(currencyTo)
                .channel(beneficiary.getChannel())
                .build();

        SimulateResponse simulation = simulatorService.simulate(simulateRequest);

        // ── ÉTAPE 4 : Créer la transaction en base avec statut PENDING ───────
        Transaction transaction = Transaction.builder()
                .reference(generateReference())           // TXN-YYYYMMDD-XXXX
                .amountSent(request.getAmount())
                .currencySent(request.getCurrency())
                .amountReceived(simulation.getAmountReceived())
                .currencyReceived(currencyTo)
                .exchangeRate(simulation.getExchangeRate())
                .fees(simulation.getFees())
                .status(TransactionStatus.PENDING)        // statut initial
                .channel(beneficiary.getChannel())
                .idempotencyKey(request.getIdempotencyKey())
                .sender(user)
                .beneficiary(beneficiary)
                .build();

        transaction = transactionRepository.save(transaction);
        log.info("Transaction creee : {} avec statut PENDING", transaction.getReference());

        // ── ÉTAPE 5 : Passer le statut à PROCESSING ─────────────────────────
        transaction.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        // ── ÉTAPE 6 : Sélectionner et exécuter la stratégie ─────────────────
        // Spring IoC injecte automatiquement la bonne stratégie via la Map
        String channelName = beneficiary.getChannel().name();
        TransferStrategy strategy = strategyMap.get(channelName);

        if (strategy == null) {
            // Canal non supporté — ne devrait jamais arriver si bien configuré
            throw new TransferException("Canal non supporte : " + channelName);
        }

        log.info("Execution de la strategie : {}", channelName);
        TransferResult result = strategy.execute(transaction, beneficiary);

        // ── ÉTAPE 7 : Mettre à jour le statut selon le résultat ─────────────
        if (result.isSuccess()) {
            // Transfert réussi → COMPLETED
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
            log.info("Transaction {} COMPLETED. Ref externe : {}",
                    transaction.getReference(), result.getExternalRef());
        } else {
            // Transfert échoué → FAILED
            // @Transactional va rollback automatiquement grâce au throw
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.error("Transaction {} FAILED : {}", transaction.getReference(), result.getMessage());

            // Lancer une exception pour déclencher le rollback de @Transactional
            throw new TransferException(
                    "Transfert echoue : " + result.getMessage() +
                            ". Le debit de votre carte sera annule.");
        }

        return toResponse(transaction);
    }

    /**
     * Récupère l'historique paginé des transactions de l'utilisateur connecté.
     *
     * @param userEmail l'email de l'utilisateur
     * @param pageable  paramètres de pagination (page, size, sort)
     * @return Page de TransactionResponse
     */
    public Page<TransactionResponse> getHistory(String userEmail, Pageable pageable) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        // Récupérer les transactions paginées depuis la base de données
        return transactionRepository
                .findBySenderIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse); // convertir chaque Transaction en DTO
    }

    /**
     * Récupère le détail d'une transaction par son identifiant.
     * Vérifie que la transaction appartient à l'utilisateur connecté.
     *
     * @param id        l'identifiant de la transaction
     * @param userEmail l'email de l'utilisateur connecté
     * @return TransactionResponse le détail de la transaction
     */
    public TransactionResponse getById(Long id, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouve : " + userEmail));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction non trouvee avec l'id : " + id));

        // Sécurité : vérifier que la transaction appartient à l'utilisateur
        if (!transaction.getSender().getId().equals(user.getId())) {
            throw new TransferException("Acces refuse a cette transaction");
        }

        return toResponse(transaction);
    }

    /**
     * Génère une référence unique et immuable pour une transaction (RG-05).
     *
     * Format : TXN-YYYYMMDD-XXXX
     * Exemple : TXN-20250601-0042
     *
     * @return String la référence générée
     */
    private String generateReference() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int count = counter.getAndIncrement();
        return String.format("TXN-%s-%04d", date, count);
    }

    /**
     * Détermine la devise de réception selon le canal utilisé.
     *
     * En production, cette logique serait plus complexe et prendrait
     * en compte le pays du bénéficiaire.
     *
     * @param channelName le nom du canal (ex: "ORANGE_MONEY")
     * @return le code devise ISO 4217
     */
    private String getCurrencyForChannel(String channelName) {
        return switch (channelName) {
            case "BANK_TRANSFER" -> "EUR";    // virement bancaire en EUR par défaut
            case "ORANGE_MONEY",
                 "MTN_MOBILE_MONEY",
                 "WAVE",
                 "MOOV_MONEY",
                 "AIRTEL_MONEY"   -> "XOF";  // Franc CFA par défaut
            default               -> "XOF";
        };
    }

    /**
     * Convertit une entité Transaction en DTO TransactionResponse.
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
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}