import type { ChannelType, TransactionStatus, NotificationType } from '@/types';
import type { TFunction } from 'i18next';

export const STRIPE_PUBLIC_KEY = import.meta.env.VITE_STRIPE_PUBLIC_KEY;
export const API_BASE_URL = 'http://localhost:8080/api';

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
export function getChannelLabel(channel: ChannelType, t: TFunction): string {
    return t(`channels.${channel}`);
}

/**
 * Détermine la devise de réception selon le canal.
 */
export function getCurrencyForChannel(channel: ChannelType): string {
    if (channel === 'BANK_TRANSFER') return 'EUR';
    if (channel === 'ORANGE_MONEY' || channel === 'MTN_MOBILE_MONEY') return 'GNF';
    return 'XOF';
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
