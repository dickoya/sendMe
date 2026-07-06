import api from './api';
import type { ApiResponse } from '@/types';
import type { PaymentChannel } from '@/types/paymentChannel';

/**
 * Service pour récupérer les canaux de paiement actifs.
 * Endpoint public (sans authentification).
 */
export const channelService = {
    /**
     * Récupère la liste des canaux de paiement actifs.
     * GET /api/channels/active
     */
    async getActive(): Promise<PaymentChannel[]> {
        const { data } = await api.get<ApiResponse<PaymentChannel[]>>('/channels/active');
        return data.data;
    },
};
