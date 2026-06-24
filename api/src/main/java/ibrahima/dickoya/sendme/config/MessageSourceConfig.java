package ibrahima.dickoya.sendme.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * Configuration de l'internationalisation (i18n) pour les messages backend.
 * 
 * Cette configuration permet d'externaliser tous les messages de l'application
 * (erreurs, notifications, logs) dans des fichiers .properties par langue.
 * 
 * Fichiers de messages :
 * - messages_fr.properties : français (langue par défaut)
 * - messages_en.properties : anglais
 * 
 * La langue est déterminée automatiquement selon le header HTTP
 * Accept-Language.
 */
@Configuration
public class MessageSourceConfig {

    /**
     * Configure le MessageSource pour charger les messages internationalisés.
     * 
     * @return MessageSource configuré avec les fichiers de messages
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();

        // Nom de base des fichiers de messages (sans extension ni locale)
        messageSource.setBasename("classpath:messages");

        // Encodage UTF-8 pour supporter les caractères accentués
        messageSource.setDefaultEncoding("UTF-8");

        // Langue par défaut si aucune locale n'est spécifiée
        messageSource.setDefaultLocale(Locale.FRENCH);

        // Durée du cache en secondes (-1 = cache permanent en production)
        // En dev, mettre 0 pour recharger les fichiers à chaque fois
        messageSource.setCacheSeconds(3600); // 1 heure

        // Comportement si une clé n'existe pas
        messageSource.setUseCodeAsDefaultMessage(true); // Retourne la clé si message introuvable

        return messageSource;
    }

    /**
     * Configure le résolveur de locale basé sur le header Accept-Language.
     * 
     * @return LocaleResolver configuré
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.FRENCH);
        return localeResolver;
    }
}
