import { Routes, Route } from 'react-router-dom';

// Layouts
import PublicLayout from '@/layouts/PublicLayout';
import PrivateLayout from '@/layouts/PrivateLayout';
import AdminLayout from '@/layouts/AdminLayout';

// Guards
import PrivateRoute from './PrivateRoute';
import AdminRoute from './AdminRoute';

// Pages publiques
import Home from '@/pages/public/Home';
import Login from '@/pages/public/Login';
import Register from '@/pages/public/Register';
import EmailVerification from '@/pages/public/EmailVerification';
import VerificationSent from '@/pages/public/VerificationSent';
import LegalNotice from '@/pages/public/LegalNotice';

// Pages privées
import Dashboard from '@/pages/private/Dashboard';
import Transfer from '@/pages/private/Transfer';
import Beneficiaries from '@/pages/private/Beneficiaries';
import History from '@/pages/private/History';
import Notifications from '@/pages/private/Notifications';
import Profile from '@/pages/private/Profile';
import ReferralPage from '@/pages/private/ReferralPage';
import Faq from '@/pages/private/Faq';

// Pages admin
import AdminDashboard from '@/pages/admin/AdminDashboard';
import AdminUsers from '@/pages/admin/AdminUsers';
import AdminTransactions from '@/pages/admin/AdminTransactions';
import AdminLogs from '@/pages/admin/AdminLogs';
import AdminSettings from '@/pages/admin/AdminSettings';
import AdminPaymentChannels from '@/pages/admin/AdminPaymentChannels';

// Divers
import NotFound from '@/pages/NotFound';

/**
 * Arborescence des routes de l'application.
 *
 * Structure :
 * - "/"  redirige vers /dashboard
 * - Routes publiques (Login, Register) → PublicLayout, accessibles sans JWT
 * - Routes privées (/dashboard, /transfer, ...)  PrivateLayout, protégées par PrivateRoute
 * - Routes admin (/admin/*) → AdminLayout, protégées par AdminRoute (ROLE_ADMIN)
 * - "*"  page 404
 */
function AppRoutes() {
    return (
        <Routes>
            {/* ── Page d'accueil publique ──────────────────────────────────── */}
            <Route path="/" element={<Home />} />

            {/* ── Routes de vérification d'email (pages autonomes, sans layout) ── */}
            <Route path="/verify-email" element={<EmailVerification />} />
            <Route path="/verify-email-sent" element={<VerificationSent />} />

            {/* ── Routes publiques (Login, Register, Legal) ──────────────────── */}
            <Route element={<PublicLayout />}>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/legal" element={<LegalNotice />} />
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
                    <Route path="/referral" element={<ReferralPage />} />
                    <Route path="/faq" element={<Faq />} />
                    <Route path="/legal" element={<LegalNotice />} />
                </Route>
            </Route>

            {/* ── Routes admin (ROLE_ADMIN) ───────────────────────────────────── */}
            <Route element={<AdminRoute />}>
                <Route element={<AdminLayout />}>
                    <Route path="/admin" element={<AdminDashboard />} />
                    <Route path="/admin/users" element={<AdminUsers />} />
                    <Route path="/admin/transactions" element={<AdminTransactions />} />
                    <Route path="/admin/logs" element={<AdminLogs />} />
                    <Route path="/admin/settings" element={<AdminSettings />} />
                    <Route path="/admin/payment-channels" element={<AdminPaymentChannels />} />
                </Route>
            </Route>

            {/* ── 404 ──────────────────────────────────────────────────────────── */}
            <Route path="*" element={<NotFound />} />
        </Routes>
    );
}

export default AppRoutes;
