import { NavLink } from 'react-router-dom';
import {
    LayoutDashboard,
    Send,
    Users,
    History,
    Bell,
    User,
    Settings,
    LayoutGrid,
    Receipt,
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { useAuth } from '@/store/auth/authContext';

interface NavItem {
    label: string;
    to: string;
    icon: React.ReactNode;
}

const userNavItems: NavItem[] = [
    { label: 'Tableau de bord', to: '/dashboard', icon: <LayoutDashboard size={18} /> },
    { label: 'Nouveau transfert', to: '/transfer', icon: <Send size={18} /> },
    { label: 'Bénéficiaires', to: '/beneficiaries', icon: <Users size={18} /> },
    { label: 'Historique', to: '/history', icon: <History size={18} /> },
    { label: 'Notifications', to: '/notifications', icon: <Bell size={18} /> },
    { label: 'Profil', to: '/profile', icon: <User size={18} /> },
];

const adminNavItems: NavItem[] = [
    { label: 'Vue d\'ensemble', to: '/admin', icon: <LayoutGrid size={18} /> },
    { label: 'Utilisateurs', to: '/admin/users', icon: <Users size={18} /> },
    { label: 'Transactions', to: '/admin/transactions', icon: <Receipt size={18} /> },
    { label: 'Paramètres', to: '/admin/settings', icon: <Settings size={18} /> },
];

export interface SidebarProps {
    variant?: 'user' | 'admin';
}

/**
 * Barre latérale de navigation.
 *
 * @param variant - 'user' affiche le menu utilisateur, 'admin' affiche le menu admin
 */
function Sidebar({ variant = 'user' }: SidebarProps) {
    const { user } = useAuth();
    const items = variant === 'admin' ? adminNavItems : userNavItems;

    return (
        <aside className="hidden h-screen w-60 flex-shrink-0 flex-col border-r border-gray-100 bg-white md:flex">
            {/* Logo */}
            <div className="flex h-16 items-center gap-2 border-b border-gray-100 px-6">
                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                    S
                </div>
                <span className="text-lg font-bold text-brand-600">SendMe</span>
            </div>

            {/* Navigation */}
            <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4">
                {items.map((item) => (
                    <NavLink
                        key={item.to}
                        to={item.to}
                        end={item.to === '/admin'}
                        className={({ isActive }) =>
                            cn(
                                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                                isActive
                                    ? 'bg-brand-50 text-brand-600'
                                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                            )
                        }
                    >
                        {item.icon}
                        {item.label}
                    </NavLink>
                ))}
            </nav>

            {/* Lien retour pour l'admin */}
            {variant === 'admin' && (
                <div className="border-t border-gray-100 p-3">
                    <NavLink
                        to="/dashboard"
                        className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50"
                    >
                        <LayoutDashboard size={18} />
                        Retour au site
                    </NavLink>
                </div>
            )}

            {/* Mini profil en bas */}
            <div className="border-t border-gray-100 p-4">
                <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700">
                        {user?.firstName?.[0]}
                        {user?.lastName?.[0]}
                    </div>
                    <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium text-gray-900">
                            {user?.firstName} {user?.lastName}
                        </p>
                        <p className="truncate text-xs text-gray-400">{user?.email}</p>
                    </div>
                </div>
            </div>
        </aside>
    );
}

export default Sidebar;