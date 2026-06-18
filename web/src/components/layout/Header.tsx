import { LogOut, Menu } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/store/auth/authContext';
import NotificationBell from '@/components/notification/NotificationBell';
import LanguageSwitcher from '@/components/layout/LanguageSwitcher';
import ThemeToggle from '@/components/layout/ThemeToggle';

export interface HeaderProps {
    title?: string;
    onMenuClick?: () => void;
}

/**
 * En-tête commun aux layouts privé et admin.
 *
 * Migration i18n (Phase 4) : "Déconnexion" et le aria-label du bouton
 * menu mobile proviennent du namespace "common".
 *
 * Note : `title`, s'il est fourni par les pages (PrivateLayout/AdminLayout),
 * reste de la responsabilité de la page appelante de le traduire —
 * voir Phases 6-11 pour la migration des titres de page.
 */
function Header({ title, onMenuClick }: HeaderProps) {
    const { t } = useTranslation('common');
    const { logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className="flex h-16 items-center justify-between border-b border-gray-100 bg-white/80 backdrop-blur-md px-4 dark:border-[#222222] dark:bg-[#0D0D0D]/80 dark:backdrop-blur-md md:px-6">

            <div className="flex items-center gap-3">
                <button
                    onClick={onMenuClick}
                    className="rounded-md p-2 text-gray-500 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800 md:hidden"
                    aria-label={t('actions.openMenu')}
                >
                    <Menu size={20} />
                </button>
                {title && <h1 className="text-lg font-semibold text-gray-900 dark:text-gray-100">{title}</h1>}
            </div>

            <div className="flex items-center gap-2">
                <ThemeToggle />
                <LanguageSwitcher />
                <NotificationBell />

                <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800"
                >
                    <LogOut size={16} />
                    <span className="hidden sm:inline">{t('nav.logout')}</span>
                </button>
            </div>
        </header>
    );
}

export default Header;
