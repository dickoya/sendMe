import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '@/store/auth/authContext';
import Spinner from '@/components/ui/Spinner';

/**
 * Protège les routes réservées à ROLE_ADMIN.
 * - Redirige vers /login si non authentifié
 * - Redirige vers /dashboard si authentifié mais pas admin
 */
function AdminRoute() {
    const { isAuthenticated, isLoading, user } = useAuth();

    if (isLoading) {
        return <Spinner fullPage label="Chargement..." />;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (user?.role !== 'ROLE_ADMIN') {
        return <Navigate to="/dashboard" replace />;
    }

    return <Outlet />;
}

export default AdminRoute;