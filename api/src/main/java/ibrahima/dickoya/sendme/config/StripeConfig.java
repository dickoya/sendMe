package ibrahima.dickoya.sendme.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de Stripe au démarrage de l'application.
 *
 * @PostConstruct garantit que la clé secrète Stripe est initialisée
 * UNE SEULE FOIS au démarrage, avant toute requête.
 *
 * En mode sandbox (sk_test_...) : aucun vrai argent n'est débité.
 * En mode production (sk_live_...) : vrais débits — à utiliser avec précaution.
 *
 * La clé secrète NE DOIT JAMAIS être commitée sur GitHub.
 * Elle est lue depuis application.properties qui lui-même
 * la lit depuis la variable d'environnement STRIPE_SECRET_KEY.
 */
@Configuration
@Slf4j
public class StripeConfig {

    /**
     * Clé secrète Stripe injectée depuis application.properties.
     * Format sandbox  : sk_test_XXXX
     * Format production : sk_live_XXXX
     */
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    /**
     * Initialise le SDK Stripe avec la clé secrète.
     * Appelé automatiquement par Spring après l'injection des dépendances.
     */
    @PostConstruct
    public void initStripe() {
        // Injecter la clé secrète dans le SDK Stripe globalement
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe SDK initialise en mode : {}",
                stripeSecretKey.startsWith("sk_test") ? "SANDBOX" : "PRODUCTION");
    }
}