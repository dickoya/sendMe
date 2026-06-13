import api from './api';
import type { ApiResponse, Page, Transaction } from '@/types';

/**
 * Service transferts — appels vers /api/transactions et /api/transfers
 */
export const transferService = {
    /**
     * Récupère l'historique paginé des transactions de l'utilisateur.
     * GET /api/transactions?page=0&size=10
     */
    async getHistory(page = 0, size = 10): Promise<Page<Transaction>> {
        const { data } = await api.get<ApiResponse<Page<Transaction>>>('/transactions', {
            params: { page, size },
        });
        return data.data;
    },

    /**
     * Récupère le détail d'une transaction.
     * GET /api/transactions/{id}
     */
    async getById(id: number): Promise<Transaction> {
        const { data } = await api.get<ApiResponse<Transaction>>(`/transactions/${id}`);
        return data.data;
    },
};