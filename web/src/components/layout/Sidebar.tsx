import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
    LayoutDashboard, Users, Send, History, Bell,
    LayoutGrid, UserCog, ArrowRightLeft, Settings, ArrowLeft,
} from 'lucide-react';
import { cn } from '@/lib/utils';

export interface SidebarProps {
    isOpen: boolean;
    onClose: () => void;
    variant: 'user' | 'admin';
}

/**
 * Barre latérale de navigation — variante utilisateur ou admin.
 *
 * Migration i18n (Phase 4) : tous les libellés proviennent du
 * namespace "common" (clés nav.*), partagé avec Header pour
 * cohérence terminologique entre les deux composants.
 */
function Sidebar({ isOpen, onClose, variant }: SidebarProps) {
    const { t } = useTranslation('common');
    console.log("Nom de l'application traduit :", t('app.name'));

    const userLinks = [
        { to: '/dashboard', label: t('nav.dashboard'), icon: LayoutDashboard },
        { to: '/beneficiaries', label: t('nav.beneficiaries'), icon: Users },
        { to: '/transfer', label: t('nav.transfer'), icon: Send },
        { to: '/history', label: t('nav.history'), icon: History },
        { to: '/notifications', label: t('nav.notifications'), icon: Bell },
    ];

    const adminLinks = [
        { to: '/admin', label: t('nav.admin.dashboard'), icon: LayoutGrid },
        { to: '/admin/users', label: t('nav.admin.users'), icon: UserCog },
        { to: '/admin/transactions', label: t('nav.admin.transactions'), icon: ArrowRightLeft },
        { to: '/admin/settings', label: t('nav.admin.settings'), icon: Settings },
    ];

    const links = variant === 'admin' ? adminLinks : userLinks;

    return (
        <>
            {/* Overlay mobile */}
            {isOpen && (
                <div
                    className="fixed inset-0 z-40 bg-black/30 md:hidden"
                    onClick={onClose}
                    aria-hidden="true"
                />
            )}

            <aside
                className={cn(
                    'fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-gray-100 bg-white transition-transform md:relative md:translate-x-0',
                    isOpen ? 'translate-x-0' : '-translate-x-full'
                )}
            >
                {/* Logo */}
                <div className="flex h-16 items-center gap-2 border-b border-gray-100 px-6">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                        S
                    </div>
                    <span className="text-lg font-bold text-gray-900">{t('app.name')}</span>
                </div>

                {/* Navigation */}
                <nav className="flex flex-1 flex-col gap-1 p-3">
                    {links.map(({ to, label, icon: Icon }) => (
                        <NavLink
                            key={to}
                            to={to}
                            onClick={onClose}
                            className={({ isActive }) =>
                                cn(
                                    'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors',
                                    isActive
                                        ? 'bg-brand-50 text-brand-700'
                                        : 'text-gray-600 hover:bg-gray-50'
                                )
                            }
                        >
                            <Icon size={18} />
                            {label}
                        </NavLink>
                    ))}
                </nav>

                {/* Retour au site (admin uniquement) */}
                {variant === 'admin' && (
                    <div className="border-t border-gray-100 p-3">
                        <NavLink
                            to="/dashboard"
                            className="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50"
                        >
                            <ArrowLeft size={18} />
                            {t('nav.backToSite')}
                        </NavLink>
                    </div>
                )}
            </aside>
        </>
    );
}

export default Sidebar;