import { cn } from '@/lib/utils';

export interface AvatarProps {
    firstName: string;
    lastName: string;
    size?: 'sm' | 'md' | 'lg' | 'xl';
    src?: string;
    className?: string;
}

const sizeMap = {
    sm: 'h-8 w-8 text-xs',
    md: 'h-10 w-10 text-sm',
    lg: 'h-12 w-12 text-base',
    xl: 'h-16 w-16 text-2xl',
};

/**
 * Avatar avec initiales ou image.
 *
 * @example
 * <Avatar firstName="John" lastName="Doe" size="md" />
 * <Avatar firstName="John" lastName="Doe" src="/avatar.jpg" size="lg" />
 */
function Avatar({ firstName, lastName, size = 'md', src, className }: AvatarProps) {
    const initials = `${firstName?.[0] ?? ''}${lastName?.[0] ?? ''}`.toUpperCase();

    if (src) {
        return (
            <img
                src={src}
                alt={`${firstName} ${lastName}`}
                className={cn(
                    'flex-shrink-0 rounded-full object-cover',
                    sizeMap[size],
                    className
                )}
            />
        );
    }

    return (
        <div
            className={cn(
                'flex flex-shrink-0 items-center justify-center rounded-full bg-brand-50 font-semibold text-brand-700 dark:bg-brand-900/30 dark:text-brand-400',
                sizeMap[size],
                className
            )}
        >
            {initials}
        </div>
    );
}

export default Avatar;
