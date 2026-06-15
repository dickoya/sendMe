import { useState, useRef, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Bell, CheckCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useNotification } from '@/store/notification/notificationContext';
import NotificationList from './NotificationList';

/**
 * Cloche de notifications dans le Header.
 *
 * Migration i18n (Phase 10) : namespace "notifications" pour le titre,
 * "tout marquer comme lu" et le lien vers le centre complet ;
 * "common" pour le aria-label de la cloche elle-même.
 */
function NotificationBell() {
    const { t } = useTranslation('notifications');
    const { t: tCommon } = useTranslation('common');
    const { notifications, unreadCount, markAsRead, markAllAsRead } = useNotification();
    const [isOpen, setIsOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

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
                aria-label={tCommon('nav.notifications')}
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
                        <h3 className="text-sm font-semibold text-gray-900">{t('title')}</h3>
                        {unreadCount > 0 && (
                            <button
                                onClick={() => markAllAsRead()}
                                className="flex items-center gap-1 text-xs font-medium text-brand-600 hover:underline"
                            >
                                <CheckCheck size={14} /> {t('markAllRead')}
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
                            className="block rounded-lg py-2 text-center text-sm font-medium text-brand-600 hover:bg-brand-50"
                        >
                            {t('viewAll')}
                        </Link>
                    </div>
                </div>
            )}
        </div>
    );
}

export default NotificationBell;