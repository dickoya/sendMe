import { useState, useEffect, useCallback } from 'react';
import type { DashboardResponse, ExchangeRateDebug, ExchangeRateDebugEntry } from '@/types';
import { adminService } from '@/services/adminService';
import { getErrorMessage } from '@/lib/utils';

export interface ExchangeRateConfig {
    cacheDurationMinutes: number;
    marginPercent: number;
}

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
    const [exchangeRateConfig, setExchangeRateConfig] = useState<ExchangeRateConfig | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchDashboard = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const [dashboardData, exchangeRateDebugData, allRates, config] = await Promise.all([
                adminService.getDashboard(),
                adminService.getExchangeRateDebug(),
                adminService.getAllExchangeRates(),
                adminService.getExchangeRateConfig(),
            ]);
            setDashboard(dashboardData);
            setExchangeRateDebug(exchangeRateDebugData);
            setAllExchangeRates(allRates);
            setExchangeRateConfig(config);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchDashboard();
    }, [fetchDashboard]);

    return {
        dashboard,
        exchangeRateDebug,
        allExchangeRates,
        exchangeRateConfig,
        isLoading,
        error,
        refetch: fetchDashboard,
    };
}
