import { useState, useEffect, useCallback } from 'react';
import { dashboardService, type UserDashboardStats, type TransferEvolution, type LiveExchangeRate, type ExchangeRateHistoryPoint } from '@/services/dashboardService';
import { getErrorMessage } from '@/lib/utils';

/**
 * Hook pour le tableau de bord utilisateur.
 * Récupère les statistiques personnelles, l'évolution des transferts
 * et les taux de change en direct.
 */
export function useDashboard() {
    const [stats, setStats] = useState<UserDashboardStats | null>(null);
    const [evolution, setEvolution] = useState<TransferEvolution | null>(null);
    const [exchangeRates, setExchangeRates] = useState<LiveExchangeRate[]>([]);
    const [rateHistory, setRateHistory] = useState<ExchangeRateHistoryPoint[]>([]);
    const [selectedRatePair, setSelectedRatePair] = useState<{ from: string; to: string }>({ from: 'EUR', to: 'XOF' });
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchAll = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const [statsData, evolutionData, ratesData] = await Promise.all([
                dashboardService.getStats(),
                dashboardService.getTransferEvolution(),
                dashboardService.getLiveExchangeRates(),
            ]);
            setStats(statsData);
            setEvolution(evolutionData);
            setExchangeRates(ratesData);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, []);

    const fetchRateHistory = useCallback(async (from: string, to: string) => {
        try {
            const history = await dashboardService.getExchangeRateHistory(from, to);
            setRateHistory(history);
            setSelectedRatePair({ from, to });
        } catch (err) {
            console.error('Erreur chargement historique taux:', err);
        }
    }, []);

    useEffect(() => {
        fetchAll();
    }, [fetchAll]);

    useEffect(() => {
        fetchRateHistory(selectedRatePair.from, selectedRatePair.to);
    }, [selectedRatePair.from, selectedRatePair.to, fetchRateHistory]);

    return {
        stats,
        evolution,
        exchangeRates,
        rateHistory,
        selectedRatePair,
        setSelectedRatePair,
        isLoading,
        error,
        refetch: fetchAll,
        fetchRateHistory,
    };
}
