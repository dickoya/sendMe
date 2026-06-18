import api from './api';
import type { ApiResponse, ReferralCodeResponse, ReferralCouponResponse } from '@/types';

/**
 * Service de parrainage — appels vers /api/referral/*
 */
export const referralService = {
    /**
     * Récupère les informations de parrainage de l'utilisateur connecté.
     * GET /api/referral/my-code
     */
    async getMyCode(): Promise<ReferralCodeResponse> {
        const { data } = await api.get<ApiResponse<ReferralCodeResponse>>('/referral/my-code');
        return data.data;
    },

    /**
     * Récupère la liste des coupons de réduction disponibles.
     * GET /api/referral/coupons
     */
    async getCoupons(): Promise<ReferralCouponResponse[]> {
        const { data } = await api.get<ApiResponse<ReferralCouponResponse[]>>('/referral/coupons');
        return data.data;
    },

    /**
     * Vérifie le montant de réduction disponible pour une devise.
     * GET /api/referral/discount?currency=EUR
     */
    async getDiscount(currency: string = 'EUR'): Promise<number> {
        const { data } = await api.get<ApiResponse<number>>('/referral/discount', {
            params: { currency },
        });
        return data.data;
    },

    /**
     * Vérifie si un code de parrainage est valide (existe et actif).
     * Endpoint public — utilisé par le formulaire d'inscription.
     * GET /api/referral/verify?code=SENDME-XXXX
     */
    async verifyCode(code: string): Promise<boolean> {
        const { data } = await api.get<ApiResponse<boolean>>('/referral/verify', {
            params: { code },
        });
        return data.data;
    },
};
