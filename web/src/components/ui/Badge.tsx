import { type HTMLAttributes } from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

const badgeVariants = cva(
    'inline-flex items-center rounded-full font-medium whitespace-nowrap',
    {
        variants: {
            variant: {
                success: 'bg-accent-50 text-accent-600 dark:bg-accent-900/30 dark:text-accent-400',
                danger: 'bg-danger-50 text-danger-600 dark:bg-danger-900/30 dark:text-danger-400',
                warning: 'bg-warning-50 text-warning-600 dark:bg-warning-900/30 dark:text-warning-400',
                info: 'bg-brand-50 text-brand-600 dark:bg-brand-900/30 dark:text-brand-400',
                purple: 'bg-purple-50 text-purple-600 dark:bg-purple-900/30 dark:text-purple-400',
                gray: 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-400',
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
