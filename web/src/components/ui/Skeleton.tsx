import { cn } from '@/lib/utils';

export interface SkeletonProps {
    variant?: 'text' | 'circular' | 'rectangular';
    width?: string | number;
    height?: string | number;
    count?: number;
    className?: string;
}

/**
 * Placeholder de chargement (skeleton) pour les états de loading.
 *
 * @example
 * <Skeleton variant="text" width="200px" />
 * <Skeleton variant="circular" width={40} height={40} />
 * <Skeleton variant="rectangular" height={100} count={3} />
 */
function Skeleton({ variant = 'text', width, height, count = 1, className }: SkeletonProps) {
    const baseClass = 'animate-pulse bg-gray-200 dark:bg-[#333333]';

    const variantClass = {
        text: 'h-4 rounded',
        circular: 'rounded-full',
        rectangular: 'rounded-lg',
    };

    const style: React.CSSProperties = {};
    if (width) style.width = typeof width === 'number' ? `${width}px` : width;
    if (height) style.height = typeof height === 'number' ? `${height}px` : height;
    if (variant === 'text' && !height) style.height = '16px';
    if (variant === 'text' && !width) style.width = '100%';

    const items = Array.from({ length: count }, (_, i) => i);

    return (
        <>
            {items.map((i) => (
                <div
                    key={i}
                    className={cn(baseClass, variantClass[variant], className)}
                    style={style}
                />
            ))}
        </>
    );
}

export default Skeleton;
