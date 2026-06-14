import api from './api';
import type { ApiResponse, Page, UserResponse, Transaction, DashboardResponse, TransactionStatus } from '@/types';

export interface FeeConfigPayload {
    feeRate: number;
    reason: string;
}

/**
 * Service administration — appels vers /api/admin/*
 * Toutes ces routes nécessitent ROLE_ADMIN (vérifié côté backend).
 */
export const adminService = {
    /**
     * Tableau de bord avec statistiques globales.
     * GET /api/admin/dashboard
     */
    async getDashboard(): Promise<DashboardResponse> {
        const { data } = await api.get<ApiResponse<DashboardResponse>>('/admin/dashboard');
        return data.data;
    },

    /**
     * Liste paginée des utilisateurs.
     * GET /api/admin/users?page=0&size=20
     */
    async getUsers(page = 0, size = 20): Promise<Page<UserResponse>> {
        const { data } = await api.get<ApiResponse<Page<UserResponse>>>('/admin/users', {
            params: { page, size },
        });
        return data.data;
    },

    /**
     * Active ou désactive un compte utilisateur.
     * PUT /api/admin/users/{id}/toggle
     */
    async toggleUserStatus(id: number): Promise<UserResponse> {
        const { data } = await api.put<ApiResponse<UserResponse>>(`/admin/users/${id}/toggle`);
        return data.data;
    },

    /**
     * Liste paginée de toutes les transactions, avec filtre optionnel par statut.
     * GET /api/admin/transactions?page=0&size=20&status=COMPLETED
     */
    async getTransactions(page = 0, size = 20, status?: TransactionStatus | 'ALL'): Promise<Page<Transaction>> {
        const { data } = await api.get<ApiResponse<Page<Transaction>>>('/admin/transactions', {
            params: { page, size, status: status === 'ALL' ? undefined : status },
        });
        return data.data;
    },

    /**
     * Met à jour le taux de commission de la plateforme.
     * Déclenche l'envoi d'un email à tous les utilisateurs actifs (Phase F backend).
     * PUT /api/admin/config/fees
     */
    async updateFeeRate(payload: FeeConfigPayload): Promise<void> {
        await api.put('/admin/config/fees', payload);
    },

    /**
     * Envoie une notification de maintenance à tous les utilisateurs actifs.
     * POST /api/admin/notifications/maintenance?date=...&duration=...
     */
    async sendMaintenanceNotification(date: string, duration: string): Promise<void> {
        await api.post('/admin/notifications/maintenance', null, {
            params: { date, duration },
        });
    },
};