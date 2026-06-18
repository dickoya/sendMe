import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils';
import { formatDate } from '@/utils/formatDate';
import { NOTIFICATION_DISPLAY, getNotificationMessage } from '@/utils/notificationDisplay';
import type { Notification } from '@/types';

export interface NotificationItemProps {
    notification: Notification;
    onClick: () => void;
}

/**
 * Ligne de notification individuelle.
 *
 * Migration i18n (Phase 10) : le message affiché provient de
 * getNotificationMessage() (reconstruction frontend — Option A),
 * et non plus de notification.message (FR backend brut).
 */
function NotificationItem({ notification, onClick }: NotificationItemProps) {
    const { t } = useTranslation('notifications');
    const { icon: Icon, className } = NOTIFICATION_DISPLAY[notification.type];

    return (
        <button
            onClick={onClick}
            className={cn(
                'flex w-full items-start gap-3 rounded-xl p-3 text-left transition-all duration-200 hover:bg-gray-50 dark:hover:bg-[#222222]',
                !notification.isRead && 'bg-brand-50/50 dark:bg-brand-900/20'
            )}
        >
            <div className={cn('flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full', className)}>
                <Icon size={16} />
            </div>

            <div className="min-w-0 flex-1">
                <p className={cn('text-sm', notification.isRead ? 'text-gray-700 dark:text-[#8B8FA3]' : 'font-medium text-gray-900 dark:text-[#EDEEF0]')}>
                    {getNotificationMessage(notification, t)}
                </p>
                <p className="mt-0.5 text-xs text-gray-400 dark:text-[#5A5E72]">{formatDate(notification.createdAt)}</p>
            </div>

            {!notification.isRead && (
                <span className="mt-1.5 h-2 w-2 flex-shrink-0 rounded-full bg-brand-500 dark:bg-brand-400" />
            )}
        </button>

    );
}

export default NotificationItem;
