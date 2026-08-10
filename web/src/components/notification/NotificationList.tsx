import { useTranslation } from 'react-i18next';
import { Bell } from 'lucide-react';
import NotificationItem from './NotificationItem';
import type { Notification } from '@/types';

export interface NotificationListProps {
    notifications: Notification[];
    onItemClick: (notification: Notification) => void;
}

/**
 * Liste de notifications avec état vide illustré.
 *
 * Namespace "notifications" (clés empty.*).
 */
function NotificationList({ notifications, onItemClick }: NotificationListProps) {
    const { t } = useTranslation('notifications');

    if (notifications.length === 0) {
        return (
            <div className="flex flex-col items-center gap-2 py-10 text-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400 dark:bg-[#222222] dark:text-[#5A5E72]">
                    <Bell size={20} />
                </div>
                <p className="font-medium text-gray-900 dark:text-[#EDEEF0]">{t('empty.title')}</p>
                <p className="text-sm text-gray-500 dark:text-[#8B8FA3]">{t('empty.message')}</p>
            </div>

        );
    }

    return (
        <div className="flex flex-col gap-1">
            {notifications.map((n) => (
                <NotificationItem key={n.id} notification={n} onClick={() => onItemClick(n)} />
            ))}
        </div>
    );
}

export default NotificationList;
