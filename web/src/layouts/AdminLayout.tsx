import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { X } from 'lucide-react';
import Sidebar from '@/components/layout/Sidebar';
import Header from '@/components/layout/Header';

const ADMIN_PAGE_TITLES: Record<string, string> = {
    '/admin': 'Vue d\'ensemble',
    '/admin/users': 'Gestion des utilisateurs',
    '/admin/transactions': 'Toutes les transactions',
    '/admin/settings': 'Paramètres de la plateforme',
};

/**
 * Layout pour les pages d'administration (ROLE_ADMIN).
 * Même structure que PrivateLayout mais avec le menu admin.
 */
function AdminLayout() {
    const [mobileOpen, setMobileOpen] = useState(false);
    const location = useLocation();
    const title = ADMIN_PAGE_TITLES[location.pathname] ?? 'Administration';

    return (
        <div className="flex h-screen overflow-hidden bg-gray-50">
            <Sidebar variant="admin" />

            {mobileOpen && (
                <div className="fixed inset-0 z-40 flex md:hidden">
                    <div className="absolute inset-0 bg-black/40" onClick={() => setMobileOpen(false)} />
                    <div className="relative z-50 w-60 bg-white shadow-xl">
                        <button
                            onClick={() => setMobileOpen(false)}
                            className="absolute right-3 top-3 rounded-md p-1.5 text-gray-500 hover:bg-gray-100"
                            aria-label="Fermer le menu"
                        >
                            <X size={18} />
                        </button>
                        <Sidebar variant="admin" />
                    </div>
                </div>
            )}

            <div className="flex flex-1 flex-col overflow-hidden">
                <Header title={title} onMenuClick={() => setMobileOpen(true)} />
                <main className="flex-1 overflow-y-auto p-4 md:p-6">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default AdminLayout;