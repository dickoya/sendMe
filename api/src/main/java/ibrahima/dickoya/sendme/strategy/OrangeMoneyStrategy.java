package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.BeneficiaryChannel;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Stratégie concrète pour les transferts Orange Money.
 *
 * En production, cette classe utiliserait le SDK officiel Orange Money
 * ou l'API REST d'Orange avec authentification OAuth2.
 *
 * En mode simulation (strategy.simulation-mode=true) :
 * - Délai entre 500ms et 2 secondes (non-bloquant via CompletableFuture)
 * - Taux d'échec configurable via strategy.failure-rate (défaut: 0%)
 */
@Component("ORANGE_MONEY")
@Slf4j
public class OrangeMoneyStrategy extends BaseTransferStrategy {

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        // Récupérer le canal Orange Money
        String phoneNumber = beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == ChannelType.ORANGE_MONEY)
                .findFirst()
                .map(BeneficiaryChannel::getPhoneNumber)
                .orElse("N/A");
        log.info("[ORANGE_MONEY] Debut du transfert vers le numero : {}", maskPhone(phoneNumber));

        try {
            // Simuler le délai de traitement Orange Money (non-bloquant)
            simulateProcessingDelay(500, 2000).get(5, TimeUnit.SECONDS);

            // Simuler un échec selon le taux configuré (uniquement en mode simulation)
            if (shouldSimulateFailure()) {
                log.warn("[ORANGE_MONEY] Transfert rejete : numero inactif ou solde insuffisant (simulation)");
                return TransferResult.failure("Numero Orange Money inactif ou indisponible");
            }

            // Générer une référence externe simulée
            String externalRef = "OM-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

            log.info("[ORANGE_MONEY] Transfert reussi vers {}. Ref : {}",
                    maskPhone(phoneNumber), externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            return handleInterruption("ORANGE_MONEY");
        } catch (Exception e) {
            log.error("[ORANGE_MONEY] Erreur lors du transfert : {}", e.getMessage());
            return TransferResult.failure("Erreur technique Orange Money : " + e.getMessage());
        }
    }
}
