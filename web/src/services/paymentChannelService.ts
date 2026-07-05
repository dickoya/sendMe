import api from './api';
import type { ApiResponse } from '@/types';
import type { PaymentChannel, PaymentChannelRequest } from '@/types/paymentChannel';

/**
 * Service de gestion des canaux de paiement.
 * Tous les appels nécessitent ROLE_ADMIN.
 */
export const paymentChannelService = {
    /**
     * Récupère tous les canaux de paiement (actifs et inactifs).
     * GET /api/admin/payment-channels
     */
    async getAll(): Promise<PaymentChannel[]> {
        const { data } = await api.get<ApiResponse<PaymentChannel[]>>('/admin/payment-channels');
        return data.data;
    },

    /**
     * Ajoute un nouveau canal de paiement.
     * POST /api/admin/payment-channels
     */
    async add(request: PaymentChannelRequest): Promise<PaymentChannel> {
        const { data } = await api.post<ApiResponse<PaymentChannel>>('/admin/payment-channels', request);
        return data.data;
    },

    /**
     * Active ou désactive un canal de paiement.
     * PUT /api/admin/payment-channels/{id}/toggle
     */
    async toggle(id: number): Promise<PaymentChannel> {
        const { data } = await api.put<ApiResponse<PaymentChannel>>(`/admin/payment-channels/${id}/toggle`);
        return data.data;
    },
};
