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
     * Construit l'URL du flux SSE avec le token JWT en query param.
     *
     * Note : EventSource ne supporte pas les headers personnalisés
     * (impossible d'envoyer Authorization: Bearer ...). Le token
     * doit donc être passé en query param, et le backend doit
     * accepter ce mode d'authentification pour /notifications/stream.
     *
     * GET /api/notifications/stream?token=...
     */
    getStreamUrl(): string {
        const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
        return `${API_BASE_URL}/notifications/stream?token=${token}`;
    },
};