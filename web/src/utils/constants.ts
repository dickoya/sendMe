import type { ChannelType, TransactionStatus, NotificationType } from '@/types';
import type { TFunction } from 'i18next';

//export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
export const STRIPE_PUBLIC_KEY = import.meta.env.VITE_STRIPE_PUBLIC_KEY;
export const API_BASE_URL = 'http://localhost:8080/api';

export const CHANNEL_LABELS: Record<ChannelType, string> = {
    BANK_TRANSFER: 'Virement bancaire',
    ORANGE_MONEY: 'Orange Money',
    MTN_MOBILE_MONEY: 'MTN Mobile Money',
    WAVE: 'Wave',
    MOOV_MONEY: 'Moov Money',
    AIRTEL_MONEY: 'Airtel Money',
};

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

export const STATUS_LABELS: Record<TransactionStatus, string> = {
    PENDING: 'En attente',
    PROCESSING: 'En cours',
    COMPLETED: 'Complétée',
    FAILED: 'Échouée',
    REFUNDED: 'Remboursée',
};

/**
 * Mapping statut → variante de Badge Tailwind (voir Étape 2).
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
 * Retourne l'icône d'un canal. Fonction (plutôt qu'accès direct à
 * CHANNEL_ICONS) pour garder une API symétrique avec getChannelLabel
 * dans les composants qui affichent icône + libellé ensemble.
 */
export function getChannelIcon(channel: ChannelType): string {
    return CHANNEL_ICONS[channel];
}

/**
 * Retourne le libellé traduit d'un canal.
 *
 * @param channel Le type de canal
 * @param t       Fonction de traduction du namespace "common"
 *                (useTranslation('common').t)
 *
 * @example
 * const { t } = useTranslation('common');
 * getChannelLabel('ORANGE_MONEY', t) // "Orange Money" (identique FR/EN)
 * getChannelLabel('BANK_TRANSFER', t) // "Virement bancaire" ou "Bank transfer"
 */
export function getChannelLabel(channel: ChannelType, t: TFunction): string {
    return t(`channels.${channel}`);
}

/**
 * Détermine la devise de réception selon le canal,
 * miroir de TransferService.getCurrencyForChannel() côté backend.
 */
export function getCurrencyForChannel(channel: ChannelType): string {
    return channel === 'BANK_TRANSFER' ? 'EUR' : 'XOF';
}

/**
 * Retourne le libellé traduit d'un statut de transaction.
 *
 * @example
 * const { t } = useTranslation('common');
 * getStatusLabel('COMPLETED', t) // "Complétée" ou "Completed"
 */
export function getStatusLabel(status: string, t: TFunction): string {
    return t(`status.${status}`);
}

/**
 * Langues disponibles dans l'application.
 *
 * Utilisé par le sélecteur de langue (Phase 3) et par la configuration
 * i18next (src/i18n/config.ts — supportedLngs doit rester synchronisé
 * avec cette liste).
 *
 * Pour ajouter une langue : voir le commentaire dans i18n/config.ts.
 */
export const LANGUAGES = [
    { code: 'fr', label: 'Français', flag: '🇫🇷' },
    { code: 'en', label: 'English', flag: '🇬🇧' },
] as const;

export type LanguageCode = (typeof LANGUAGES)[number]['code'];