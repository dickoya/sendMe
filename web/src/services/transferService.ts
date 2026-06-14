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

    /**
     * Télécharge le reçu PDF d'une transaction et déclenche
     * le téléchargement dans le navigateur.
     * GET /api/transactions/{id}/receipt
     *
     * Le backend retourne du binaire (application/pdf), donc on
     * utilise responseType: 'blob' pour qu'Axios ne tente pas
     * de parser la réponse en JSON.
     */
    async downloadReceipt(id: number, reference: string): Promise<void> {
        const response = await api.get(`/transactions/${id}/receipt`, {
            responseType: 'blob',
        });

        // Créer un lien temporaire pour déclencher le téléchargement
        const blob = new Blob([response.data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `recu-${reference}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    },

    /**
     * Récupère l'URL de prévisualisation du reçu PDF (ouverture dans un nouvel onglet).
     * GET /api/transactions/{id}/receipt/preview
     *
     * Crée une URL blob temporaire à ouvrir avec window.open().
     */
    async previewReceipt(id: number): Promise<string> {
        const response = await api.get(`/transactions/${id}/receipt/preview`, {
            responseType: 'blob',
        });
        const blob = new Blob([response.data], { type: 'application/pdf' });
        return window.URL.createObjectURL(blob);
    },
};