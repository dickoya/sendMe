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

export interface ForgotPasswordPayload {
    email: string;
}

export interface ResetPasswordPayload {
    token: string;
    email: string;
    newPassword: string;
    confirmPassword: string;
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

    /**
     * Vérifie l'adresse email d'un utilisateur à partir d'un token.
     * GET /api/auth/verify-email?token=xxx
     * Retourne l'ApiResponse complète car elle contient les tokens JWT.
     */
    async verifyEmail(token: string): Promise<ApiResponse<AuthResponse>> {
        const { data } = await api.get<ApiResponse<AuthResponse>>('/auth/verify-email', {
            params: { token },
        });
        return data;
    },

    /**
     * Renvoie l'email de vérification à un utilisateur.
     * POST /api/auth/resend-verification
     */
    async resendVerification(email: string): Promise<ApiResponse<void>> {
        const { data } = await api.post<ApiResponse<void>>('/auth/resend-verification', { email });
        return data;
    },

    /**
     * Demande un lien de réinitialisation de mot de passe.
     * POST /api/auth/forgot-password
     */
    async forgotPassword(payload: ForgotPasswordPayload): Promise<ApiResponse<void>> {
        const { data } = await api.post<ApiResponse<void>>('/auth/forgot-password', payload);
        return data;
    },

    /**
     * Réinitialise le mot de passe après clic sur le lien reçu par email.
     * POST /api/auth/reset-password
     */
    async resetPassword(payload: ResetPasswordPayload): Promise<ApiResponse<void>> {
        const { data } = await api.post<ApiResponse<void>>('/auth/reset-password', payload);
        return data;
    },
};


