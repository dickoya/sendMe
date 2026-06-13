import type { ChannelType, TransactionStatus, NotificationType } from '@/types';

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

export const CHANNEL_ICONS: Record<ChannelType, string> = {
    BANK_TRANSFER: '🏦',
    ORANGE_MONEY: '🟠',
    MTN_MOBILE_MONEY: '🟡',
    WAVE: '🌊',
    MOOV_MONEY: '📱',
    AIRTEL_MONEY: '📡',
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