import { useState, useEffect, useCallback } from 'react';
import type { VolumeDataPoint, SuccessRateDataPoint } from '@/types';
import { adminService } from '@/services/adminService';
import { getErrorMessage } from '@/lib/utils';

export type PeriodOption = 7 | 30 | 90;

/**
 * Hook pour récupérer les données analytiques (volume + taux de succès)
 * du dashboard admin, avec sélection de période (7j / 30j / 90j).
 *
 * @example
 * const { volumeData, successRateData, period, setPeriod, isLoading } = useAdminAnalytics();
 */
export function useAdminAnalytics(initialPeriod: PeriodOption = 30) {
    const [period, setPeriod] = useState<PeriodOption>(initialPeriod);
    const [volumeData, setVolumeData] = useState<VolumeDataPoint[]>([]);
    const [successRateData, setSuccessRateData] = useState<SuccessRateDataPoint[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchAnalytics = useCallback(async (days: number) => {
        setIsLoading(true);
        setError(null);
        try {
            const [volume, successRate] = await Promise.all([
                adminService.getVolumeData(days),
                adminService.getSuccessRateData(days),
            ]);
            setVolumeData(volume);
            setSuccessRateData(successRate);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchAnalytics(period);
    }, [period, fetchAnalytics]);

    return {
        volumeData,
        successRateData,
        period,
        setPeriod,
        isLoading,
        error,
        refetch: () => fetchAnalytics(period),
    };
}
