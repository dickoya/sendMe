import { type ReactNode } from 'react';
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';
import { cn } from '@/lib/utils';

export type AlertType = 'success' | 'error' | 'warning' | 'info';

export interface AlertProps {
    type?: AlertType;
    message: string;
    onClose?: () => void;
    icon?: ReactNode;
    className?: string;
}

const DEFAULT_ICONS: Record<AlertType, ReactNode> = {
    success: <CheckCircle size={18} />,
    error: <XCircle size={18} />,
    warning: <AlertTriangle size={18} />,
    info: <Info size={18} />,
};

const STYLES: Record<AlertType, string> = {
    success: 'bg-accent-50 text-accent-700 border-accent-200 dark:bg-accent-900/20 dark:text-accent-400 dark:border-accent-800',
    error: 'bg-danger-50 text-danger-700 border-danger-200 dark:bg-danger-900/20 dark:text-danger-400 dark:border-danger-800',
    warning: 'bg-warning-50 text-warning-700 border-warning-200 dark:bg-warning-900/20 dark:text-warning-400 dark:border-warning-800',
    info: 'bg-brand-50 text-brand-700 border-brand-200 dark:bg-brand-900/20 dark:text-brand-400 dark:border-brand-800',
};

/**
 * Bannière d'alerte colorée pour les messages d'erreur, succès, avertissement ou info.
 *
 * @example
 * <Alert type="error" message={errorMessage} onClose={() => setError(null)} />
 * <Alert type="success" message="Opération réussie" />
 */
function Alert({ type = 'info', message, onClose, icon, className }: AlertProps) {
    return (
        <div
            className={cn(
                'flex items-center gap-2 rounded-lg border px-3.5 py-3 text-sm',
                STYLES[type],
                className
            )}
        >
            <span className="flex-shrink-0">
                {icon ?? DEFAULT_ICONS[type]}
            </span>
            <span className="flex-1">{message}</span>
            {onClose && (
                <button
                    onClick={onClose}
                    aria-label="Fermer"
                    className="flex-shrink-0 opacity-60 hover:opacity-100 transition-opacity"
                >
                    <X size={14} />
                </button>
            )}
        </div>
    );
}

export default Alert;
