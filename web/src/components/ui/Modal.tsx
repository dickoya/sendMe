import { type ReactNode } from 'react';
import { X } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    size?: 'sm' | 'md' | 'lg';
    footer?: ReactNode;
    children: ReactNode;
}

const sizeMap = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-2xl',
};

/**
 * Fenêtre modale centrée avec overlay.
 *
 * @example
 * <Modal isOpen={open} onClose={() => setOpen(false)} title="Confirmer">
 *   <p>Êtes-vous sûr ?</p>
 * </Modal>
 */
function Modal({ isOpen, onClose, title, size = 'md', footer, children }: ModalProps) {
    if (!isOpen) return null;

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/45 p-4 animate-in fade-in duration-150 dark:bg-black/60"
            onClick={onClose}
        >
            <div
                className={cn(
                    'flex max-h-[90vh] w-full flex-col rounded-2xl bg-white shadow-xl animate-in slide-in-from-bottom-4 duration-200 dark:bg-[#141414] dark:border dark:border-[#333333]',
                    sizeMap[size]
                )}
                onClick={(e) => e.stopPropagation()}
            >
                {title && (
                    <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4 dark:border-[#222222]">
                        <h3 className="text-lg font-semibold text-gray-900 dark:text-[#EDEEF0]">{title}</h3>
                        <button
                            onClick={onClose}
                            aria-label="Fermer"
                            className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 dark:text-[#5A5E72] dark:hover:bg-[#222222] dark:hover:text-[#8B8FA3]"
                        >
                            <X size={18} />
                        </button>
                    </div>
                )}

                <div className="overflow-y-auto p-5">{children}</div>

                {footer && (
                    <div className="flex justify-end gap-2 border-t border-gray-100 p-5 dark:border-[#222222]">{footer}</div>
                )}
            </div>

        </div>
    );
}

export default Modal;
