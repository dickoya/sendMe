import api from './api';
import type { ApiResponse, AuthResponse } from '@/types';

export interface RegisterPayload {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
    phoneNumber?: string;
    referralCode?: string;
}

export interface LoginPayload {
    email: string;
    password: string;
}

/**
 * Service d'authentification — appels vers /api/auth/*
 */
export const authService = {
    /**
     * Inscrit un nouvel utilisateur.
     * POST /api/auth/register
     */
    async register(payload: RegisterPayload): Promise<AuthResponse> {
        const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/register', payload);
        return data.data;
    },

    /**
     * Connecte un utilisateur existant.
     * POST /api/auth/login
     */
    async login(payload: LoginPayload): Promise<AuthResponse> {
        const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/login', payload);
        return data.data;
    },

    /**
     * Rafraîchit les tokens JWT à partir d'un refresh token valide.
     * POST /api/auth/refresh
     */
    async refresh(refreshToken: string): Promise<AuthResponse> {
        const { data } = await api.post<ApiResponse<AuthResponse>>('/auth/refresh', { refreshToken });
        return data.data;
    },
};


