import { Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface SpinnerProps {
    size?: 'sm' | 'md' | 'lg';
    label?: string;
    fullPage?: boolean;
    className?: string;
}

const sizeMap = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
};

/**
 * Indicateur de chargement.
 *
 * @example
 * <Spinner fullPage label="Chargement du tableau de bord..." />
 */
function Spinner({ size = 'md', label, fullPage, className }: SpinnerProps) {
    const content = (
        <div className="flex flex-col items-center gap-2">
            <Loader2 className={cn('animate-spin text-brand-600 dark:text-brand-400', sizeMap[size], className)} />
            {label && <span className="text-sm text-gray-500 dark:text-gray-300">{label}</span>}
        </div>
    );

    if (fullPage) {
        return <div className="flex min-h-[60vh] items-center justify-center">{content}</div>;
    }

    return content;
}

export default Spinner;
