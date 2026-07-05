package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stratégie concrète pour les transferts Airtel Money.
 *
 * Airtel Money est présent dans 14 pays africains dont
 * le Kenya, la Tanzanie, l'Ouganda, le Rwanda, le Niger, etc.
 *
 * En production, utiliserait l'API Airtel Money
 * (https://developers.airtel.africa) avec OAuth2.
 *
 * Le squelette d'exécution est défini dans {@link BaseTransferStrategy}.
 * Cette classe ne fait que fournir les paramètres spécifiques à Airtel Money.
 */
@Component("AIRTEL_MONEY")
@Slf4j
public class AirtelMoneyStrategy extends BaseTransferStrategy {

    @Override
    protected ChannelType getChannelType() {
        return ChannelType.AIRTEL_MONEY;
    }

    @Override
    protected String getChannelName() {
        return "AIRTEL_MONEY";
    }

    @Override
    protected String getReferencePrefix() {
        return "AIRTEL-";
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
        return "Compte Airtel Money inactif ou reseau indisponible";
    }

    @Override
    protected String getErrorMessage() {
        return "Erreur technique Airtel Money";
    }
}
