package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Service dédié à la persistance des transactions en échec.
 * 
 * Séparé de TransferService pour éviter le problème de self-invocation
 * des proxies AOP Spring. Les appels à ce service depuis TransferService
 * passent bien par le proxy, ce qui permet à @Transactional(REQUIRES_NEW)
 * de fonctionner correctement et de persister même si la transaction
 * parente est rollbackée.
 * 
 * IMPORTANT : Ce service crée une NOUVELLE entité Transaction plutôt que
 * de modifier l'entité existante. Cela évite les deadlocks MySQL :
 * la transaction parente a un lock sur la ligne de la table transactions
 * (INSERT du PENDING), et REQUIRES_NEW suspend la transaction parente
 * mais ne libère pas le lock. Un UPDATE (merge) sur la même ligne
 * provoque un "Lock wait timeout". Un INSERT d'une nouvelle ligne
 * ne rencontre pas ce problème.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionPersistenceService {

        private final TransactionRepository transactionRepository;

        /**
         * Sauvegarde une transaction en échec dans une transaction séparée.
         * REQUIRES_NEW permet de persister même si la transaction parente
         * est rollbackée suite à une exception.
         * 
         * Crée une NOUVELLE entité pour éviter les deadlocks MySQL
         * (voir commentaire de classe).
         */
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public Transaction saveFailedTransaction(Transaction original, String stripePaymentIntentId,
                        String stripePaymentStatus) {
                // Tronquer le message Stripe à 490 caractères pour éviter
                // les erreurs de troncature MySQL (VARCHAR(500))
                var truncatedStatus = stripePaymentStatus != null && stripePaymentStatus.length() > 490
                                ? stripePaymentStatus.substring(0, 490)
                                : stripePaymentStatus;

                // Créer une NOUVELLE entité pour éviter les deadlocks MySQL.
                // La transaction parente a un lock sur la ligne originale (INSERT du PENDING).
                // Un UPDATE (merge) sur la même ligne dans REQUIRES_NEW provoque un
                // "Lock wait timeout" car REQUIRES_NEW suspend la transaction parente
                // mais ne libère pas ses locks.
                // La référence et l'idempotency_key sont modifiées pour éviter les
                // contraintes UNIQUE sur ces colonnes.
                // Format référence : TXN-YYYYMMDD-XXXX-F-99999 (suffixe court)
                // Format idempotency_key : UUID-F-99999
                int randomSuffix = ThreadLocalRandom.current().nextInt(10000, 99999);
                String failedReference = original.getReference() + "-F-" + randomSuffix;
                String failedIdempotencyKey = original.getIdempotencyKey() + "-F-" + randomSuffix;

                Transaction failed = Transaction.builder()
                                .reference(failedReference)
                                .amountSent(original.getAmountSent())
                                .currencySent(original.getCurrencySent())
                                .amountReceived(original.getAmountReceived())
                                .currencyReceived(original.getCurrencyReceived())
                                .exchangeRate(original.getExchangeRate())
                                .fees(original.getFees())
                                .status(TransactionStatus.FAILED)
                                .channel(original.getChannel())
                                .idempotencyKey(failedIdempotencyKey)
                                .sender(original.getSender())
                                .beneficiary(original.getBeneficiary())
                                .stripePaymentIntentId(stripePaymentIntentId)
                                .stripePaymentStatus(truncatedStatus)
                                .build();

                Transaction saved = transactionRepository.save(failed);
                log.info("Transaction echouee sauvegardee : {}", saved.getReference());
                return saved;
        }

}
