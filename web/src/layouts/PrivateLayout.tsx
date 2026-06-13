import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { X } from 'lucide-react';
import Sidebar from '@/components/layout/Sidebar';
import Header from '@/components/layout/Header';

/**
 * Map des chemins vers leur titre affiché dans le Header.
 * Étendre cette map au fur et à mesure de l'ajout des pages.
 */
const PAGE_TITLES: Record<string, string> = {
    '/dashboard': 'Tableau de bord',
    '/transfer': 'Nouveau transfert',
    '/beneficiaries': 'Bénéficiaires',
    '/history': 'Historique',
    '/notifications': 'Notifications',
    '/profile': 'Mon profil',
};

/**
 * Layout pour les pages privées (utilisateur connecté).
 * Sidebar fixe à gauche (desktop) + Header en haut + contenu scrollable.
 * La sidebar se transforme en panneau coulissant sur mobile.
 */
function PrivateLayout() {
    const [mobileOpen, setMobileOpen] = useState(false);
    const location = useLocation();
    const title = PAGE_TITLES[location.pathname] ?? '';

    return (
        <div className="flex h-screen overflow-hidden bg-gray-50">
            {/* Sidebar desktop */}
            <Sidebar variant="user" />

            {/* Sidebar mobile (overlay) */}
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
                        <Sidebar variant="user" />
                    </div>
                </div>
            )}

            {/* Contenu principal */}
            <div className="flex flex-1 flex-col overflow-hidden">
                <Header title={title} onMenuClick={() => setMobileOpen(true)} />
                <main className="flex-1 overflow-y-auto p-4 md:p-6">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}

export default PrivateLayout;