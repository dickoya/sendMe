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
 * Stratégie concrète pour les transferts MTN Mobile Money (MoMo).
 *
 * En production, utiliserait l'API officielle MTN MoMo
 * (https://momodeveloper.mtn.com) avec clé API et sandbox/production.
 *
 * En mode simulation (strategy.simulation-mode=true) :
 * - Délai entre 500ms et 2 secondes (non-bloquant via CompletableFuture)
 * - Taux d'échec configurable via strategy.failure-rate (défaut: 0%)
 */
@Component("MTN_MOBILE_MONEY")
@Slf4j
public class MtnMobileMoneyStrategy extends BaseTransferStrategy {

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        String phoneNumber = beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == ChannelType.MTN_MOBILE_MONEY)
                .findFirst()
                .map(BeneficiaryChannel::getPhoneNumber)
                .orElse("N/A");
        log.info("[MTN_MOMO] Debut du transfert vers : {}", maskPhone(phoneNumber));

        try {
            simulateProcessingDelay(500, 2000).get(5, TimeUnit.SECONDS);

            if (shouldSimulateFailure()) {
                log.warn("[MTN_MOMO] Transfert rejete par MTN MoMo (simulation)");
                return TransferResult.failure("Compte MTN MoMo inactif ou reseau indisponible");
            }

            String externalRef = "MTN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            log.info("[MTN_MOMO] Transfert reussi. Ref : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            return handleInterruption("MTN_MOBILE_MONEY");
        } catch (Exception e) {
            log.error("[MTN_MOMO] Erreur lors du transfert : {}", e.getMessage());
            return TransferResult.failure("Erreur technique MTN MoMo : " + e.getMessage());
        }
    }
}
