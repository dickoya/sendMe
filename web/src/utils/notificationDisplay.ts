import {
    Send, CheckCircle2, XCircle, RotateCcw, CreditCard,
    UserPlus, UserX, UserCheck, ShieldAlert, ShieldCheck,
    Info, Wrench, DollarSign,
} from 'lucide-react';
import type { NotificationType } from '@/types';

interface NotificationDisplay {
    icon: typeof Send;
    /** Classes Tailwind pour le fond + couleur de l'icône */
    className: string;
}

/**
 * Mapping NotificationType → icône lucide + couleur Tailwind.
 * Utilisé par NotificationItem pour un affichage cohérent.
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