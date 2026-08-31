import { lazy, Suspense } from 'react';
import { Routes, Route } from 'react-router-dom';
import Spinner from '@/components/ui/Spinner';

// Layouts (chargés immédiatement — légers, nécessaires au rendu)
import PublicLayout from '@/layouts/PublicLayout';
import PrivateLayout from '@/layouts/PrivateLayout';
import AdminLayout from '@/layouts/AdminLayout';

// Guards
import PrivateRoute from './PrivateRoute';
import AdminRoute from './AdminRoute';

// Pages publiques (chargées à la demande — code splitting)
const Home = lazy(() => import('@/pages/public/Home'));
const Login = lazy(() => import('@/pages/public/Login'));
const Register = lazy(() => import('@/pages/public/Register'));
const EmailVerification = lazy(() => import('@/pages/public/EmailVerification'));
const VerificationSent = lazy(() => import('@/pages/public/VerificationSent'));
const ForgotPassword = lazy(() => import('@/pages/public/ForgotPassword'));
const ResetPassword = lazy(() => import('@/pages/public/ResetPassword'));
const LegalNotice = lazy(() => import('@/pages/public/LegalNotice'));
const Contact = lazy(() => import('@/pages/public/Contact'));
const About = lazy(() => import('@/pages/public/About'));

// Pages privées
const Dashboard = lazy(() => import('@/pages/private/Dashboard'));
const Transfer = lazy(() => import('@/pages/private/Transfer'));
const Beneficiaries = lazy(() => import('@/pages/private/Beneficiaries'));
const History = lazy(() => import('@/pages/private/History'));
const Notifications = lazy(() => import('@/pages/private/Notifications'));
const Profile = lazy(() => import('@/pages/private/Profile'));
const ReferralPage = lazy(() => import('@/pages/private/ReferralPage'));
const Faq = lazy(() => import('@/pages/private/Faq'));

// Pages admin
const AdminDashboard = lazy(() => import('@/pages/admin/AdminDashboard'));
const AdminUsers = lazy(() => import('@/pages/admin/AdminUsers'));
const AdminTransactions = lazy(() => import('@/pages/admin/AdminTransactions'));
const AdminLogs = lazy(() => import('@/pages/admin/AdminLogs'));
const AdminSettings = lazy(() => import('@/pages/admin/AdminSettings'));
const AdminPaymentChannels = lazy(() => import('@/pages/admin/AdminPaymentChannels'));

// Divers
const NotFound = lazy(() => import('@/pages/NotFound'));

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
        <Suspense
            fallback={<Spinner fullPage label="Chargement de la page..." />}
        >
            <Routes>
                {/* Page d'accueil publique */}
                <Route path="/" element={<Home />} />

                {/* Routes de vérification d'email (pages autonomes, sans layout) */}
                <Route path="/verify-email" element={<EmailVerification />} />
                <Route path="/verify-email-sent" element={<VerificationSent />} />

                {/* Routes de réinitialisation de mot de passe (pages autonomes, sans layout) */}
                <Route path="/forgot-password" element={<ForgotPassword />} />
                <Route path="/reset-password" element={<ResetPassword />} />

                {/* Routes publiques (Login, Register, Legal, Contact) */}
                <Route element={<PublicLayout />}>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/legal" element={<LegalNotice />} />
                    <Route path="/contact" element={<Contact />} />
                    <Route path="/about" element={<About />} />
                </Route>

                {/* Routes privées (utilisateur connecté) */}
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
                    </Route>
                </Route>

                {/* Routes admin (ROLE_ADMIN) */}
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

                {/* 404 */}
                <Route path="*" element={<NotFound />} />
            </Routes>
        </Suspense>
    );
}

export default AppRoutes;
