import { cn } from '@/lib/utils';

export interface ProgressProps {
    value: number;
    size?: 'sm' | 'md' | 'lg';
    variant?: 'brand' | 'success' | 'warning' | 'danger';
    label?: string;
    showValue?: boolean;
    className?: string;
}

const sizeMap = {
    sm: 'h-1.5',
    md: 'h-2.5',
    lg: 'h-4',
};

const variantMap = {
    brand: 'bg-brand-600 dark:bg-brand-500',
    success: 'bg-accent-500 dark:bg-accent-400',
    warning: 'bg-warning-500 dark:bg-warning-400',
    danger: 'bg-danger-500 dark:bg-danger-400',
};

/**
 * Barre de progression.
 *
 * @example
 * <Progress value={75} size="md" variant="brand" showValue />
 * <Progress value={40} size="sm" label="Téléchargement..." />
 */
function Progress({ value, size = 'md', variant = 'brand', label, showValue, className }: ProgressProps) {
    const clampedValue = Math.min(100, Math.max(0, value));

    return (
        <div className={cn('flex flex-col gap-1', className)}>
            {(label || showValue) && (
                <div className="flex items-center justify-between">
                    {label && <span className="text-xs font-medium text-gray-600 dark:text-gray-300">{label}</span>}
                    {showValue && <span className="text-xs font-medium text-gray-500 dark:text-gray-300">{Math.round(clampedValue)}%</span>}
                </div>
            )}
            <div className={cn('w-full overflow-hidden rounded-full bg-gray-100 dark:bg-[#333333]', sizeMap[size])}>
                <div
                    className={cn(
                        'h-full rounded-full transition-all duration-500 ease-out',
                        variantMap[variant]
                    )}
                    style={{ width: `${clampedValue}%` }}
                />
            </div>
        </div>
    );
}

export default Progress;
