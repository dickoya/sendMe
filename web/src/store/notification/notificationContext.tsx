import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import type { Notification } from '@/types';
import { notificationService } from '@/services/notificationService';
import { useSSE } from '@/hooks/useSSE';
import { useAuth } from '@/store/auth/authContext';
import { useToast } from './toastContext';
import { getNotificationMessage } from '@/utils/notificationDisplay';
import i18n from '@/i18n/index';

interface NotificationContextValue {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  markAsRead: (id: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  refetch: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextValue | null>(null);

/**
 * Provider centralisant l'état des notifications.
 *
 * - Charge l'historique au montage (si authentifié)
 * - Ouvre une connexion SSE pour recevoir les nouvelles notifications en temps réel
 * - Affiche un toast à chaque nouvelle notification reçue via SSE
 * - Expose markAsRead/markAllAsRead synchronisés avec le compteur
 */
export function NotificationProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const { showToast } = useToast();

  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);

  const fetchAll = useCallback(async () => {
    if (!isAuthenticated) return;
    setIsLoading(true);
    try {
      const [list, count] = await Promise.all([
        notificationService.getAll(),
        notificationService.getUnreadCount(),
      ]);
      setNotifications(list);
      setUnreadCount(count);
    } catch (err) {
      console.error('[Notifications] Échec du chargement', err);
    } finally {
      setIsLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  // Connexion SSE temps réel
  // Utilise fetch() avec Authorization header (sécurisé, pas de token dans l'URL)
  useSSE(
    (notification) => {
      // Ajouter la nouvelle notification en tête de liste
      setNotifications((prev) => [notification, ...prev]);
      setUnreadCount((prev) => prev + 1);

      // 1. Générer la fonction 't' liée au namespace 'notifications'
      const t = i18n.getFixedT(null, 'notifications');

      // 2. Reconstruire le message traduit
      const message = getNotificationMessage(notification, t);

      // 3. Afficher le toast avec le bon message
      showToast(message, toastTypeForNotification(notification.type));
    },
    isAuthenticated
  );

  const markAsRead = async (id: number) => {
    await notificationService.markAsRead(id);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
    );
    setUnreadCount((prev) => Math.max(0, prev - 1));
  };

  const markAllAsRead = async () => {
    await notificationService.markAllAsRead();
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    setUnreadCount(0);
  };

  return (
    <NotificationContext.Provider
      value={{ notifications, unreadCount, isLoading, markAsRead, markAllAsRead, refetch: fetchAll }}
    >
      {children}
    </NotificationContext.Provider>
  );
}

/**
 * Hook pour accéder aux notifications et au compteur non lu.
 *
 * @example
 * const { unreadCount, notifications, markAsRead } = useNotification();
 */
export function useNotification() {
  const context = useContext(NotificationContext);
  if (!context) throw new Error('useNotification doit être utilisé dans un NotificationProvider');
  return context;
}

/**
 * Mappe un NotificationType vers un type de toast (couleur).
 */
function toastTypeForNotification(type: string): 'success' | 'error' | 'warning' | 'info' {
  if (type.includes('COMPLETED') || type.includes('CONFIRMED') || type.includes('REACTIVATED') || type.includes('CREATED')) {
    return 'success';
  }
  if (type.includes('FAILED') || type.includes('SUSPICIOUS') || type.includes('DISABLED')) {
    return 'error';
  }
  if (type.includes('REFUNDED') || type.includes('MAINTENANCE') || type.includes('FEE')) {
    return 'warning';
  }
  return 'info';
}
