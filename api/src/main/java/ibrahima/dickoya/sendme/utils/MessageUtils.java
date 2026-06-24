package ibrahima.dickoya.sendme.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Utilitaire pour accéder facilement aux messages internationalisés.
 * 
 * Cette classe simplifie l'utilisation de MessageSource en fournissant
 * des méthodes courtes et pratiques pour récupérer les messages.
 * 
 * Utilisation :
 * 
 * <pre>
 * {@code
 * // Injection dans un service
 * @Autowired
 * private MessageUtils messageUtils;
 * 
 * // Message simple
 * String msg = messageUtils.get("error.user.notfound", email);
 * 
 * // Message avec paramètres
 * String msg = messageUtils.get("notification.transfer.completed", amount, currency, beneficiary);
 * }
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class MessageUtils {

    private final MessageSource messageSource;

    /**
     * Récupère un message internationalisé selon la locale courante.
     * La locale est déterminée automatiquement depuis le contexte (header
     * Accept-Language).
     * 
     * @param key    clé du message dans le fichier .properties
     * @param params paramètres dynamiques à insérer dans le message ({0}, {1},
     *               etc.)
     * @return message formaté avec les paramètres
     * 
     * @example
     * 
     *          <pre>
     *          get("error.user.notfound", "john@example.com")
     *          // Retourne : "Utilisateur non trouvé : john@example.com" (si locale=fr)
     *          // Retourne : "User not found: john@example.com" (si locale=en)
     *          </pre>
     */
    public String get(String key, Object... params) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, params, currentLocale);
    }

    /**
     * Récupère un message avec une locale spécifique.
     * Utile pour forcer une langue indépendamment du contexte.
     * 
     * @param key    clé du message
     * @param locale langue souhaitée
     * @param params paramètres dynamiques
     * @return message formaté
     * 
     * @example
     * 
     *          <pre>
     * get("email.welcome.subject", Locale.FRENCH)
     * // Retourne : "Bienvenue sur SendMe !"
     * 
     * get("email.welcome.subject", Locale.ENGLISH)
     * // Retourne : "Welcome to SendMe!"
     *          </pre>
     */
    public String get(String key, Locale locale, Object... params) {
        return messageSource.getMessage(key, params, locale);
    }

    /**
     * Récupère un message avec valeur par défaut si la clé n'existe pas.
     * 
     * @param key          clé du message
     * @param defaultValue valeur à retourner si la clé n'existe pas
     * @param params       paramètres dynamiques
     * @return message formaté ou valeur par défaut
     */
    public String getOrDefault(String key, String defaultValue, Object... params) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, params, defaultValue, currentLocale);
    }

    /**
     * Récupère la locale courante depuis le contexte Spring.
     * 
     * @return Locale courante (déterminée par Accept-Language header)
     */
    public Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Vérifie si la locale courante est le français.
     * 
     * @return true si la locale est française
     */
    public boolean isFrench() {
        return Locale.FRENCH.getLanguage().equals(getCurrentLocale().getLanguage());
    }

    /**
     * Vérifie si la locale courante est l'anglais.
     * 
     * @return true si la locale est anglaise
     */
    public boolean isEnglish() {
        return Locale.ENGLISH.getLanguage().equals(getCurrentLocale().getLanguage());
    }
}
