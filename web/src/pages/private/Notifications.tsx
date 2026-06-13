import { CheckCheck } from 'lucide-react';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import NotificationList from '@/components/notification/NotificationList';
import { useNotification } from '@/store/notification/notificationContext.tsx';

/**
 * Centre de notifications complet — historique entier
 * avec possibilité de tout marquer comme lu.
 */
function Notifications() {
    const { notifications, unreadCount, isLoading, markAsRead, markAllAsRead } = useNotification();

    return (
        <Card
            title="Centre de notifications"
            actions={
                unreadCount > 0 && (
                    <Button size="sm" variant="outline" icon={<CheckCheck size={16} />} onClick={() => markAllAsRead()}>
                        Tout marquer comme lu ({unreadCount})
                    </Button>
                )
            }
        >
            {isLoading ? (
                <Spinner label="Chargement des notifications..." />
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