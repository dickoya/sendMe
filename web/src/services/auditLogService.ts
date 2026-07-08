import api from './api';
import type { ApiResponse, Page } from '../types';

/**
 * Interface pour un log d'audit centralisé.
 */
export interface AuditLogEntry {
    id: number;
    category: string;
    eventType: string;
    userEmail: string | null;
    userId: number | null;
    resourceId: number | null;
    resourceType: string | null;
    status: string;
    message: string | null;
    details: string | null;
    ipAddress: string | null;
    userAgent: string | null;
    createdAt: string;
}

/**
 * Interface pour les filtres de recherche.
 */
export interface AuditLogFilters {
    category?: string;
    eventType?: string;
    status?: string;
    userEmail?: string;
    dateFrom?: string;
    dateTo?: string;
}

/**
 * Service pour interagir avec l'API des logs d'audit centralisés.
 */
const auditLogService = {
    getAll: async (page = 0, size = 50): Promise<Page<AuditLogEntry>> => {
        const response = await api.get<ApiResponse<Page<AuditLogEntry>>>(
            `/admin/audit-logs?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    getByCategory: async (category: string, page = 0, size = 50): Promise<Page<AuditLogEntry>> => {
        const response = await api.get<ApiResponse<Page<AuditLogEntry>>>(
            `/admin/audit-logs/category/${category}?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    getByEventType: async (eventType: string, page = 0, size = 50): Promise<Page<AuditLogEntry>> => {
        const response = await api.get<ApiResponse<Page<AuditLogEntry>>>(
            `/admin/audit-logs/type/${eventType}?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    getByUserEmail: async (userEmail: string, page = 0, size = 50): Promise<Page<AuditLogEntry>> => {
        const response = await api.get<ApiResponse<Page<AuditLogEntry>>>(
            `/admin/audit-logs/user/${encodeURIComponent(userEmail)}?page=${page}&size=${size}`
        );
        return response.data.data;
    },

    search: async (filters: AuditLogFilters, page = 0, size = 50): Promise<Page<AuditLogEntry>> => {
        const params = new URLSearchParams();
        params.append('page', page.toString());
        params.append('size', size.toString());

        if (filters.category) params.append('category', filters.category);
        if (filters.eventType) params.append('eventType', filters.eventType);
        if (filters.status) params.append('status', filters.status);
        if (filters.userEmail) params.append('userEmail', filters.userEmail);
        if (filters.dateFrom) params.append('dateFrom', filters.dateFrom);
        if (filters.dateTo) params.append('dateTo', filters.dateTo);

        const response = await api.get<ApiResponse<Page<AuditLogEntry>>>(
            `/admin/audit-logs/search?${params.toString()}`
        );
        return response.data.data;
    },

    getStatsByCategory: async (): Promise<Record<string, number>> => {
        const response = await api.get<ApiResponse<Record<string, number>>>(
            '/admin/audit-logs/stats/category'
        );
        return response.data.data;
    },

    getStatsByEventType: async (): Promise<Record<string, number>> => {
        const response = await api.get<ApiResponse<Record<string, number>>>(
            '/admin/audit-logs/stats/type'
        );
        return response.data.data;
    },

    getStatsByStatus: async (): Promise<Record<string, number>> => {
        const response = await api.get<ApiResponse<Record<string, number>>>(
            '/admin/audit-logs/stats/status'
        );
        return response.data.data;
    },
};

export default auditLogService;
