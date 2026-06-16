import api from './api';
import type { ApiResponse, Beneficiary, BeneficiaryFormData, Page } from '@/types';

/**
 * Service bénéficiaires — appels vers /api/beneficiaries/*
 */
export const beneficiaryService = {
    /**
     * Récupère les bénéficiaires de l'utilisateur connecté avec pagination.
     * GET /api/beneficiaries?page=0&size=20
     */
    async getAll(page = 0, size = 20): Promise<Page<Beneficiary>> {
        const { data } = await api.get<ApiResponse<Page<Beneficiary>>>('/beneficiaries', {
            params: { page, size },
        });
        return data.data;
    },

    /**
     * Crée un nouveau bénéficiaire.
     * POST /api/beneficiaries
     */
    async create(payload: BeneficiaryFormData): Promise<Beneficiary> {
        const { data } = await api.post<ApiResponse<Beneficiary>>('/beneficiaries', payload);
        return data.data;
    },

    /**
     * Met à jour un bénéficiaire existant.
     * PUT /api/beneficiaries/{id}
     */
    async update(id: number, payload: BeneficiaryFormData): Promise<Beneficiary> {
        const { data } = await api.put<ApiResponse<Beneficiary>>(`/beneficiaries/${id}`, payload);
        return data.data;
    },

    /**
     * Supprime un bénéficiaire.
     * DELETE /api/beneficiaries/{id}
     */
    async remove(id: number): Promise<void> {
        await api.delete(`/beneficiaries/${id}`);
    },
};
