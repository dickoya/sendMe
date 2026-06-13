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
                'rounded-2xl border border-gray-100 bg-white shadow-sm',
                className
            )}
            {...props}
        >
            {title && (
                <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4">
                    <h3 className="text-base font-semibold text-gray-900">{title}</h3>
                    {actions && <div className="flex gap-2">{actions}</div>}
                </div>
            )}
            <div className={cn(!noPadding && 'p-5')}>{children}</div>
        </div>
    );
}

export default Card;