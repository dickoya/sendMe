import {
    Send, CheckCircle2, XCircle, RotateCcw, CreditCard,
    UserPlus, UserX, UserCheck, ShieldAlert, ShieldCheck,
    Info, Wrench, DollarSign,
} from 'lucide-react';
import type { TFunction } from 'i18next';
import type { Notification, NotificationType } from '@/types';

interface NotificationDisplay {
    icon: typeof Send;
    /** Classes Tailwind pour le fond + couleur de l'icône */
    className: string;
}

/**
 * Mapping NotificationType → icône lucide + couleur Tailwind.
 * Purement visuel — indépendant de la langue, pas de migration nécessaire.
 */
export const NOTIFICATION_DISPLAY: Record<NotificationType, NotificationDisplay> = {
    TRANSFER_INITIATED: { icon: Send, className: 'bg-brand-50 text-brand-600' },
    TRANSFER_PROCESSING: { icon: Send, className: 'bg-warning-50 text-warning-600' },
    TRANSFER_COMPLETED: { icon: CheckCircle2, className: 'bg-accent-50 text-accent-600' },
    TRANSFER_FAILED: { icon: XCircle, className: 'bg-danger-50 text-danger-600' },
    TRANSFER_REFUNDED: { icon: RotateCcw, className: 'bg-purple-50 text-purple-600' },
    PAYMENT_CONFIRMED: { icon: CreditCard, className: 'bg-accent-50 text-accent-600' },
    PAYMENT_FAILED: { icon: CreditCard, className: 'bg-danger-50 text-danger-600' },
    ACCOUNT_CREATED: { icon: UserPlus, className: 'bg-brand-50 text-brand-600' },
    ACCOUNT_DISABLED: { icon: UserX, className: 'bg-danger-50 text-danger-600' },
    ACCOUNT_REACTIVATED: { icon: UserCheck, className: 'bg-accent-50 text-accent-600' },
    SECURITY_LOGIN: { icon: ShieldCheck, className: 'bg-brand-50 text-brand-600' },
    SECURITY_PASSWORD_CHANGED: { icon: ShieldCheck, className: 'bg-brand-50 text-brand-600' },
    SECURITY_SUSPICIOUS_LOGIN: { icon: ShieldAlert, className: 'bg-danger-50 text-danger-600' },
    INFO: { icon: Info, className: 'bg-gray-100 text-gray-500' },
    MAINTENANCE: { icon: Wrench, className: 'bg-warning-50 text-warning-600' },
    FEE_UPDATE: { icon: DollarSign, className: 'bg-warning-50 text-warning-600' },
};

/**
 * Types de notification dont le message contient une référence
 * de transaction (TXN-XXXXXXXX-XXXX) interpolable via {{reference}}.
 */
const TYPES_WITH_REFERENCE: NotificationType[] = [
    'TRANSFER_INITIATED',
    'TRANSFER_PROCESSING',
    'TRANSFER_COMPLETED',
    'TRANSFER_FAILED',
    'TRANSFER_REFUNDED',
    'PAYMENT_CONFIRMED',
    'PAYMENT_FAILED',
];

/**
 * Extrait une référence de transaction (format TXN-XXXXXXXX-XXXX)
 * d'une chaîne, ou retourne null si absente.
 */
function extractReference(message: string): string | null {
    const match = message.match(/TXN-\d{8}-\d{4}/);
    return match ? match[0] : null;
}

/**
 * Reconstruit le message d'affichage d'une notification dans la langue
 * active, à partir de son type et — si pertinent — d'une référence de
 * transaction extraite du message brut backend (toujours en français,
 * mais le format de référence TXN-... est indépendant de la langue).
 *
 * Option A du plan i18n (Phase 10) : ignore `notification.message`
 * (texte FR backend) sauf pour l'extraction de données structurées
 * (référence). Le texte affiché provient entièrement de
 * notifications.json (FR/EN).
 *
 * @example
 * getNotificationMessage(notif, t)
 * // FR: "Transfert TXN-20260614-0007 complété avec succès"
 * // EN: "Transfer TXN-20260614-0007 completed successfully"
 */
export function getNotificationMessage(notification: Notification, t: TFunction): string {
    const { type, message } = notification;

    if (TYPES_WITH_REFERENCE.includes(type)) {
        const reference = extractReference(message);
        if (reference) {
            return t(`types.${type}`, { reference });
        }
        // Référence non trouvée — message générique sans interpolation
        // (la clé contient {{reference}}, on retire la partie référence
        // en passant une chaîne vide pour éviter d'afficher "{{reference}}" brut)
        return t(`types.${type}`, { reference: '' }).replace(/\s*$/, '').trim();
    }

    if (type === 'MAINTENANCE') {
        // Date non extraite de façon fiable depuis le texte FR backend —
        // message générique sans date précise (limitation documentée).
        return t('types.MAINTENANCE', { date: '' }).replace(/\s*$/, '').trim();
    }

    // Types sans interpolation (ACCOUNT_*, SECURITY_*, INFO, FEE_UPDATE)
    return t(`types.${type}`);
}