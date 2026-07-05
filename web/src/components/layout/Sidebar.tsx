import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
    LayoutDashboard, Users, Send, History, Bell, User, Gift,
    LayoutGrid, UserCog, ArrowRightLeft, Settings, ArrowLeft,
    HelpCircle, ClipboardList,
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
        { to: '/referral', label: t('nav.referral', 'Parrainage'), icon: Gift },
        { to: '/notifications', label: t('nav.notifications'), icon: Bell },
        { to: '/profile', label: t('nav.profile'), icon: User },
        { to: '/faq', label: t('nav.faq'), icon: HelpCircle },
    ];

    const adminLinks = [
        { to: '/admin', label: t('nav.admin.dashboard'), icon: LayoutGrid },
        { to: '/admin/users', label: t('nav.admin.users'), icon: UserCog },
        { to: '/admin/transactions', label: t('nav.admin.transactions'), icon: ArrowRightLeft },
        { to: '/admin/logs', label: t('nav.admin.logs'), icon: ClipboardList },
        { to: '/admin/settings', label: t('nav.admin.settings'), icon: Settings },
    ];

    const links = variant === 'admin' ? adminLinks : userLinks;

    return (
        <>
            {/* Overlay mobile */}
            {isOpen && (
                <div
                    className="fixed inset-0 z-40 bg-black/30 md:hidden dark:bg-black/50"
                    onClick={onClose}
                    aria-hidden="true"
                />
            )}

            <aside
                className={cn(
                    'fixed inset-y-0 left-0 z-50 flex w-64 flex-col border-r border-gray-100 bg-white transition-all duration-300 md:relative md:translate-x-0 dark:border-[#222222] dark:bg-[#0D0D0D]',
                    isOpen ? 'translate-x-0' : '-translate-x-full'
                )}
            >
                {/* Logo */}
                <div className="flex h-16 items-center gap-2 border-b border-gray-100 px-6 dark:border-[#222222]">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white shadow-[0_0_12px_rgba(90,159,224,0.3)] dark:bg-brand-500 dark:shadow-[0_0_16px_rgba(90,159,224,0.4)]">
                        S
                    </div>
                    <span className="text-lg font-bold text-gray-900 dark:text-[#EDEEF0]">{t('app.name')}</span>
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
                                    'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-all duration-200',
                                    isActive
                                        ? 'bg-brand-50 text-brand-700 dark:bg-[#222222] dark:text-brand-400 dark:shadow-[0_0_12px_rgba(90,159,224,0.08)] dark:border dark:border-[#333333]'
                                        : 'text-gray-600 hover:bg-gray-50 dark:text-[#8B8FA3] dark:hover:bg-[#1A1A1A] dark:hover:text-[#EDEEF0]'
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
                    <div className="border-t border-gray-100 p-3 dark:border-[#222222]">
                        <NavLink
                            to="/dashboard"
                            className="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50 dark:text-[#8B8FA3] dark:hover:bg-[#1A1A1A] dark:hover:text-[#EDEEF0]"
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
