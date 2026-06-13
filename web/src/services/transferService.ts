import api from './api';
import type { ApiResponse, Page, Transaction, SimulateRequest, SimulateResponse, TransferRequest } from '@/types';

export const transferService = {
    async getHistory(page = 0, size = 10): Promise<Page<Transaction>> {
        const { data } = await api.get<ApiResponse<Page<Transaction>>>('/transactions', {
            params: { page, size },
        });
        return data.data;
    },

    async getById(id: number): Promise<Transaction> {
        const { data } = await api.get<ApiResponse<Transaction>>(`/transactions/${id}`);
        return data.data;
    },

    /**
     * Simule un transfert pour calculer frais, taux de change et montant reçu.
     * POST /api/transfers/simulate
     * Appelé en temps réel à chaque modification du montant — ne crée rien en base.
     */
    async simulate(payload: SimulateRequest): Promise<SimulateResponse> {
        const { data } = await api.post<ApiResponse<SimulateResponse>>('/transfers/simulate', payload);
        return data.data;
    },

    /**
     * Initie un transfert réel — débite la carte via Stripe et lance la stratégie.
     * POST /api/transfers
     */
    async transfer(payload: TransferRequest): Promise<Transaction> {
        const { data } = await api.post<ApiResponse<Transaction>>('/transfers', payload);
        return data.data;
    },
};