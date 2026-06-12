/**
 * Configuration i18next pour SendMe.
 *
 * Pour l'instant, seul le français est implémenté.
 * La structure permet d'ajouter facilement en/es/nl
 * en créant simplement les fichiers de traduction correspondants
 * dans locales/{lang}/translation.json et en les important ici.
 */
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import fr from './locales/fr/translation.json';

i18n
    .use(initReactI18next)
    .init({
        resources: {
            fr: { translation: fr },
        },
        lng: 'fr',
        fallbackLng: 'fr',
        interpolation: {
            escapeValue: false,
        },
    });

export default i18n;