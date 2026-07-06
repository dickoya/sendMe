import { AlertTriangle } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import Modal from './Modal';
import Button from './Button';

export interface ConfirmDialogProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    title: string;
    message: string | React.ReactNode;
    confirmLabel?: string;
    cancelLabel?: string;
    variant?: 'danger' | 'primary';
    isLoading?: boolean;
}

/**
 * Boîte de dialogue de confirmation standardisée.
 *
 * @example
 * <ConfirmDialog
 *   isOpen={!!deleting}
 *   onClose={() => setDeleting(null)}
 *   onConfirm={confirmDelete}
 *   title={t('modals.deleteTitle')}
 *   message={t('modals.deleteMessage')}
 *   variant="danger"
 *   isLoading={isDeleting}
 * />
 */
function ConfirmDialog({
    isOpen,
    onClose,
    onConfirm,
    title,
    message,
    confirmLabel,
    cancelLabel,
    variant = 'primary',
    isLoading,
}: ConfirmDialogProps) {
    const { t } = useTranslation('common');

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={title}
            size="sm"
            footer={
                <div className="flex gap-2">
                    <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
                        {cancelLabel ?? t('actions.cancel')}
                    </Button>
                    <button
                        type="button"
                        onClick={onConfirm}
                        disabled={isLoading}
                        className="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg font-medium transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 active:scale-[0.97] h-10 px-4 text-sm bg-brand-600 text-white shadow-sm hover:bg-brand-700 focus-visible:ring-brand-500 dark:bg-brand-500 dark:hover:bg-brand-400"
                    >
                        {isLoading && <span className="h-4 w-4 animate-spin border-2 border-white border-t-transparent rounded-full mr-2" />}
                        {confirmLabel ?? t('actions.confirm')}
                    </button>
                </div>
            }
        >
            <div className="flex flex-col gap-3">
                {variant === 'danger' && (
                    <div className="flex items-center gap-2 rounded-lg bg-danger-50 p-3 text-sm text-danger-600 dark:bg-danger-900/20 dark:text-danger-400">
                        <AlertTriangle size={16} className="flex-shrink-0" />
                        {typeof message === 'string' ? <p>{message}</p> : message}
                    </div>
                )}
                {variant !== 'danger' && typeof message === 'string' && (
                    <p className="text-sm text-gray-600 dark:text-gray-300">{message}</p>
                )}
                {variant !== 'danger' && typeof message !== 'string' && message}
            </div>
        </Modal>
    );
}

export default ConfirmDialog;
