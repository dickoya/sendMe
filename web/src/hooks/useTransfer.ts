import { useState, useEffect, useCallback } from 'react';
import type { Transaction, Page, TransactionStatus } from '@/types';
import { transferService } from '@/services/transferService';
import { getErrorMessage } from '@/lib/utils';

/**
 * Hook pour récupérer l'historique paginé des transactions,
 * avec filtrage local par statut.
 *
 * Note : le filtrage par statut est fait côté client car
 * GET /api/transactions ne supporte pas de paramètre status
 * (seul l'endpoint admin le supporte). Pour un historique
 * utilisateur, le volume reste gérable page par page.
 *
 * @example
 * const { transactions, isLoading, page, totalPages, setPage, statusFilter, setStatusFilter } = useTransferHistory();
 */
export function useTransferHistory(initialSize = 10) {
    const [data, setData] = useState<Page<Transaction> | null>(null);
    const [page, setPage] = useState(0);
    const [statusFilter, setStatusFilter] = useState<TransactionStatus | 'ALL'>('ALL');
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

    const allTransactions = data?.content ?? [];
    const filteredTransactions =
        statusFilter === 'ALL'
            ? allTransactions
            : allTransactions.filter((tx) => tx.status === statusFilter);

    return {
        transactions: filteredTransactions,
        totalElements: data?.totalElements ?? 0,
        totalPages: data?.totalPages ?? 0,
        page,
        setPage,
        statusFilter,
        setStatusFilter,
        isLoading,
        error,
        refetch: () => fetchPage(page),
    };
}