import { createContext, useContext, useState, useCallback } from 'react';
import Toast from '../../components/common/Toast';

/**
 * Context global pour afficher des notifications "toast" éphémères.
 *
 * Usage dans n'importe quel composant :
 *   const { showToast } = useToast();
 *   showToast('Transfert effectué avec succès', 'success');
 *
 * Types disponibles : success, error, warning, info
 */
const ToastContext = createContext(null);

export function ToastProvider({ children }) {
    const [toasts, setToasts] = useState([]);

    /**
     * Affiche un nouveau toast et le retire automatiquement après 4 secondes.
     */
    const showToast = useCallback((message, type = 'info') => {
        const id = Date.now();
        setToasts((prev) => [...prev, { id, message, type }]);

        setTimeout(() => {
            setToasts((prev) => prev.filter((t) => t.id !== id));
        }, 4000);
    }, []);

    const removeToast = (id) => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
    };

    return (
        <ToastContext.Provider value={{ showToast }}>
            {children}
            <div className="toast-container">
                {toasts.map((toast) => (
                    <Toast key={toast.id} {...toast} onClose={() => removeToast(toast.id)} />
                ))}
            </div>
        </ToastContext.Provider>
    );
}

/**
 * Hook pour accéder au système de toast depuis n'importe quel composant.
 */
export function useToast() {
    const context = useContext(ToastContext);
    if (!context) {
        throw new Error('useToast doit être utilisé dans un ToastProvider');
    }
    return context;
}