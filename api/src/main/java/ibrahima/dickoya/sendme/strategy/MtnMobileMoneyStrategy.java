package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stratégie concrète pour les transferts MTN Mobile Money (MoMo).
 *
 * En production, utiliserait l'API officielle MTN MoMo
 * (https://momodeveloper.mtn.com) avec clé API et sandbox/production.
 *
 * Le squelette d'exécution est défini dans {@link BaseTransferStrategy}.
 * Cette classe ne fait que fournir les paramètres spécifiques à MTN MoMo.
 */
@Component("MTN_MOBILE_MONEY")
@Slf4j
public class MtnMobileMoneyStrategy extends BaseTransferStrategy {

    @Override
    protected ChannelType getChannelType() {
        return ChannelType.MTN_MOBILE_MONEY;
    }

    @Override
    protected String getChannelName() {
        return "MTN_MOBILE_MONEY";
    }

    @Override
    protected String getReferencePrefix() {
        return "MTN-";
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
        return "Compte MTN MoMo inactif ou reseau indisponible";
    }

    @Override
    protected String getErrorMessage() {
        return "Erreur technique MTN MoMo";
    }
}
