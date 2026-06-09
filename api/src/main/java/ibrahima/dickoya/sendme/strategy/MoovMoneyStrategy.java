package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Stratégie concrète pour les transferts Moov Money.
 *
 * Moov Money (Atlantique Télécom) est présent dans plusieurs pays
 * d'Afrique de l'Ouest et Centrale.
 *
 * En production, utiliserait l'API Moov Africa avec
 * authentification par certificat client.
 */
@Component("MOOV_MONEY")
@Slf4j
public class MoovMoneyStrategy implements TransferStrategy {

    private final Random random = new Random();

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        String phoneNumber = beneficiary.getMobileMoneyDetails().getPhoneNumber();
        log.info("[MOOV_MONEY] Debut du transfert vers : {}", maskPhone(phoneNumber));

        try {
            simulateProcessingDelay(500, 2000);

            if (random.nextInt(100) < 5) {
                log.warn("[MOOV_MONEY] Transfert rejete par Moov Money");
                return TransferResult.failure("Compte Moov Money inactif ou service indisponible");
            }

            String externalRef = "MOOV-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            log.info("[MOOV_MONEY] Transfert reussi. Ref : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return TransferResult.failure("Interruption du traitement Moov Money");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, phone.length() - 4) + "****";
    }

    private void simulateProcessingDelay(int minMs, int maxMs)
            throws InterruptedException {
        Thread.sleep(minMs + new Random().nextInt(maxMs - minMs));
    }
}