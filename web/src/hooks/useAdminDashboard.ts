import { useState, useEffect, useCallback } from 'react';
import type { DashboardResponse } from '@/types';
import { adminService } from '@/services/adminService';
import { getErrorMessage } from '@/lib/utils';

/**
 * Hook pour le tableau de bord admin avec rafraîchissement manuel.
 *
 * @example
 * const { dashboard, isLoading, refetch } = useAdminDashboard();
 */
export function useAdminDashboard() {
    const [dashboard, setDashboard] = useState<DashboardResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchDashboard = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await adminService.getDashboard();
            setDashboard(data);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchDashboard();
    }, [fetchDashboard]);

    return { dashboard, isLoading, error, refetch: fetchDashboard };
}