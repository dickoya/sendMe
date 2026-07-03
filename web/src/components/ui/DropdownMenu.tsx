import { useState, useRef, useEffect, type ReactNode } from 'react';
import { cn } from '@/lib/utils';

export interface DropdownItem {
    label: string;
    icon?: ReactNode;
    onClick: () => void;
    variant?: 'default' | 'danger';
    disabled?: boolean;
}

export interface DropdownMenuProps {
    trigger: ReactNode;
    items: DropdownItem[];
    align?: 'left' | 'right';
    className?: string;
}

/**
 * Menu contextuel déroulant.
 *
 * @example
 * <DropdownMenu
 *   trigger={<Button variant="ghost"><MoreVertical size={16} /></Button>}
 *   items={[
 *     { label: 'Modifier', icon: <Pencil size={14} />, onClick: handleEdit },
 *     { label: 'Supprimer', icon: <Trash2 size={14} />, onClick: handleDelete, variant: 'danger' },
 *   ]}
 * />
 */
function DropdownMenu({ trigger, items, align = 'right', className }: DropdownMenuProps) {
    const [isOpen, setIsOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };

        if (isOpen) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [isOpen]);

    return (
        <div ref={menuRef} className={cn('relative inline-block', className)}>
            <div onClick={() => setIsOpen(!isOpen)}>{trigger}</div>

            {isOpen && (
                <div
                    className={cn(
                        'absolute z-50 mt-1 min-w-[160px] rounded-xl border border-gray-100 bg-white py-1 shadow-lg animate-in fade-in zoom-in-95 duration-150 dark:border-[#333333] dark:bg-[#1A1A1A]',
                        align === 'right' ? 'right-0' : 'left-0'
                    )}
                >
                    {items.map((item, index) => (
                        <button
                            key={index}
                            onClick={() => {
                                if (!item.disabled) {
                                    item.onClick();
                                    setIsOpen(false);
                                }
                            }}
                            disabled={item.disabled}
                            className={cn(
                                'flex w-full items-center gap-2 px-3 py-2 text-sm transition-colors',
                                item.variant === 'danger'
                                    ? 'text-danger-600 hover:bg-danger-50 dark:text-danger-400 dark:hover:bg-danger-900/20'
                                    : 'text-gray-700 hover:bg-gray-50 dark:text-[#8B8FA3] dark:hover:bg-[#222222]',
                                item.disabled && 'opacity-40 cursor-not-allowed'
                            )}
                        >
                            {item.icon && <span className="flex-shrink-0">{item.icon}</span>}
                            {item.label}
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
}

export default DropdownMenu;
