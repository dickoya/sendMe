import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Sidebar from '@/components/layout/Sidebar';
import Header from '@/components/layout/Header';

/**
 * Layout des pages privées (utilisateur connecté).
 *
 * Migration i18n (Phase 4) : le titre affiché dans le Header est
 * déterminé dynamiquement selon la route, via les clés du namespace
 * "common" (nav.*) — garantit la cohérence avec les libellés de la Sidebar.
 */
function PrivateLayout() {
    const { t } = useTranslation('common');
    const location = useLocation();
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const pageTitle = getPageTitle(location.pathname, t);

    return (
        <div className="flex min-h-screen bg-gray-50 dark:bg-gray-950">
            <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} variant="user" />

            <div className="flex flex-1 flex-col">
                <Header title={pageTitle} onMenuClick={() => setSidebarOpen(true)} />
                <main className="flex-1 p-4 md:p-6">
                    <Outlet />
                </main>
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
    return '';
}

export default PrivateLayout;
