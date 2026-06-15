import { useState, useEffect, useCallback } from 'react';
import type { DashboardResponse, ExchangeRateDebug, ExchangeRateDebugEntry } from '@/types';
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
    const [exchangeRateDebug, setExchangeRateDebug] = useState<ExchangeRateDebug | null>(null);
    const [allExchangeRates, setAllExchangeRates] = useState<ExchangeRateDebugEntry[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchDashboard = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const [dashboardData, exchangeRateDebugData, allRates] = await Promise.all([
                adminService.getDashboard(),
                adminService.getExchangeRateDebug(),
                adminService.getAllExchangeRates(),
            ]);
            setDashboard(dashboardData);
            setExchangeRateDebug(exchangeRateDebugData);
            setAllExchangeRates(allRates);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchDashboard();
    }, [fetchDashboard]);

    return { dashboard, exchangeRateDebug, allExchangeRates, isLoading, error, refetch: fetchDashboard };
}
