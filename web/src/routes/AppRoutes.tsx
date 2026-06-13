import { Routes, Route, Navigate } from 'react-router-dom';

// Layouts
import PublicLayout from '@/layouts/PublicLayout';
import PrivateLayout from '@/layouts/PrivateLayout';
import AdminLayout from '@/layouts/AdminLayout';

// Guards
import PrivateRoute from './PrivateRoute';
import AdminRoute from './AdminRoute';

// Pages publiques
import Login from '@/pages/public/Login';
import Register from '@/pages/public/Register';

// Pages privées
import Dashboard from '@/pages/private/Dashboard';
import Transfer from '@/pages/private/Transfer';
import Beneficiaries from '@/pages/private/Beneficiaries';
import History from '@/pages/private/History';
import Notifications from '@/pages/private/Notifications';
import Profile from '@/pages/private/Profile';

// Pages admin
import AdminDashboard from '@/pages/admin/AdminDashboard';
import AdminUsers from '@/pages/admin/AdminUsers';
import AdminTransactions from '@/pages/admin/AdminTransactions';
import AdminSettings from '@/pages/admin/AdminSettings';

// Divers
import NotFound from '@/pages/NotFound';

/**
 * Arborescence des routes de l'application.
 *
 * Structure :
 * - "/" → redirige vers /dashboard
 * - Routes publiques (Login, Register) → PublicLayout, accessibles sans JWT
 * - Routes privées (/dashboard, /transfer, ...) → PrivateLayout, protégées par PrivateRoute
 * - Routes admin (/admin/*) → AdminLayout, protégées par AdminRoute (ROLE_ADMIN)
 * - "*" → page 404
 */
function AppRoutes() {
    return (
        <Routes>
            {/* Redirection racine */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            {/* ── Routes publiques ──────────────────────────────────────────── */}
            <Route element={<PublicLayout />}>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
            </Route>

            {/* ── Routes privées (utilisateur connecté) ───────────────────────── */}
            <Route element={<PrivateRoute />}>
                <Route element={<PrivateLayout />}>
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/transfer" element={<Transfer />} />
                    <Route path="/beneficiaries" element={<Beneficiaries />} />
                    <Route path="/history" element={<History />} />
                    <Route path="/notifications" element={<Notifications />} />
                    <Route path="/profile" element={<Profile />} />
                </Route>
            </Route>

            {/* ── Routes admin (ROLE_ADMIN) ───────────────────────────────────── */}
            <Route element={<AdminRoute />}>
                <Route element={<AdminLayout />}>
                    <Route path="/admin" element={<AdminDashboard />} />
                    <Route path="/admin/users" element={<AdminUsers />} />
                    <Route path="/admin/transactions" element={<AdminTransactions />} />
                    <Route path="/admin/settings" element={<AdminSettings />} />
                </Route>
            </Route>

            {/* ── 404 ──────────────────────────────────────────────────────────── */}
            <Route path="*" element={<NotFound />} />
        </Routes>
    );
}

export default AppRoutes;