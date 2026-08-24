import { BrowserRouter } from 'react-router-dom';
import { ToastProvider } from '@/store/notification/toastContext';
//import { NotificationProvider } from '@/store/notification/notificationContext';
import { NotificationProvider } from '@/store/notification/notificationContext.tsx'
import { AuthProvider } from '@/store/auth/authContext';
import { ThemeProvider } from '@/store/theme/themeContext';
import AppRoutes from '@/routes/AppRoutes';
import CookieConsent from '@/components/layout/CookieConsent';

/**
 * Composant racine de l'application.
 *
 * Ordre des providers (important) :
 * 1. BrowserRouter      — routing
 * 2. ThemeProvider       — thème sombre/clair (doit être avant les composants visuels)
 * 3. AuthProvider       — état d'authentification (utilisé par les guards et SSE)
 * 4. ToastProvider      — notifications globales (utilisé par NotificationProvider)
 * 5. NotificationProvider — historique + SSE temps réel (dépend de Auth + Toast)
 */
function App() {
    return (
        <BrowserRouter>
            <ThemeProvider>
                <AuthProvider>
                    <ToastProvider>
                        <NotificationProvider>
                            <AppRoutes />
                            <CookieConsent />
                        </NotificationProvider>
                    </ToastProvider>
                </AuthProvider>
            </ThemeProvider>
        </BrowserRouter>
    );
}

export default App;
