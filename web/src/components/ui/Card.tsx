import { type HTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
    title?: string;
    actions?: React.ReactNode;
    noPadding?: boolean;
}

/**
 * Conteneur de carte avec en-tête optionnel.
 *
 * @example
 * <Card title="Mes bénéficiaires" actions={<Button size="sm">Ajouter</Button>}>
 *   ...
 * </Card>
 */
function Card({ title, actions, noPadding, className, children, ...props }: CardProps) {
    return (
        <div
            className={cn(
                'rounded-2xl border border-gray-100 bg-white shadow-[0_1px_3px_rgba(0,0,0,0.08),0_1px_2px_rgba(0,0,0,0.06)] transition-all duration-200',
                'dark:border-[#222222] dark:bg-[#141414] dark:shadow-[0_1px_2px_rgba(0,0,0,0.3),0_1px_3px_rgba(0,0,0,0.2)]',
                'hover:shadow-[0_4px_12px_rgba(0,0,0,0.1),0_2px_4px_rgba(0,0,0,0.06)]',
                'dark:hover:shadow-[0_4px_12px_rgba(0,0,0,0.4),0_0_0_1px_rgba(90,159,224,0.08)]',
                className
            )}
            {...props}
        >
            {title && (
                    <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4 dark:border-[#222222]">
                    <h3 className="text-base font-semibold text-gray-900 dark:text-[#EDEEF0]">{title}</h3>
                    {actions && <div className="flex gap-2">{actions}</div>}
                </div>
            )}
            <div className={cn(!noPadding && 'p-5')}>{children}</div>
        </div>
    );
}


export default Card;
