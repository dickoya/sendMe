import { type HTMLAttributes } from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

const badgeVariants = cva(
    'inline-flex items-center rounded-full font-medium whitespace-nowrap',
    {
        variants: {
            variant: {
                success: 'bg-accent-50 text-accent-600',
                danger: 'bg-danger-50 text-danger-600',
                warning: 'bg-warning-50 text-warning-600',
                info: 'bg-brand-50 text-brand-600',
                purple: 'bg-purple-50 text-purple-600',
                gray: 'bg-gray-100 text-gray-600',
            },
            size: {
                sm: 'px-2 py-0.5 text-[10px]',
                md: 'px-3 py-1 text-xs',
            },
        },
        defaultVariants: {
            variant: 'gray',
            size: 'md',
        },
    }
);

export interface BadgeProps
    extends HTMLAttributes<HTMLSpanElement>,
        VariantProps<typeof badgeVariants> {}

/**
 * Étiquette colorée pour afficher un statut.
 *
 * @example
 * <Badge variant={STATUS_VARIANT[transaction.status]}>
 *   {STATUS_LABELS[transaction.status]}
 * </Badge>
 */
function Badge({ className, variant, size, ...props }: BadgeProps) {
    return <span className={cn(badgeVariants({ variant, size }), className)} {...props} />;
}

export default Badge;