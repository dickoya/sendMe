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
public class OrangeMoneyStrategy extends BaseTransferStrategy {

    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {

        // Récupérer le canal Orange Money
        String phoneNumber = beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == ChannelType.ORANGE_MONEY)
                .findFirst()
                .map(BeneficiaryChannel::getPhoneNumber)
                .orElse("N/A");
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
            return handleInterruption("ORANGE_MONEY");
        }
    }
}