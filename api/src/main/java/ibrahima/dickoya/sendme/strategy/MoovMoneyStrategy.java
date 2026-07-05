package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stratégie concrète pour les transferts Moov Money.
 *
 * Moov Money (Atlantique Télécom) est présent dans plusieurs pays
 * d'Afrique de l'Ouest et Centrale.
 *
 * En production, utiliserait l'API Moov Africa avec
 * authentification par certificat client.
 *
 * Le squelette d'exécution est défini dans {@link BaseTransferStrategy}.
 * Cette classe ne fait que fournir les paramètres spécifiques à Moov Money.
 */
@Component("MOOV_MONEY")
@Slf4j
public class MoovMoneyStrategy extends BaseTransferStrategy {

    @Override
    protected ChannelType getChannelType() {
        return ChannelType.MOOV_MONEY;
    }

    @Override
    protected String getChannelName() {
        return "MOOV_MONEY";
    }

    @Override
    protected String getReferencePrefix() {
        return "MOOV-";
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
        return "Compte Moov Money inactif ou service indisponible";
    }

    @Override
    protected String getErrorMessage() {
        return "Erreur technique Moov Money";
    }
}
