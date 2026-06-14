import { useEffect, useRef, useCallback } from 'react';
import type { Notification, NotificationType } from '@/types';

type SSEEventHandler = (notification: Notification) => void;

/**
 * Hook gérant la connexion SSE (Server-Sent Events) vers
 * /api/notifications/stream.
 *
 * Reconnexion automatique : EventSource reconnecte nativement
 *
 *
 * en cas de coupure (comportement natif du navigateur),
 * sauf si le composant est démonté (cleanup ferme la connexion).
 *
 * @param url        URL du flux SSE (avec token en query param)
 * @param onMessage  callback appelé pour CHAQUE type d'événement reçu
 * @param enabled    si false, ne se connecte pas (ex: utilisateur non authentifié)
 *
 * @example
 * useSSE(notificationService.getStreamUrl(), (notif) => {
 *   addNotification(notif);
 *   showToast(notif.message, 'info');
 * }, isAuthenticated);
 */
export function useSSE(url: string, onMessage: SSEEventHandler, enabled = true) {
    const eventSourceRef = useRef<EventSource | null>(null);

    // Garde la dernière version du callback sans recréer la connexion
    const handlerRef = useRef(onMessage);
    handlerRef.current = onMessage;

    const ALL_EVENT_TYPES: NotificationType[] = [
        'TRANSFER_INITIATED',
        'TRANSFER_PROCESSING',
        'TRANSFER_COMPLETED',
        'TRANSFER_FAILED',
        'TRANSFER_REFUNDED',
        'PAYMENT_CONFIRMED',
        'PAYMENT_FAILED',
        'ACCOUNT_CREATED',
        'ACCOUNT_DISABLED',
        'ACCOUNT_REACTIVATED',
        'SECURITY_LOGIN',
        'SECURITY_PASSWORD_CHANGED',
        'SECURITY_SUSPICIOUS_LOGIN',
        'INFO',
        'MAINTENANCE',
        'FEE_UPDATE',
    ];

    useEffect(() => {
        if (!enabled) return;

        const eventSource = new EventSource(url);
        eventSourceRef.current = eventSource;

        // Événement de confirmation de connexion (envoyé par SseEmitterService)
        eventSource.addEventListener('CONNECTED', () => {
            console.debug('[SSE] Connexion établie');
        });

        // Un addEventListener par type de notification métier
        // (le backend envoie .name(type.name()) — voir SseEmitterService.sendToUser)
        ALL_EVENT_TYPES.forEach((type) => {
            eventSource.addEventListener(type, (event: MessageEvent) => {
                try {
                    const notification: Notification = JSON.parse(event.data);
                    handlerRef.current(notification);
                } catch (err) {
                    console.error('[SSE] Erreur de parsing', err);
                }
            });
        });

        eventSource.onerror = () => {
            console.warn('[SSE] Erreur de connexion — tentative de reconnexion automatique');
        };

        return () => {
            eventSource.close();
            eventSourceRef.current = null;
        };
    }, [url, enabled]);
}