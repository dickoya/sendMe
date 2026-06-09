package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

/**
 * Stratégie concrète pour les virements bancaires (SEPA/SWIFT).
 *
 * Simule l'appel vers l'API d'une banque partenaire.
 * En production, cette classe ferait un appel HTTP vers l'API SWIFT/SEPA
 * avec les coordonnées bancaires (IBAN, BIC) du bénéficiaire.
 *
 * Simulation :
 * - Délai entre 1 et 3 secondes (temps de traitement bancaire)
 * - 5% de chance d'échec aléatoire (simule les rejets bancaires)
 *
 * @Component("BANK_TRANSFER") permet à Spring d'injecter cette stratégie
 * automatiquement via son nom dans le Map<String, TransferStrategy>.
 */
@Component("BANK_TRANSFER")
@Slf4j
public class BankTransferStrategy implements TransferStrategy {

    /** Générateur de nombres aléatoires pour simuler les échecs. */
    private final Random random = new Random();

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        log.info("[BANK_TRANSFER] Debut du virement vers IBAN: {}",
                maskIban(beneficiary.getBankDetails().getIban()));

        try {
            // Simuler le délai de traitement bancaire (1 à 3 secondes)
            simulateProcessingDelay(1000, 3000);

            // Simuler 5% de chance d'échec (rejet bancaire)
            if (random.nextInt(100) < 5) {
                log.warn("[BANK_TRANSFER] Virement rejete par la banque destinataire");
                return TransferResult.failure("Virement rejete par la banque destinataire");
            }

            // Générer une référence externe simulée (en prod : réponse de l'API bancaire)
            String externalRef = "BANK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            log.info("[BANK_TRANSFER] Virement reussi. Ref externe : {}", externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            // Restaurer l'état d'interruption du thread
            Thread.currentThread().interrupt();
            return TransferResult.failure("Interruption du traitement bancaire");
        }
    }

    /**
     * Masque l'IBAN pour la sécurité dans les logs.
     * Exemple : FR7630006000011234567890189 → FR76****890189
     *
     * @param iban l'IBAN complet
     * @return l'IBAN masqué
     */
    private String maskIban(String iban) {
        if (iban == null || iban.length() < 8) return "****";
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 6);
    }

    /**
     * Simule un délai de traitement réseau.
     *
     * @param minMs délai minimum en millisecondes
     * @param maxMs délai maximum en millisecondes
     */
    private void simulateProcessingDelay(int minMs, int maxMs)
            throws InterruptedException {
        int delay = minMs + random.nextInt(maxMs - minMs);
        Thread.sleep(delay);
    }
}