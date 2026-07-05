package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stratégie concrète pour les transferts Wave.
 *
 * Wave est un opérateur Mobile Money populaire en Afrique de l'Ouest
 * (Sénégal, Côte d'Ivoire, Mali, Burkina Faso, Guinée, Ouganda).
 *
 * En production, utiliserait l'API Wave Business
 * avec authentification par token Bearer.
 *
 * Le squelette d'exécution est défini dans {@link BaseTransferStrategy}.
 * Cette classe ne fait que fournir les paramètres spécifiques à Wave.
 */
@Component("WAVE")
@Slf4j
public class WaveStrategy extends BaseTransferStrategy {

    @Override
    protected ChannelType getChannelType() {
        return ChannelType.WAVE;
    }

    @Override
    protected String getChannelName() {
        return "WAVE";
    }

    @Override
    protected String getReferencePrefix() {
        return "WAVE-";
    }

    @Override
    protected int getDelayMinMs() {
        return 300;
    }

    @Override
    protected int getDelayMaxMs() {
        return 1500;
    }

    @Override
    protected String getFailureMessage() {
        return "Compte Wave inactif ou limite depassee";
    }

    @Override
    protected String getErrorMessage() {
        return "Erreur technique Wave";
    }
}
