package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stratégie concrète pour les transferts Orange Money.
 *
 * En production, cette classe utiliserait le SDK officiel Orange Money
 * ou l'API REST d'Orange avec authentification OAuth2.
 *
 * Le squelette d'exécution est défini dans {@link BaseTransferStrategy}.
 * Cette classe ne fait que fournir les paramètres spécifiques à Orange Money.
 */
@Component("ORANGE_MONEY")
@Slf4j
public class OrangeMoneyStrategy extends BaseTransferStrategy {

    @Override
    protected ChannelType getChannelType() {
        return ChannelType.ORANGE_MONEY;
    }

    @Override
    protected String getChannelName() {
        return "ORANGE_MONEY";
    }

    @Override
    protected String getReferencePrefix() {
        return "OM-";
    }

    @Override
    protected int getDelayMinMs() {
        return 500;
    }

    @Override
    protected int getDelayMaxMs() {
        return 2000;
    }

    @Override
    protected String getFailureMessage() {
        return "Numero Orange Money inactif ou indisponible";
    }

    @Override
    protected String getErrorMessage() {
        return "Erreur technique Orange Money";
    }
}
