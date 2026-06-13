import { cn } from '@/lib/utils';
import { formatDate } from '@/utils/formatDate';
import { NOTIFICATION_DISPLAY } from '@/utils/notificationDisplay';
import type { Notification } from '@/types';

export interface NotificationItemProps {
    notification: Notification;
    onClick: () => void;
}

/**
 * Ligne de notification individuelle — icône colorée selon le type,
 * message, date relative, et pastille bleue si non lue.
 */
function NotificationItem({ notification, onClick }: NotificationItemProps) {
    const { icon: Icon, className } = NOTIFICATION_DISPLAY[notification.type];

    return (
        <button
            onClick={onClick}
            className={cn(
                'flex w-full items-start gap-3 rounded-xl p-3 text-left transition-colors hover:bg-gray-50',
                !notification.isRead && 'bg-brand-50/50'
            )}
        >
            <div className={cn('flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full', className)}>
                <Icon size={16} />
            </div>

            <div className="min-w-0 flex-1">
                <p className={cn('text-sm', notification.isRead ? 'text-gray-700' : 'font-medium text-gray-900')}>
                    {notification.message}
                </p>
                <p className="mt-0.5 text-xs text-gray-400">{formatDate(notification.createdAt)}</p>
            </div>

            {!notification.isRead && (
                <span className="mt-1.5 h-2 w-2 flex-shrink-0 rounded-full bg-brand-500" />
            )}
        </button>
    );
}

export default NotificationItem;