import { useTranslation } from 'react-i18next';
import { CheckCheck } from 'lucide-react';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import NotificationList from '@/components/notification/NotificationList';
import { useNotification } from '@/store/notification/notificationContext';

/**
 * Centre de notifications complet.
 *
 * Migration i18n (Phase 10) : namespace "notifications".
 */
function Notifications() {
    const { t } = useTranslation('notifications');
    const { t: tCommon } = useTranslation('common');
    const { notifications, unreadCount, isLoading, markAsRead, markAllAsRead } = useNotification();

    return (
        <Card
            title={t('title')}
            actions={
                unreadCount > 0 && (
                    <Button size="sm" variant="outline" icon={<CheckCheck size={16} />} onClick={() => markAllAsRead()}>
                        {t('markAllReadWithCount', { count: unreadCount })}
                    </Button>
                )
            }
        >
            {isLoading ? (
                <Spinner label={tCommon('states.loading')} />
            ) : (
                <NotificationList
                    notifications={notifications}
                    onItemClick={(n) => !n.isRead && markAsRead(n.id)}
                />
            )}
        </Card>
    );
}

export default Notifications;