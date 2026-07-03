import { type ReactNode, useState } from 'react';
import { cn } from '@/lib/utils';

export interface TooltipProps {
    content: string;
    children: ReactNode;
    position?: 'top' | 'bottom' | 'left' | 'right';
    className?: string;
}

const positionClasses = {
    top: 'bottom-full left-1/2 -translate-x-1/2 mb-2',
    bottom: 'top-full left-1/2 -translate-x-1/2 mt-2',
    left: 'right-full top-1/2 -translate-y-1/2 mr-2',
    right: 'left-full top-1/2 -translate-y-1/2 ml-2',
};

const arrowClasses = {
    top: 'top-full left-1/2 -translate-x-1/2 border-l-transparent border-r-transparent border-b-transparent border-t-gray-900',
    bottom: 'bottom-full left-1/2 -translate-x-1/2 border-l-transparent border-r-transparent border-t-transparent border-b-gray-900',
    left: 'left-full top-1/2 -translate-y-1/2 border-t-transparent border-b-transparent border-r-transparent border-l-gray-900',
    right: 'right-full top-1/2 -translate-y-1/2 border-t-transparent border-b-transparent border-l-transparent border-r-gray-900',
};

/**
 * Infobulle stylisée qui apparaît au survol.
 *
 * @example
 * <Tooltip content="Supprimer">
 *   <button><Trash2 size={16} /></button>
 * </Tooltip>
 */
function Tooltip({ content, children, position = 'top', className }: TooltipProps) {
    const [isVisible, setIsVisible] = useState(false);

    return (
        <div
            className={cn('relative inline-flex', className)}
            onMouseEnter={() => setIsVisible(true)}
            onMouseLeave={() => setIsVisible(false)}
            onFocus={() => setIsVisible(true)}
            onBlur={() => setIsVisible(false)}
        >
            {children}
            {isVisible && content && (
                <div
                    className={cn(
                        'absolute z-50 whitespace-nowrap rounded-md bg-gray-900 px-2.5 py-1.5 text-xs font-medium text-white shadow-lg dark:bg-[#EDEEF0] dark:text-gray-900',
                        'animate-in fade-in duration-150',
                        positionClasses[position]
                    )}
                >
                    {content}
                    <div
                        className={cn(
                            'absolute h-0 w-0 border-4',
                            arrowClasses[position]
                        )}
                    />
                </div>
            )}
        </div>
    );
}

export default Tooltip;
