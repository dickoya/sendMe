import { useState } from 'react';
import { Outlet, useLocation, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Scale } from 'lucide-react';
import Sidebar from '@/components/layout/Sidebar';
import Header from '@/components/layout/Header';

/**
 * Layout des pages privées (utilisateur connecté).
 *
 * Le titre affiché dans le Header est déterminé dynamiquement selon
 * la route, via les clés du namespace "common" (nav.*) — garantit la
 * cohérence avec les libellés de la Sidebar.
 */
function PrivateLayout() {
    const { t } = useTranslation('common');
    const location = useLocation();
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const pageTitle = getPageTitle(location.pathname, t);

    return (
        <div className="flex min-h-screen bg-gray-50 dark:bg-[#0D0D0D]">

            <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} variant="user" />

            <div className="flex flex-1 flex-col">
                <Header title={pageTitle} onMenuClick={() => setSidebarOpen(true)} />
                <main className="flex-1 p-4 md:p-6">
                    <Outlet />
                </main>

                {/* Footer */}
                <footer className="border-t border-gray-100 bg-white px-6 py-4 dark:border-[#222222] dark:bg-[#0D0D0D]">
                    <div className="flex flex-col items-center justify-between gap-2 sm:flex-row">
                        <p className="text-xs text-gray-400 dark:text-[#5A5E72]">
                            &copy; {new Date().getFullYear()} SendMe. {t('footer.rights')}
                        </p>
                        <Link
                            to="/legal"
                            className="inline-flex items-center gap-1.5 text-xs text-gray-400 transition-colors hover:text-brand-600 dark:text-[#5A5E72] dark:hover:text-brand-400"
                        >
                            <Scale size={12} />
                            {t('nav.legal', 'Mentions légales')}
                        </Link>
                    </div>
                </footer>
            </div>
        </div>
    );
}

/**
 * Détermine le titre de page à afficher dans le Header selon la route.
 * Réutilise les clés de navigation (common.nav.*) pour éviter la
 * duplication de libellés.
 */
function getPageTitle(pathname: string, t: (key: string) => string): string {
    if (pathname.startsWith('/dashboard')) return t('nav.dashboard');
    if (pathname.startsWith('/beneficiaries')) return t('nav.beneficiaries');
    if (pathname.startsWith('/transfer')) return t('nav.transfer');
    if (pathname.startsWith('/history')) return t('nav.history');
    if (pathname.startsWith('/notifications')) return t('nav.notifications');
    if (pathname.startsWith('/faq')) return t('nav.faq');
    return '';
}

export default PrivateLayout;
