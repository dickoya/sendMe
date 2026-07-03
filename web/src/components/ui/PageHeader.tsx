import { type ReactNode } from 'react';
import { cn } from '@/lib/utils';

export interface PageHeaderProps {
    title: string;
    subtitle?: string;
    actions?: ReactNode;
    variant?: 'default' | 'gradient';
    className?: string;
}

/**
 * En-tête de page standardisé avec titre, sous-titre et actions.
 *
 * @example
 * <PageHeader
 *   title={t('pageTitle')}
 *   subtitle={t('pageSubtitle')}
 *   actions={<Button size="sm">Ajouter</Button>}
 * />
 */
function PageHeader({ title, subtitle, actions, variant = 'default', className }: PageHeaderProps) {
    if (variant === 'gradient') {
        return (
            <div
                className={cn(
                    'rounded-2xl bg-gradient-to-br from-brand-600 to-brand-400 p-6 text-white shadow-[0_4px_15px_rgba(33,97,140,0.3)] dark:from-brand-700 dark:to-brand-500 dark:shadow-[0_0_30px_rgba(90,159,224,0.15)]',
                    className
                )}
            >
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                        <h2 className="text-xl font-bold">{title}</h2>
                        {subtitle && <p className="mt-1 text-sm text-brand-50 dark:text-brand-200">{subtitle}</p>}
                    </div>
                    {actions && <div className="flex-shrink-0">{actions}</div>}
                </div>
            </div>
        );
    }

    return (
        <div className={cn('space-y-1', className)}>
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold text-gray-900 dark:text-[#EDEEF0]">{title}</h1>
                {actions && <div className="flex gap-2">{actions}</div>}
            </div>
            {subtitle && <p className="text-gray-500 dark:text-[#8B8FA3]">{subtitle}</p>}
        </div>
    );
}

export default PageHeader;
