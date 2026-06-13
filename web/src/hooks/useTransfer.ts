import { useState, useEffect, useCallback } from 'react';
import type { Transaction, Page } from '@/types';
import { transferService } from '@/services/transferService';
import { getErrorMessage } from '@/lib/utils';

/**
 * Hook pour récupérer l'historique paginé des transactions.
 *
 * @example
 * const { transactions, isLoading, page, totalPages, setPage } = useTransferHistory();
 */
export function useTransferHistory(initialSize = 10) {
    const [data, setData] = useState<Page<Transaction> | null>(null);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPage = useCallback(async (pageNumber: number) => {
        setIsLoading(true);
        setError(null);
        try {
            const result = await transferService.getHistory(pageNumber, initialSize);
            setData(result);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, [initialSize]);

    useEffect(() => {
        fetchPage(page);
    }, [page, fetchPage]);

    return {
        transactions: data?.content ?? [],
        totalElements: data?.totalElements ?? 0,
        totalPages: data?.totalPages ?? 0,
        page,
        setPage,
        isLoading,
        error,
        refetch: () => fetchPage(page),
    };
}