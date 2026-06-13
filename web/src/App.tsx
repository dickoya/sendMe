import { BrowserRouter } from 'react-router-dom';
import { ToastProvider } from '@/store/notification/toastContext';
//import { NotificationProvider } from '@/store/notification/notificationContext';
import { NotificationProvider } from '@/store/notification/notificationContext.tsx'
import { AuthProvider } from '@/store/auth/authContext';
import AppRoutes from '@/routes/AppRoutes';

/**
 * Composant racine de l'application.
 *
 * Ordre des providers (important) :
 * 1. BrowserRouter      — routing
 * 2. AuthProvider       — état d'authentification (utilisé par les guards et SSE)
 * 3. ToastProvider      — notifications globales (utilisé par NotificationProvider)
 * 4. NotificationProvider — historique + SSE temps réel (dépend de Auth + Toast)
 */
function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <ToastProvider>
                    <NotificationProvider>
                        <AppRoutes />
                    </NotificationProvider>
                </ToastProvider>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;