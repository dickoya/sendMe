package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.BeneficiaryChannel;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Stratégie concrète pour les virements bancaires (SEPA/SWIFT).
 *
 * En production, cette classe ferait un appel HTTP vers l'API SWIFT/SEPA
 * avec les coordonnées bancaires (IBAN, BIC) du bénéficiaire.
 *
 * En mode simulation (strategy.simulation-mode=true) :
 * - Délai entre 1 et 3 secondes (non-bloquant via CompletableFuture)
 * - Taux d'échec configurable via strategy.failure-rate (défaut: 0%)
 *
 * @Component("BANK_TRANSFER") permet à Spring d'injecter cette stratégie
 * automatiquement via son nom dans le Map<String, TransferStrategy>.
 */
@Component("BANK_TRANSFER")
@Slf4j
public class BankTransferStrategy extends BaseTransferStrategy {

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        // Récupérer le canal bancaire principal
        String iban = beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == ChannelType.BANK_TRANSFER)
                .findFirst()
                .map(BeneficiaryChannel::getIban)
                .orElse("N/A");

        log.info("[BANK_TRANSFER] Debut du virement vers IBAN: {}",
                maskIban(iban));

        try {
            // Simuler le délai de traitement bancaire (1 à 3 secondes, non-bloquant)
            simulateProcessingDelay(1000, 3000).get(5, TimeUnit.SECONDS);

            // Simuler un échec selon le taux configuré (uniquement en mode simulation)
            if (shouldSimulateFailure()) {
                log.warn("[BANK_TRANSFER] Virement rejete par la banque destinataire (simulation)");
                return TransferResult.failure("Virement rejete par la banque destinataire");
            }

            // Générer une référence externe simulée (en prod : réponse de l'API bancaire)
            String externalRef = "BANK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            log.info("[BANK_TRANSFER] Virement reussi. Ref externe : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            return handleInterruption("BANK_TRANSFER");
        } catch (Exception e) {
            log.error("[BANK_TRANSFER] Erreur lors du virement : {}", e.getMessage());
            return TransferResult.failure("Erreur technique virement bancaire : " + e.getMessage());
        }
    }
}
