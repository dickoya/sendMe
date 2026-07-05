package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stratégie concrète pour les virements bancaires (SEPA/SWIFT).
 *
 * En production, cette classe ferait un appel HTTP vers l'API SWIFT/SEPA
 * avec les coordonnées bancaires (IBAN, BIC) du bénéficiaire.
 *
 * Le squelette d'exécution est défini dans {@link BaseTransferStrategy}.
 * Cette classe ne fait que fournir les paramètres spécifiques aux virements
 * bancaires.
 */
@Component("BANK_TRANSFER")
@Slf4j
public class BankTransferStrategy extends BaseTransferStrategy {

    @Override
    protected ChannelType getChannelType() {
        return ChannelType.BANK_TRANSFER;
    }

    @Override
    protected String getChannelName() {
        return "BANK_TRANSFER";
    }

    @Override
    protected String getReferencePrefix() {
        return "BANK-";
    }

    @Override
    protected int getDelayMinMs() {
        return 1000;
    }

    @Override
    protected int getDelayMaxMs() {
        return 3000;
    }

    @Override
    protected String getFailureMessage() {
        return "Virement rejete par la banque destinataire";
    }

    @Override
    protected String getErrorMessage() {
        return "Erreur technique virement bancaire";
    }
}
