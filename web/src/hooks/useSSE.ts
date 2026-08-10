import { useEffect, useRef } from 'react';
import type { Notification } from '@/types';
import { notificationService } from '@/services/notificationService';

type SSEEventHandler = (notification: Notification) => void;

/**
 * Hook gérant la connexion SSE (Server-Sent Events) vers
 * /api/notifications/stream via fetch() avec Authorization header.
 *
 * Utilise notificationService.connectSSE() qui envoie le token JWT
 * dans le header Authorization (sécurisé, pas de token dans l'URL).
 *
 * Reconnexion automatique en cas de coupure.
 *
 * @param onMessage  callback appelé pour chaque notification reçue
 * @param enabled    si false, ne se connecte pas (ex: utilisateur non authentifié)
 */
export function useSSE(onMessage: SSEEventHandler, enabled = true) {
    const abortRef = useRef<AbortController | null>(null);
    const reconnectTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    // Garde la dernière version du callback sans recréer la connexion
    const handlerRef = useRef(onMessage);
    handlerRef.current = onMessage;

    useEffect(() => {
        if (!enabled) return;

        let isCancelled = false;

        const connect = () => {
            if (isCancelled) return;

            const abortCtrl = notificationService.connectSSE(
                (notification) => {
                    handlerRef.current(notification);
                },
                (error) => {
                    console.warn('[SSE] Erreur de connexion — reconnexion dans 5s', error);
                    if (!isCancelled) {
                        reconnectTimeoutRef.current = setTimeout(connect, 5000);
                    }
                }
            );
            abortRef.current = abortCtrl;
        };

        connect();

        return () => {
            isCancelled = true;
            if (abortRef.current) {
                abortRef.current.abort();
                abortRef.current = null;
            }
            if (reconnectTimeoutRef.current) {
                clearTimeout(reconnectTimeoutRef.current);
                reconnectTimeoutRef.current = null;
            }
        };
    }, [enabled]);
}
