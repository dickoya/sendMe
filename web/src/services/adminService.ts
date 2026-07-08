import api from './api';
import type {
    ApiResponse,
    Page,
    UserResponse,
    Transaction,
    DashboardResponse,
    TransactionStatus,
    ExchangeRateDebug,
    ExchangeRateDebugEntry,
    VolumeDataPoint,
    SuccessRateDataPoint,
} from '@/types';

export interface FeeConfigPayload {
    feeRate: number;
    reason: string;
}

export interface ExchangeRateConfigPayload {
    cacheDurationMinutes?: number;
    marginPercent?: number;
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
     * Infos debug du taux EUR -> XOF affichees dans le dashboard admin.
     * GET /api/admin/debug/rate?from=EUR&to=XOF
     */
    async getExchangeRateDebug(from = 'EUR', to = 'XOF'): Promise<ExchangeRateDebug> {
        const { data } = await api.get<ApiResponse<ExchangeRateDebug>>('/admin/debug/rate', {
            params: { from, to },
        });
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
     * Récupère la configuration actuelle des taux de change.
     * GET /api/admin/config/exchange-rate
     */
    async getExchangeRateConfig(): Promise<{ cacheDurationMinutes: number; marginPercent: number }> {
        const { data } = await api.get<ApiResponse<{ cacheDurationMinutes: number; marginPercent: number }>>('/admin/config/exchange-rate');
        return data.data;
    },

    /**
     * Met à jour la configuration des taux de change (durée du cache et/ou marge).
     * PUT /api/admin/config/exchange-rate
     */
    async updateExchangeRateConfig(payload: ExchangeRateConfigPayload): Promise<void> {
        await api.put('/admin/config/exchange-rate', payload);
    },

    /**
     * Récupère les taux de change debug pour toutes les paires de devises.
     * GET /api/debug/rates
     */
    async getAllExchangeRates(): Promise<ExchangeRateDebugEntry[]> {
        const { data } = await api.get<ApiResponse<ExchangeRateDebugEntry[]>>('/debug/rates');
        return data.data;
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

    /**
     * Liste paginée des utilisateurs avec filtres avancés.
     * GET /api/admin/users/filter?page=0&size=20&search=...&isActive=...&dateFrom=...&dateTo=...&sort=field,dir
     */
    async searchUsers(
        page = 0,
        size = 20,
        filters: {
            search?: string;
            status?: 'ALL' | 'ACTIVE' | 'INACTIVE';
            dateFrom?: string;
            dateTo?: string;
            sortBy?: string;
            sortDir?: 'asc' | 'desc';
        } = {}
    ): Promise<Page<UserResponse>> {
        const isActive =
            filters.status === 'ACTIVE' ? true : filters.status === 'INACTIVE' ? false : undefined;
        const sort = `${filters.sortBy || 'createdAt'},${filters.sortDir || 'desc'}`;

        const { data } = await api.get<ApiResponse<Page<UserResponse>>>('/admin/users/filter', {
            params: {
                page,
                size,
                search: filters.search || undefined,
                isActive,
                dateFrom: filters.dateFrom || undefined,
                dateTo: filters.dateTo || undefined,
                sort,
            },
        });
        return data.data;
    },

    /**
     * Active ou désactive plusieurs utilisateurs en une seule requête.
     * POST /api/admin/users/batch-toggle
     */
    async batchToggleUsers(ids: number[], active: boolean): Promise<void> {
        await api.post('/admin/users/batch-toggle', { userIds: ids, suspend: !active });
    },

    /**
     * Volume de transactions par jour pour une période donnée.
     * GET /api/admin/analytics/volume?days=30
     */
    async getVolumeData(days = 30): Promise<VolumeDataPoint[]> {
        const { data } = await api.get<ApiResponse<VolumeDataPoint[]>>('/admin/analytics/volume', {
            params: { days },
        });
        return data.data;
    },

    /**
     * Évolution du taux de succès par jour pour une période donnée.
     * GET /api/admin/analytics/success-rate?days=30
     */
    async getSuccessRateData(days = 30): Promise<SuccessRateDataPoint[]> {
        const { data } = await api.get<ApiResponse<SuccessRateDataPoint[]>>('/admin/analytics/success-rate', {
            params: { days },
        });
        return data.data;
    },
};


