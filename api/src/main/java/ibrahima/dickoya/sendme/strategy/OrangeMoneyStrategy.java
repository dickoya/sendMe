package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Stratégie concrète pour les transferts Orange Money.
 *
 * Simule l'appel vers l'API Orange Money.
 * En production, cette classe utiliserait le SDK officiel Orange Money
 * ou l'API REST d'Orange avec authentification OAuth2.
 *
 * Simulation :
 * - Délai entre 500ms et 2 secondes
 * - 5% de chance d'échec (numéro inactif, réseau indisponible, etc.)
 */
@Component("ORANGE_MONEY")
@Slf4j
public class OrangeMoneyStrategy implements TransferStrategy {

    private final Random random = new Random();

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        String phoneNumber = beneficiary.getMobileMoneyDetails().getPhoneNumber();
        log.info("[ORANGE_MONEY] Debut du transfert vers le numero : {}", maskPhone(phoneNumber));

        try {
            // Simuler le délai de traitement Orange Money
            simulateProcessingDelay(500, 2000);

            // Simuler 5% de chance d'échec
            if (random.nextInt(100) < 5) {
                log.warn("[ORANGE_MONEY] Transfert rejete : numero inactif ou solde insuffisant");
                return TransferResult.failure("Numero Orange Money inactif ou indisponible");
            }

            // Générer une référence externe simulée
            String externalRef = "OM-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

            log.info("[ORANGE_MONEY] Transfert reussi vers {}. Ref : {}",
                    maskPhone(phoneNumber), externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return TransferResult.failure("Interruption du traitement Orange Money");
        }
    }

    /**
     * Masque le numéro de téléphone pour la sécurité dans les logs.
     * Exemple : +22507123456 → +225****3456
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, phone.length() - 4) + "****";
    }

    private void simulateProcessingDelay(int minMs, int maxMs)
            throws InterruptedException {
        Thread.sleep(minMs + new Random().nextInt(maxMs - minMs));
    }
}