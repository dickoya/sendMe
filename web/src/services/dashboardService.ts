import api from './api';
import type { ApiResponse } from '@/types';

export interface UserDashboardStats {
    totalSent: number;
    totalReceived: number;
    totalFees: number;
    totalTransactions: number;
    completedTransactions: number;
    savings: number;
    favoriteBeneficiary: {
        id: number;
        firstName: string;
        lastName: string;
        count: number;
    } | null;
}

export interface MonthlyTransfer {
    month: string; // "2025-01"
    count: number;
    totalAmount: number;
}

export interface TransferEvolution {
    monthly: MonthlyTransfer[];
    yearly: {
        year: number;
        count: number;
        totalAmount: number;
    }[];
}

export interface LiveExchangeRate {
    from: string;
    to: string;
    rate: number;
    rawRate: number;
    timestamp: string;
}

export interface ExchangeRateHistoryPoint {
    date: string;
    rate: number;
}

/**
 * Service pour le tableau de bord utilisateur.
 * Appels vers /api/dashboard/*
 */
export const dashboardService = {
    /**
     * Récupère les statistiques personnelles de l'utilisateur connecté.
     * GET /api/dashboard/stats
     */
    async getStats(): Promise<UserDashboardStats> {
        const { data } = await api.get<ApiResponse<UserDashboardStats>>('/dashboard/stats');
        return data.data;
    },

    /**
     * Récupère l'évolution mensuelle/annuelle des transferts.
     * GET /api/dashboard/evolution
     */
    async getTransferEvolution(): Promise<TransferEvolution> {
        const { data } = await api.get<ApiResponse<TransferEvolution>>('/dashboard/evolution');
        return data.data;
    },

    /**
     * Récupère les taux de change en direct.
     * GET /api/dashboard/exchange-rates
     */
    async getLiveExchangeRates(): Promise<LiveExchangeRate[]> {
        const { data } = await api.get<ApiResponse<LiveExchangeRate[]>>('/dashboard/exchange-rates');
        return data.data;
    },

    /**
     * Récupère l'historique des taux de change pour un couple de devises.
     * GET /api/dashboard/exchange-rates/history/{from}/{to}
     */
    async getExchangeRateHistory(from: string, to: string): Promise<ExchangeRateHistoryPoint[]> {
        const { data } = await api.get<ApiResponse<ExchangeRateHistoryPoint[]>>(
            `/dashboard/exchange-rates/history/${from}/${to}`
        );
        return data.data;
    },
};
