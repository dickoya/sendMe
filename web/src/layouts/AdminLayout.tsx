import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Sidebar from '@/components/layout/Sidebar';
import Header from '@/components/layout/Header';

/**
 * Layout des pages d'administration.
 *
 * Migration i18n (Phase 4) : même principe que PrivateLayout,
 * avec les clés common.nav.admin.*.
 */
function AdminLayout() {
    const { t } = useTranslation('common');
    const location = useLocation();
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const pageTitle = getAdminPageTitle(location.pathname, t);

    return (
        <div className="flex min-h-screen bg-gray-50 dark:bg-[#0D0D0D]">

            <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} variant="admin" />

            <div className="flex flex-1 flex-col">
                <Header title={pageTitle} onMenuClick={() => setSidebarOpen(true)} />
                <main className="flex-1 p-4 md:p-6">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

function getAdminPageTitle(pathname: string, t: (key: string) => string): string {
    if (pathname.startsWith('/admin/users')) return t('nav.admin.users');
    if (pathname.startsWith('/admin/transactions')) return t('nav.admin.transactions');
    if (pathname.startsWith('/admin/settings')) return t('nav.admin.settings');
    if (pathname.startsWith('/admin')) return t('nav.admin.dashboard');
    return '';
}

export default AdminLayout;
