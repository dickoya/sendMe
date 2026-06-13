import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/store/auth/authContext';
import Spinner from '@/components/ui/Spinner';

/**
 * Protège les routes nécessitant une authentification.
 * Redirige vers /login si l'utilisateur n'est pas connecté.
 */
function PrivateRoute() {
    const { isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return <Spinner fullPage label="Chargement..." />;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
}

export default PrivateRoute;