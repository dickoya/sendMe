import type { ChannelType, TransactionStatus, NotificationType } from '@/types';
import type { TFunction } from 'i18next';

export const STRIPE_PUBLIC_KEY = import.meta.env.VITE_STRIPE_PUBLIC_KEY;
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Compte Stripe attendu (préfixe de compte extrait des clés Stripe).
 * Utilisé par stripeService pour REFUSER toute clé publique d'un AUTRE compte —
 * empêche un bundle frontend construit avec une ancienne clé (web/.env modifié
 * sans relance/rebuild) d'envoyer des pm_ du mauvais compte au backend.
 * À adapter uniquement si le compte Stripe du projet change.
 */
export const EXPECTED_STRIPE_ACCOUNT = '1U15b7';

/**
 * Icônes par canal — statiques, indépendantes de la langue.
 */
export const CHANNEL_ICONS: Record<ChannelType, string> = {
    BANK_TRANSFER: '🏦',
    ORANGE_MONEY: '🟠',
    MTN_MOBILE_MONEY: '🟡',
    WAVE: '🔵',
    MOOV_MONEY: '🟢',
    AIRTEL_MONEY: '🔴',
};

/**
 * Mapping statut → variante de Badge Tailwind.
 */
export const STATUS_VARIANT: Record<TransactionStatus, 'success' | 'warning' | 'danger' | 'info' | 'purple'> = {
    PENDING: 'warning',
    PROCESSING: 'info',
    COMPLETED: 'success',
    FAILED: 'danger',
    REFUNDED: 'purple',
};

export const STORAGE_KEYS = {
    ACCESS_TOKEN: 'sendme_access_token',
    REFRESH_TOKEN: 'sendme_refresh_token',
    USER: 'sendme_user',
} as const;

export const DEFAULT_PAGE_SIZE = 10;

export const NOTIFICATION_TYPES: Record<string, NotificationType> = {
    TRANSFER_INITIATED: 'TRANSFER_INITIATED',
    TRANSFER_COMPLETED: 'TRANSFER_COMPLETED',
    TRANSFER_FAILED: 'TRANSFER_FAILED',
    TRANSFER_REFUNDED: 'TRANSFER_REFUNDED',
    PAYMENT_CONFIRMED: 'PAYMENT_CONFIRMED',
    PAYMENT_FAILED: 'PAYMENT_FAILED',
};

/**
 * Retourne l'icône d'un canal.
 */
export function getChannelIcon(channel: ChannelType): string {
    return CHANNEL_ICONS[channel];
}

/**
 * Retourne le libellé traduit d'un canal depuis les fichiers i18n locales.
 */
export function getChannelLabel(channel: ChannelType | string | undefined, t: TFunction): string {
    if (!channel) {
        return t('channels.UNKNOWN', 'Canal inconnu');
    }

    const label = t(`channels.${channel}`);
    if (label === `channels.${channel}`) {
        return t('channels.UNKNOWN', 'Canal inconnu');
    }

    return label;
}

/**
 * Mapping pays → devise.
 */
export const COUNTRY_CURRENCY: Record<string, string> = {
    // Afrique de l'Ouest - UEMOA (XOF)
    BEN: 'XOF', BFA: 'XOF', CIV: 'XOF', GNB: 'XOF',
    MLI: 'XOF', NER: 'XOF', SEN: 'XOF', TGO: 'XOF',
    // Afrique centrale - CEMAC (XAF)
    CMR: 'XAF', CAF: 'XAF', TCD: 'XAF', COG: 'XAF',
    GNQ: 'XAF', GAB: 'XAF',
    // Guinée
    GIN: 'GNF',
    // Europe
    FRA: 'EUR', BEL: 'EUR', DEU: 'EUR', ESP: 'EUR',
    ITA: 'EUR', PRT: 'EUR', NLD: 'EUR', LUX: 'EUR',
    CHE: 'CHF', GBR: 'GBP',
    // Amérique
    USA: 'USD', CAN: 'CAD',
    // Autres
    MAR: 'MAD', DZA: 'DZD', TUN: 'TND', EGY: 'EGP', ZAF: 'ZAR',
};

/**
 * Liste des pays avec leurs indicatifs téléphoniques et codes.
 */
export const COUNTRIES = [
    { code: 'GIN', name: 'Guinée', dialCode: '+224', currency: 'GNF' },
    { code: 'SEN', name: 'Sénégal', dialCode: '+221', currency: 'XOF' },
    { code: 'CIV', name: "Côte d'Ivoire", dialCode: '+225', currency: 'XOF' },
    { code: 'MLI', name: 'Mali', dialCode: '+223', currency: 'XOF' },
    { code: 'BFA', name: 'Burkina Faso', dialCode: '+226', currency: 'XOF' },
    { code: 'BEN', name: 'Bénin', dialCode: '+229', currency: 'XOF' },
    { code: 'TGO', name: 'Togo', dialCode: '+228', currency: 'XOF' },
    { code: 'NER', name: 'Niger', dialCode: '+227', currency: 'XOF' },
    { code: 'CMR', name: 'Cameroun', dialCode: '+237', currency: 'XAF' },
    { code: 'FRA', name: 'France', dialCode: '+33', currency: 'EUR' },
    { code: 'BEL', name: 'Belgique', dialCode: '+32', currency: 'EUR' },
    { code: 'MAR', name: 'Maroc', dialCode: '+212', currency: 'MAD' },
    { code: 'DZA', name: 'Algérie', dialCode: '+213', currency: 'DZD' },
    { code: 'TUN', name: 'Tunisie', dialCode: '+216', currency: 'TND' },
    { code: 'USA', name: 'États-Unis', dialCode: '+1', currency: 'USD' },
    { code: 'CAN', name: 'Canada', dialCode: '+1', currency: 'CAD' },
];

/**
 * Détermine la devise de réception selon le canal (fallback si pas de pays).
 */
export function getCurrencyForChannel(channel: ChannelType): string {
    if (channel === 'BANK_TRANSFER') return 'EUR';
    if (channel === 'ORANGE_MONEY' || channel === 'MTN_MOBILE_MONEY') return 'GNF';
    return 'XOF';
}

/**
 * Retourne la devise pour un code pays donné.
 */
export function getCurrencyForCountry(countryCode?: string): string {
    if (!countryCode) return 'EUR';
    return COUNTRY_CURRENCY[countryCode.toUpperCase()] || 'EUR';
}

/**
 * Retourne le libellé traduit d'un statut de transaction.
 */
export function getStatusLabel(status: string, t: TFunction): string {
    return t(`status.${status}`);
}

/**
 * Langues disponibles dans l'application.
 */
export const LANGUAGES = [
    { code: 'fr', label: 'Français', flag: 'fr' },
    { code: 'en', label: 'English', flag: 'gb' },
] as const;

export type LanguageCode = (typeof LANGUAGES)[number]['code'];
