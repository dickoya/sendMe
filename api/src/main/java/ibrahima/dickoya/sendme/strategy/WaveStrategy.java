package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Stratégie concrète pour les transferts Wave.
 *
 * Wave est un opérateur Mobile Money populaire en Afrique de l'Ouest
 * (Sénégal, Côte d'Ivoire, Mali, Burkina Faso, Guinée, Ouganda).
 *
 * En production, utiliserait l'API Wave Business
 * avec authentification par token Bearer.
 *
 * Simulation :
 * - Délai entre 300ms et 1.5 secondes (Wave est généralement rapide)
 * - 5% de chance d'échec
 */
@Component("WAVE")
@Slf4j
public class WaveStrategy implements TransferStrategy {

    private final Random random = new Random();

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        String phoneNumber = beneficiary.getMobileMoneyDetails().getPhoneNumber();
        log.info("[WAVE] Debut du transfert vers : {}", maskPhone(phoneNumber));

        try {
            // Wave est généralement plus rapide que les autres opérateurs
            simulateProcessingDelay(300, 1500);

            if (random.nextInt(100) < 5) {
                log.warn("[WAVE] Transfert rejete par Wave");
                return TransferResult.failure("Compte Wave inactif ou limite depassee");
            }

            String externalRef = "WAVE-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            log.info("[WAVE] Transfert reussi. Ref : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return TransferResult.failure("Interruption du traitement Wave");
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