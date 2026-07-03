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
 * Stratégie concrète pour les transferts Airtel Money.
 *
 * Airtel Money est présent dans 14 pays africains dont
 * le Kenya, la Tanzanie, l'Ouganda, le Rwanda, le Niger, etc.
 *
 * En production, utiliserait l'API Airtel Money
 * (https://developers.airtel.africa) avec OAuth2.
 *
 * En mode simulation (strategy.simulation-mode=true) :
 * - Délai entre 500ms et 2 secondes (non-bloquant via CompletableFuture)
 * - Taux d'échec configurable via strategy.failure-rate (défaut: 0%)
 */
@Component("AIRTEL_MONEY")
@Slf4j
public class AirtelMoneyStrategy extends BaseTransferStrategy {

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        String phoneNumber = beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == ChannelType.AIRTEL_MONEY)
                .findFirst()
                .map(BeneficiaryChannel::getPhoneNumber)
                .orElse("N/A");
        log.info("[AIRTEL_MONEY] Debut du transfert vers : {}", maskPhone(phoneNumber));

        try {
            simulateProcessingDelay(500, 2000).get(5, TimeUnit.SECONDS);

            if (shouldSimulateFailure()) {
                log.warn("[AIRTEL_MONEY] Transfert rejete par Airtel Money (simulation)");
                return TransferResult.failure("Compte Airtel Money inactif ou reseau indisponible");
            }

            String externalRef = "AIRTEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("[AIRTEL_MONEY] Transfert reussi. Ref : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            return handleInterruption("AIRTEL_MONEY");
        } catch (Exception e) {
            log.error("[AIRTEL_MONEY] Erreur lors du transfert : {}", e.getMessage());
            return TransferResult.failure("Erreur technique Airtel Money : " + e.getMessage());
        }
    }
}
