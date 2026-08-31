import { useEffect } from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Scale } from 'lucide-react';
import LanguageSwitcher from '@/components/layout/LanguageSwitcher';
import ThemeToggle from '@/components/layout/ThemeToggle';
import { useTheme } from '@/store/theme/themeContext';
import { useAuth } from '@/store/auth/authContext';

function PublicLayout() {
    const { t } = useTranslation('common');
    const location = useLocation();
    const { setTheme } = useTheme();
    const navigate = useNavigate();
    const { isAuthenticated, logout } = useAuth();
    const isLegalPage = location.pathname === '/legal';
    const isAuthPage = location.pathname === '/login' || location.pathname === '/register';
    const isContactPage = location.pathname === '/contact';
    const isAboutPage = location.pathname === '/about';
    const isWidePage = isLegalPage || isContactPage || isAboutPage;

    useEffect(() => {
        if (isAuthPage) {
            setTheme('light');
        }
    }, [isAuthPage, setTheme]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="flex min-h-screen flex-col bg-gray-50 dark:bg-[#0D0D0D]">

            {/* Header de navigation */}
            <header className="sticky top-0 z-50 border-b border-gray-100 bg-white/80 backdrop-blur-lg dark:border-[#222222] dark:bg-[#0D0D0D]/80">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    {/* Logo */}
                    <Link to="/" className="flex items-center gap-2">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white shadow-[0_0_12px_rgba(90,159,224,0.3)] dark:bg-brand-500 dark:shadow-[0_0_16px_rgba(90,159,224,0.4)]">
                            S
                        </div>
                        <span className="text-lg font-bold text-gray-900 dark:text-[#EDEEF0]">
                            {t('app.name')}
                        </span>
                    </Link>

                    {/* Navigation centrale */}
                    <nav className="hidden items-center gap-6 md:flex">
                        <Link
                            to="/"
                            className="text-sm font-medium text-gray-600 transition-colors hover:text-brand-600 dark:text-[#8B8FA3] dark:hover:text-brand-400"
                        >
                            {t('nav.home', 'Accueil')}
                        </Link>
                        <Link
                            to="/contact"
                            className={`text-sm font-medium transition-colors ${
                                isContactPage
                                    ? 'text-brand-600 dark:text-brand-400'
                                    : 'text-gray-600 hover:text-brand-600 dark:text-[#8B8FA3] dark:hover:text-brand-400'
                            }`}
                        >
                            {t('nav.contact', 'Contact')}
                        </Link>
                        <Link
                            to="/legal"
                            className={`text-sm font-medium transition-colors ${
                                isLegalPage
                                    ? 'text-brand-600 dark:text-brand-400'
                                    : 'text-gray-600 hover:text-brand-600 dark:text-[#8B8FA3] dark:hover:text-brand-400'
                            }`}
                        >
                            <span className="inline-flex items-center gap-1.5">
                                <Scale size={14} />
                                {t('nav.legal', 'Informations légales')}
                            </span>
                        </Link>
                    </nav>

                    {/* Actions */}
                    <div className="flex items-center gap-3">
                        <ThemeToggle />
                        <LanguageSwitcher />
                        {isAuthenticated ? (
                            <>
                                <Link
                                    to="/dashboard"
                                    className="hidden rounded-xl bg-brand-600 px-4 py-2 text-sm font-medium text-white transition-all hover:bg-brand-700 sm:inline-flex dark:bg-brand-500 dark:hover:bg-brand-600"
                                >
                                    {t('nav.dashboard', 'Tableau de bord')}
                                </Link>
                                <button
                                    type="button"
                                    onClick={handleLogout}
                                    className="hidden rounded-xl border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50 sm:inline-flex dark:border-[#333333] dark:text-[#C0C4D0] dark:hover:bg-[#1A1A1A]"
                                >
                                    {t('nav.logout', 'Déconnexion')}
                                </button>
                            </>
                        ) : (
                            <>
                                <Link
                                    to="/login"
                                    className="hidden rounded-xl bg-brand-600 px-4 py-2 text-sm font-medium text-white transition-all hover:bg-brand-700 sm:inline-flex dark:bg-brand-500 dark:hover:bg-brand-600"
                                >
                                    {t('nav.login', 'Connexion')}
                                </Link>
                                <Link
                                    to="/register"
                                    className="hidden rounded-xl border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700 transition-all hover:bg-gray-50 sm:inline-flex dark:border-[#333333] dark:text-[#C0C4D0] dark:hover:bg-[#1A1A1A]"
                                >
                                    {t('nav.register', 'S\'inscrire')}
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            </header>

            {/* Contenu principal */}
            <main className={`flex-1 ${isWidePage ? '' : 'flex items-center justify-center px-4 pb-12'}`}>
                <Outlet />
            </main>
        </div>
    );
}

export default PublicLayout;
