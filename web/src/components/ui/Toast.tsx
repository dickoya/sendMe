import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';
import { cn } from '@/lib/utils';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastProps {
    message: string;
    type?: ToastType;
    onClose: () => void;
}

const ICONS: Record<ToastType, React.ReactNode> = {
    success: <CheckCircle size={18} />,
    error: <XCircle size={18} />,
    warning: <AlertTriangle size={18} />,
    info: <Info size={18} />,
};

const STYLES: Record<ToastType, string> = {
    success: 'bg-accent-50 text-accent-600 border-accent-400 dark:bg-accent-900/30 dark:text-accent-400 dark:border-accent-600',
    error: 'bg-danger-50 text-danger-600 border-danger-500 dark:bg-danger-900/30 dark:text-danger-400 dark:border-danger-600',
    warning: 'bg-warning-50 text-warning-600 border-warning-500 dark:bg-warning-900/30 dark:text-warning-400 dark:border-warning-600',
    info: 'bg-brand-50 text-brand-600 border-brand-400 dark:bg-brand-900/30 dark:text-brand-400 dark:border-brand-600',
};

/**
 * Notification éphémère individuelle (gérée par ToastProvider).
 */
function Toast({ message, type = 'info', onClose }: ToastProps) {
    return (
        <div
            className={cn(
                'flex min-w-[280px] max-w-[360px] items-center gap-2 rounded-lg border px-3.5 py-3 text-sm shadow-md animate-in slide-in-from-right duration-200',
                STYLES[type]
            )}
        >
            <span className="flex-shrink-0">{ICONS[type]}</span>
            <span className="flex-1">{message}</span>
            <button onClick={onClose} aria-label="Fermer" className="flex-shrink-0 opacity-60 hover:opacity-100">
                <X size={14} />
            </button>
        </div>
    );
}

export default Toast;
