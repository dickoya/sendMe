import { useState, useRef, useEffect } from 'react';
import { Bell, CheckCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import { cn } from '@/lib/utils';
import { useNotification } from '@/store/notification/notificationContext.tsx';
import NotificationList from './NotificationList';

/**
 * Cloche de notifications dans le Header.
 *
 * - Badge rouge avec le nombre de non lues (max affiché : 9+)
 * - Clic → ouvre un dropdown avec les 5 dernières notifications
 * - "Tout marquer comme lu" dans l'en-tête du dropdown
 * - Lien vers le centre de notifications complet (/notifications)
 * - Fermeture au clic à l'extérieur
 */
function NotificationBell() {
    const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotification();
    const [isOpen, setIsOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

    // Fermeture au clic extérieur
    useEffect(() => {
        function handleClickOutside(e: MouseEvent) {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                setIsOpen(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const recentNotifications = notifications.slice(0, 5);

    return (
        <div className="relative" ref={ref}>
            <button
                onClick={() => setIsOpen((prev) => !prev)}
                className="relative rounded-full p-2 text-gray-500 hover:bg-gray-100"
                aria-label="Notifications"
            >
                <Bell size={20} />
                {unreadCount > 0 && (
                    <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-danger-500 px-1 text-[10px] font-bold text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
                )}
            </button>

            {isOpen && (
                <div className="absolute right-0 z-50 mt-2 w-80 rounded-2xl border border-gray-100 bg-white shadow-lg">
                    {/* En-tête */}
                    <div className="flex items-center justify-between border-b border-gray-100 px-4 py-3">
                        <h3 className="text-sm font-semibold text-gray-900">Notifications</h3>
                        {unreadCount > 0 && (
                            <button
                                onClick={() => markAllAsRead()}
                                className="flex items-center gap-1 text-xs font-medium text-brand-600 hover:underline"
                            >
                                <CheckCheck size={14} /> Tout marquer comme lu
                            </button>
                        )}
                    </div>

                    {/* Liste */}
                    <div className="max-h-96 overflow-y-auto p-2">
                        <NotificationList
                            notifications={recentNotifications}
                            onItemClick={(n) => !n.isRead && markAsRead(n.id)}
                        />
                    </div>

                    {/* Pied — lien vers le centre complet */}
                    <div className="border-t border-gray-100 p-2">
                        <Link
                            to="/notifications"
                            onClick={() => setIsOpen(false)}
                            className={cn(
                                'block rounded-lg py-2 text-center text-sm font-medium text-brand-600 hover:bg-brand-50'
                            )}
                        >
                            Voir toutes les notifications
                        </Link>
                    </div>
                </div>
            )}
        </div>
    );
}

export default NotificationBell;