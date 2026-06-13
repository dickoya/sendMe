import { Bell, LogOut, Menu } from 'lucide-react';
import { useAuth } from '@/store/auth/authContext';
import { useNavigate } from 'react-router-dom';

export interface HeaderProps {
    /** Titre de la page affiché à gauche */
    title?: string;
    /** Callback pour ouvrir le menu mobile */
    onMenuClick?: () => void;
}

/**
 * Barre supérieure affichée sur les pages privées et admin.
 * Contient le titre de la page, la cloche de notifications
 * et le bouton de déconnexion.
 */
function Header({ title, onMenuClick }: HeaderProps) {
    const { logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className="flex h-16 items-center justify-between border-b border-gray-100 bg-white px-4 md:px-6">
            <div className="flex items-center gap-3">
                {/* Bouton menu mobile */}
                <button
                    onClick={onMenuClick}
                    className="rounded-md p-2 text-gray-500 hover:bg-gray-100 md:hidden"
                    aria-label="Ouvrir le menu"
                >
                    <Menu size={20} />
                </button>
                {title && <h1 className="text-lg font-semibold text-gray-900">{title}</h1>}
            </div>

            <div className="flex items-center gap-2">
                {/* Cloche de notifications — branchée à l'Étape 7 (SSE) */}
                <button
                    className="relative rounded-full p-2 text-gray-500 hover:bg-gray-100"
                    aria-label="Notifications"
                >
                    <Bell size={20} />
                    {/* Badge de compteur — placeholder */}
                    {/* <span className="absolute right-1.5 top-1.5 h-2 w-2 rounded-full bg-danger-500" /> */}
                </button>

                <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100"
                >
                    <LogOut size={16} />
                    <span className="hidden sm:inline">Déconnexion</span>
                </button>
            </div>
        </header>
    );
}

export default Header;