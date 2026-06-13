import { BrowserRouter } from 'react-router-dom';
import { ToastProvider } from '@/store/notification/toastContext';
import { AuthProvider } from '@/store/auth/authContext';
import AppRoutes from '@/routes/AppRoutes';

/**
 * Composant racine de l'application.
 *
 * Ordre des providers :
 * 1. BrowserRouter — active le routing
 * 2. AuthProvider  — état d'authentification (utilisé par les guards)
 * 3. ToastProvider — notifications globales
 */
function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <ToastProvider>
                    <AppRoutes />
                </ToastProvider>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;