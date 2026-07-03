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
 * Stratégie concrète pour les transferts Moov Money.
 *
 * Moov Money (Atlantique Télécom) est présent dans plusieurs pays
 * d'Afrique de l'Ouest et Centrale.
 *
 * En production, utiliserait l'API Moov Africa avec
 * authentification par certificat client.
 *
 * En mode simulation (strategy.simulation-mode=true) :
 * - Délai entre 500ms et 2 secondes (non-bloquant via CompletableFuture)
 * - Taux d'échec configurable via strategy.failure-rate (défaut: 0%)
 */
@Component("MOOV_MONEY")
@Slf4j
public class MoovMoneyStrategy extends BaseTransferStrategy {

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        String phoneNumber = beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == ChannelType.MOOV_MONEY)
                .findFirst()
                .map(BeneficiaryChannel::getPhoneNumber)
                .orElse("N/A");
        log.info("[MOOV_MONEY] Debut du transfert vers : {}", maskPhone(phoneNumber));

        try {
            simulateProcessingDelay(500, 2000).get(5, TimeUnit.SECONDS);

            if (shouldSimulateFailure()) {
                log.warn("[MOOV_MONEY] Transfert rejete par Moov Money (simulation)");
                return TransferResult.failure("Compte Moov Money inactif ou service indisponible");
            }

            String externalRef = "MOOV-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            log.info("[MOOV_MONEY] Transfert reussi. Ref : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            return handleInterruption("MOOV_MONEY");
        } catch (Exception e) {
            log.error("[MOOV_MONEY] Erreur lors du transfert : {}", e.getMessage());
            return TransferResult.failure("Erreur technique Moov Money : " + e.getMessage());
        }
    }
}
