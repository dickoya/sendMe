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
 * Stratégie concrète pour les transferts Airtel Money.
 *
 * Airtel Money est présent dans 14 pays africains dont
 * le Kenya, la Tanzanie, l'Ouganda, le Rwanda, le Niger, etc.
 *
 * En production, utiliserait l'API Airtel Money
 * (https://developers.airtel.africa) avec OAuth2.
 */
@Component("AIRTEL_MONEY")
@Slf4j
public class AirtelMoneyStrategy implements TransferStrategy {

    private final Random random = new Random();

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        String phoneNumber = beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == ChannelType.AIRTEL_MONEY)
                .findFirst()
                .map(BeneficiaryChannel::getPhoneNumber)
                .orElse("N/A");
        log.info("[AIRTEL_MONEY] Debut du transfert vers : {}", maskPhone(phoneNumber));

        try {
            simulateProcessingDelay(500, 2000);

            if (random.nextInt(100) < 5) {
                log.warn("[AIRTEL_MONEY] Transfert rejete par Airtel Money");
                return TransferResult.failure("Compte Airtel Money inactif ou reseau indisponible");
            }

            String externalRef = "AIRTEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            log.info("[AIRTEL_MONEY] Transfert reussi. Ref : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return TransferResult.failure("Interruption du traitement Airtel Money");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6)
            return "****";
        return phone.substring(0, phone.length() - 4) + "****";
    }

    private void simulateProcessingDelay(int minMs, int maxMs)
            throws InterruptedException {
        Thread.sleep(minMs + new Random().nextInt(maxMs - minMs));
    }
}
