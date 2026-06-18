import api from './api';
import type { ApiResponse } from '@/types';

export interface ProfileData {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string | null;
    role: string;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface UpdateProfilePayload {
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber?: string;
}

export interface ChangePasswordPayload {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export interface DeleteAccountPayload {
    password: string;
}

export interface LoginHistoryEntry {
    id: number;
    ipAddress: string;
    userAgent: string;
    success: boolean;
    loginTime: string;
}

/**
 * Service de gestion du profil utilisateur — appels vers /api/profile/*
 */
export const profileService = {
    /**
     * Récupère le profil de l'utilisateur connecté.
     * GET /api/profile
     */
    async getProfile(): Promise<ProfileData> {
        const { data } = await api.get<ApiResponse<ProfileData>>('/profile');
        return data.data;
    },

    /**
     * Met à jour le profil de l'utilisateur connecté.
     * PUT /api/profile
     */
    async updateProfile(payload: UpdateProfilePayload): Promise<ProfileData> {
        const { data } = await api.put<ApiResponse<ProfileData>>('/profile', payload);
        return data.data;
    },

    /**
     * Change le mot de passe de l'utilisateur connecté.
     * PUT /api/profile/password
     */
    async changePassword(payload: ChangePasswordPayload): Promise<void> {
        await api.put('/profile/password', payload);
    },

    /**
     * Supprime (désactive) le compte de l'utilisateur connecté.
     * DELETE /api/profile
     */
    async deleteAccount(payload: DeleteAccountPayload): Promise<void> {
        await api.delete('/profile', { data: payload });
    },

    /**
     * Récupère l'historique de connexion de l'utilisateur connecté.
     * GET /api/profile/login-history
     */
    async getLoginHistory(): Promise<LoginHistoryEntry[]> {
        const { data } = await api.get<ApiResponse<LoginHistoryEntry[]>>('/profile/login-history');
        return data.data;
    },
};
