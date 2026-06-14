import { useState, useEffect, useCallback } from 'react';
import type { Transaction, TransactionStatus } from '@/types';
import { adminService } from '@/services/adminService';
import { getErrorMessage } from '@/lib/utils';

/**
 * Hook pour la liste paginée et filtrable de toutes les transactions (admin).
 * Contrairement à useTransferHistory (côté utilisateur), le filtre par statut
 * est ici envoyé directement au backend via le paramètre `status`.
 *
 * @example
 * const { transactions, isLoading, statusFilter, setStatusFilter, page, totalPages, setPage } = useAdminTransactions();
 */
export function useAdminTransactions(size = 20) {
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [statusFilter, setStatusFilter] = useState<TransactionStatus | 'ALL'>('ALL');
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPage = useCallback(async (pageNumber: number, status: TransactionStatus | 'ALL') => {
        setIsLoading(true);
        setError(null);
        try {
            const result = await adminService.getTransactions(pageNumber, size, status);
            setTransactions(result.content);
            setTotalPages(result.totalPages);
            setTotalElements(result.totalElements);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, [size]);

    useEffect(() => {
        fetchPage(page, statusFilter);
    }, [page, statusFilter, fetchPage]);

    /**
     * Change le filtre de statut et revient à la page 0
     * (évite d'être bloqué sur une page hors limites).
     */
    const changeStatusFilter = (status: TransactionStatus | 'ALL') => {
        setStatusFilter(status);
        setPage(0);
    };

    return {
        transactions,
        page,
        totalPages,
        totalElements,
        setPage,
        statusFilter,
        setStatusFilter: changeStatusFilter,
        isLoading,
        error,
    };
}