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
 * Stratégie concrète pour les transferts Wave.
 *
 * Wave est un opérateur Mobile Money populaire en Afrique de l'Ouest
 * (Sénégal, Côte d'Ivoire, Mali, Burkina Faso, Guinée, Ouganda).
 *
 * En production, utiliserait l'API Wave Business
 * avec authentification par token Bearer.
 *
 * En mode simulation (strategy.simulation-mode=true) :
 * - Délai entre 300ms et 1.5 secondes (non-bloquant via CompletableFuture)
 * - Taux d'échec configurable via strategy.failure-rate (défaut: 0%)
 */
@Component("WAVE")
@Slf4j
public class WaveStrategy extends BaseTransferStrategy {

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        String phoneNumber = beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == ChannelType.WAVE)
                .findFirst()
                .map(BeneficiaryChannel::getPhoneNumber)
                .orElse("N/A");
        log.info("[WAVE] Debut du transfert vers : {}", maskPhone(phoneNumber));

        try {
            // Wave est généralement plus rapide que les autres opérateurs
            simulateProcessingDelay(300, 1500).get(5, TimeUnit.SECONDS);

            if (shouldSimulateFailure()) {
                log.warn("[WAVE] Transfert rejete par Wave (simulation)");
                return TransferResult.failure("Compte Wave inactif ou limite depassee");
            }

            String externalRef = "WAVE-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            log.info("[WAVE] Transfert reussi. Ref : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            return handleInterruption("WAVE");
        } catch (Exception e) {
            log.error("[WAVE] Erreur lors du transfert : {}", e.getMessage());
            return TransferResult.failure("Erreur technique Wave : " + e.getMessage());
        }
    }
}
