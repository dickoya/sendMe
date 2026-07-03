import { type ReactNode } from 'react';
import { cn } from '@/lib/utils';

export interface EmptyStateProps {
    icon?: ReactNode;
    title: string;
    message?: string;
    actionLabel?: string;
    actionTo?: string;
    onAction?: () => void;
    className?: string;
}

/**
 * État vide standardisé pour les listes, tableaux et sections.
 *
 * @example
 * <EmptyState
 *   icon={<Users size={22} />}
 *   title={t('empty.title')}
 *   message={t('empty.message')}
 *   actionLabel={t('empty.addAction')}
 *   onAction={() => setFormOpen(true)}
 * />
 */
function EmptyState({ icon, title, message, actionLabel, onAction, className }: EmptyStateProps) {
    return (
        <div className={cn('flex flex-col items-center gap-2 py-12 text-center', className)}>
            {icon && (
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400 dark:bg-[#1A1A1A] dark:text-[#555555]">
                    {icon}
                </div>
            )}
            <p className="font-medium text-gray-900 dark:text-[#EDEEF0]">{title}</p>
            {message && <p className="text-sm text-gray-500 dark:text-[#8B8FA3] max-w-md">{message}</p>}
            {actionLabel && (
                <button
                    onClick={onAction}
                    className="mt-1 text-sm font-medium text-brand-600 hover:underline dark:text-brand-400"
                >
                    {actionLabel}
                </button>
            )}
        </div>
    );
}

export default EmptyState;
