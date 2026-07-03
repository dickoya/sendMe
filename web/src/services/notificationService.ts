import api from './api';
import { API_BASE_URL, STORAGE_KEYS } from '@/utils/constants';
import type { ApiResponse, Notification } from '@/types';

/**
 * Service notifications — appels vers /api/notifications/*
 */
export const notificationService = {
    /**
     * Récupère l'historique complet des notifications.
     * GET /api/notifications
     */
    async getAll(): Promise<Notification[]> {
        const { data } = await api.get<ApiResponse<{ content: Notification[] }>>('/notifications');
        return data.data.content;
    },

    /**
     * Récupère le nombre de notifications non lues.
     * GET /api/notifications/unread-count
     */
    async getUnreadCount(): Promise<number> {
        const { data } = await api.get<ApiResponse<{ unreadCount: number }>>('/notifications/unread-count');
        return data.data.unreadCount;
    },

    /**
     * Marque une notification comme lue.
     * PUT /api/notifications/{id}/read
     */
    async markAsRead(id: number): Promise<void> {
        await api.put(`/notifications/${id}/read`);
    },

    /**
     * Marque toutes les notifications comme lues.
     * PUT /api/notifications/read-all
     */
    async markAllAsRead(): Promise<void> {
        await api.put('/notifications/read-all');
    },

    /**
     * Établit une connexion SSE (Server-Sent Events) en utilisant fetch()
     * avec le header Authorization: Bearer standard.
     *
     * Contrairement à EventSource, fetch() permet d'envoyer des headers
     * personnalisés, ce qui évite de passer le token JWT en query param
     * (faille de sécurité : URLs loggées, historique navigateur, Referer header).
     *
     * @param onNotification callback appelée à chaque notification reçue
     * @param onError        callback appelée en cas d'erreur de connexion
     * @returns AbortController pour fermer la connexion
     */
    connectSSE(
        onNotification: (notification: Notification) => void,
        onError?: (error: Error) => void
    ): AbortController {
        const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
        const abortController = new AbortController();

        const connect = async () => {
            try {
                const response = await fetch(`${API_BASE_URL}/notifications/stream`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Accept': 'text/event-stream',
                    },
                    signal: abortController.signal,
                });

                if (!response.ok) {
                    throw new Error(`SSE connection failed: ${response.status}`);
                }

                const reader = response.body?.getReader();
                if (!reader) {
                    throw new Error('ReadableStream not supported');
                }

                const decoder = new TextDecoder();
                let buffer = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    buffer += decoder.decode(value, { stream: true });

                    // Parser les événements SSE
                    const lines = buffer.split('\n');
                    buffer = '';

                    let currentEvent = '';
                    let currentData = '';

                    for (const line of lines) {
                        if (line.startsWith('event: ')) {
                            currentEvent = line.slice(7).trim();
                        } else if (line.startsWith('data: ')) {
                            currentData = line.slice(6).trim();
                        } else if (line === '' && currentData) {
                            // Fin d'un événement SSE
                            try {
                                const notification = JSON.parse(currentData) as Notification;
                                onNotification(notification);
                            } catch {
                                // Ignorer les événements non-JSON (ex: CONNECTED)
                            }
                            currentEvent = '';
                            currentData = '';
                        }
                    }
                }
            } catch (error: unknown) {
                if (error instanceof Error && error.name === 'AbortError') {
                    return; // Fermeture volontaire
                }
                if (onError && error instanceof Error) {
                    onError(error);
                }
            }
        };

        connect();

        return abortController;
    },
};
