/**
 * Constantes globales de l'application SendMe.
 *
 * Centralise toutes les valeurs partagées entre composants
 * pour rester synchronisé avec les enums du backend Java.
 */

// ── URL de base de l'API ──────────────────────────────────────────────
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// ── Clé publique Stripe ───────────────────────────────────────────────
export const STRIPE_PUBLIC_KEY = import.meta.env.VITE_STRIPE_PUBLIC_KEY;

// ── Canaux de réception (synchronisé avec enums.ChannelType) ─────────
export const CHANNEL_TYPES = {
    BANK_TRANSFER: 'BANK_TRANSFER',
    ORANGE_MONEY: 'ORANGE_MONEY',
    MTN_MOBILE_MONEY: 'MTN_MOBILE_MONEY',
    WAVE: 'WAVE',
    MOOV_MONEY: 'MOOV_MONEY',
    AIRTEL_MONEY: 'AIRTEL_MONEY',
};

// ── Labels affichés pour chaque canal ─────────────────────────────────
export const CHANNEL_LABELS = {
    BANK_TRANSFER: 'Virement bancaire',
    ORANGE_MONEY: 'Orange Money',
    MTN_MOBILE_MONEY: 'MTN Mobile Money',
    WAVE: 'Wave',
    MOOV_MONEY: 'Moov Money',
    AIRTEL_MONEY: 'Airtel Money',
};

// ── Icônes (emoji) pour chaque canal ──────────────────────────────────
export const CHANNEL_ICONS = {
    BANK_TRANSFER: '🏦',
    ORANGE_MONEY: '🟠',
    MTN_MOBILE_MONEY: '🟡',
    WAVE: '🌊',
    MOOV_MONEY: '📱',
    AIRTEL_MONEY: '📡',
};

// ── Statuts de transaction (synchronisé avec enums.TransactionStatus) ─
export const TRANSACTION_STATUS = {
    PENDING: 'PENDING',
    PROCESSING: 'PROCESSING',
    COMPLETED: 'COMPLETED',
    FAILED: 'FAILED',
    REFUNDED: 'REFUNDED',
};

// ── Labels affichés pour chaque statut ────────────────────────────────
export const STATUS_LABELS = {
    PENDING: 'En attente',
    PROCESSING: 'En cours',
    COMPLETED: 'Complétée',
    FAILED: 'Échouée',
    REFUNDED: 'Remboursée',
};

// ── Couleurs associées à chaque statut (classes CSS) ──────────────────
export const STATUS_COLORS = {
    PENDING: 'warning',
    PROCESSING: 'info',
    COMPLETED: 'success',
    FAILED: 'danger',
    REFUNDED: 'purple',
};

// ── Types de notifications (synchronisé avec enums.NotificationType) ──
export const NOTIFICATION_TYPES = {
    TRANSFER_INITIATED: 'TRANSFER_INITIATED',
    TRANSFER_PROCESSING: 'TRANSFER_PROCESSING',
    TRANSFER_COMPLETED: 'TRANSFER_COMPLETED',
    TRANSFER_FAILED: 'TRANSFER_FAILED',
    TRANSFER_REFUNDED: 'TRANSFER_REFUNDED',
    PAYMENT_CONFIRMED: 'PAYMENT_CONFIRMED',
    PAYMENT_FAILED: 'PAYMENT_FAILED',
    ACCOUNT_CREATED: 'ACCOUNT_CREATED',
    ACCOUNT_DISABLED: 'ACCOUNT_DISABLED',
    ACCOUNT_REACTIVATED: 'ACCOUNT_REACTIVATED',
    SECURITY_LOGIN: 'SECURITY_LOGIN',
    SECURITY_PASSWORD_CHANGED: 'SECURITY_PASSWORD_CHANGED',
    SECURITY_SUSPICIOUS_LOGIN: 'SECURITY_SUSPICIOUS_LOGIN',
    INFO: 'INFO',
    MAINTENANCE: 'MAINTENANCE',
    FEE_UPDATE: 'FEE_UPDATE',
};

// ── Rôles utilisateur (synchronisé avec enums.Role) ───────────────────
export const ROLES = {
    USER: 'ROLE_USER',
    ADMIN: 'ROLE_ADMIN',
};

// ── Clés de stockage local ─────────────────────────────────────────────
export const STORAGE_KEYS = {
    ACCESS_TOKEN: 'sendme_access_token',
    REFRESH_TOKEN: 'sendme_refresh_token',
    USER: 'sendme_user',
};

// ── Pagination par défaut ──────────────────────────────────────────────
export const DEFAULT_PAGE_SIZE = 10;