import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// FR
import commonFr from './locales/fr/common.json';
import authFr from './locales/fr/auth.json';
import dashboardFr from './locales/fr/dashboard.json';
import beneficiariesFr from './locales/fr/beneficiaries.json';
import transferFr from './locales/fr/transfer.json';
import historyFr from './locales/fr/history.json';
import notificationsFr from './locales/fr/notifications.json';
import adminFr from './locales/fr/admin.json';
import homeFr from './locales/fr/home.json';

// EN
import commonEn from './locales/en/common.json';
import authEn from './locales/en/auth.json';
import dashboardEn from './locales/en/dashboard.json';
import beneficiariesEn from './locales/en/beneficiaries.json';
import transferEn from './locales/en/transfer.json';
import historyEn from './locales/en/history.json';
import notificationsEn from './locales/en/notifications.json';
import adminEn from './locales/en/admin.json';
import homeEn from './locales/en/home.json';

/**
 * Liste des namespaces disponibles dans l'application.
 *
 * Pour ajouter un nouveau domaine (ex: "settings"), créer
 * locales/fr/settings.json + locales/en/settings.json,
 * les importer ci-dessus, les ajouter dans `resources`,
 * et ajouter "settings" à ce tableau.
 */
export const NAMESPACES = [
    'common',
    'auth',
    'dashboard',
    'beneficiaries',
    'transfer',
    'history',
    'notifications',
    'admin',
    'home',
] as const;

/**
 * Configuration i18next — application bilingue FR/EN.
 *
 * - LanguageDetector détecte la langue du navigateur au premier
 *   chargement (si aucune préférence n'est encore enregistrée)
 * - La préférence de l'utilisateur est ensuite persistée dans
 *   localStorage (clé "sendme_language") et prioritaire sur la
 *   détection navigateur lors des visites suivantes
 * - fallbackLng: 'fr' si la langue détectée/sauvegardée n'est pas
 *   supportée (ex: navigateur en allemand)
 *
 * Pour ajouter une langue future (ex: "es") :
 * 1. Créer locales/es/*.json (mêmes clés que fr/en)
 * 2. Importer chaque fichier ci-dessus
 * 3. Ajouter "es: { common: commonEs, ... }" dans `resources`
 * 4. Ajouter "es" dans `supportedLngs`
 * 5. Ajouter l'entrée correspondante dans LANGUAGES (utils/constants.ts)
 */
i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
        resources: {
            fr: {
                common: commonFr,
                auth: authFr,
                dashboard: dashboardFr,
                beneficiaries: beneficiariesFr,
                transfer: transferFr,
                history: historyFr,
                notifications: notificationsFr,
                admin: adminFr,
                home: homeFr,
            },
            en: {
                common: commonEn,
                auth: authEn,
                dashboard: dashboardEn,
                beneficiaries: beneficiariesEn,
                transfer: transferEn,
                history: historyEn,
                notifications: notificationsEn,
                admin: adminEn,
                home: homeEn,
            },
        },
        fallbackLng: 'fr',
        supportedLngs: ['fr', 'en'],
        ns: NAMESPACES,
        defaultNS: 'common',
        interpolation: {
            escapeValue: false, // React échappe déjà le contenu — éviter le double échappement
        },
        returnNull: false,
        detection: {
            order: ['localStorage', 'navigator'],
            caches: ['localStorage'],
            lookupLocalStorage: 'sendme_language',
        },
        debug: false,
    });

export default i18n;
