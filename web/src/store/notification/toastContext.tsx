import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import Toast, { type ToastType } from '@/components/ui/Toast';

interface ToastItem {
    id: number;
    message: string;
    type: ToastType;
}

interface ToastContextValue {
    showToast: (message: string, type?: ToastType) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

/**
 * Provider global pour afficher des notifications toast éphémères.
 * À placer une seule fois, à la racine de l'app (voir App.tsx).
 */
export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<ToastItem[]>([]);

    const showToast = useCallback((message: string, type: ToastType = 'info') => {
        const id = Date.now();
        setToasts((prev) => [...prev, { id, message, type }]);

        setTimeout(() => {
            setToasts((prev) => prev.filter((t) => t.id !== id));
        }, 4000);
    }, []);

    const removeToast = (id: number) => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
    };

    return (
        <ToastContext.Provider value={{ showToast }}>
            {children}
            <div className="fixed right-4 top-4 z-[2000] flex flex-col gap-2">
                {toasts.map((toast) => (
                    <Toast key={toast.id} {...toast} onClose={() => removeToast(toast.id)} />
                ))}
            </div>
        </ToastContext.Provider>
    );
}

/**
 * Hook pour déclencher des toasts depuis n'importe quel composant.
 *
 * @example
 * const { showToast } = useToast();
 * showToast('Transfert effectué avec succès', 'success');
 */
export function useToast() {
    const context = useContext(ToastContext);
    if (!context) throw new Error('useToast doit être utilisé dans un ToastProvider');
    return context;
}