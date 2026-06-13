import { Bell } from 'lucide-react';
import NotificationItem from './NotificationItem';
import type { Notification } from '@/types';

export interface NotificationListProps {
    notifications: Notification[];
    onItemClick: (notification: Notification) => void;
}

/**
 * Liste de notifications avec état vide illustré.
 */
function NotificationList({ notifications, onItemClick }: NotificationListProps) {
    if (notifications.length === 0) {
        return (
            <div className="flex flex-col items-center gap-2 py-10 text-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400">
                    <Bell size={20} />
                </div>
                <p className="font-medium text-gray-900">Aucune notification</p>
                <p className="text-sm text-gray-500">
                    Vous serez notifié ici de l'activité de votre compte.
                </p>
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