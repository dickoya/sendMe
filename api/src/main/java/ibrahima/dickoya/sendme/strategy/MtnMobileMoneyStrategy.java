package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.BeneficiaryChannel;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Stratégie concrète pour les transferts MTN Mobile Money (MoMo).
 *
 * Simule l'appel vers l'API MTN MoMo.
 * En production, utiliserait l'API officielle MTN MoMo
 * (https://momodeveloper.mtn.com) avec clé API et sandbox/production.
 *
 * Simulation :
 * - Délai entre 500ms et 2 secondes
 * - 5% de chance d'échec
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
            simulateProcessingDelay(500, 2000);

            if (random.nextInt(100) < 5) {
                log.warn("[MTN_MOMO] Transfert rejete par MTN MoMo");
                return TransferResult.failure("Compte MTN MoMo inactif ou reseau indisponible");
            }

            String externalRef = "MTN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            log.info("[MTN_MOMO] Transfert reussi. Ref : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            return handleInterruption("MTN_MOBILE_MONEY");
        }
    }
}
