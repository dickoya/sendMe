import { useState, useEffect, useCallback } from 'react';
import type { UserResponse } from '@/types';
import { adminService } from '@/services/adminService';
import { getErrorMessage } from '@/lib/utils';
import type { UserFiltersState } from '@/components/admin/UserFilters';

/**
 * Hook pour la gestion paginée des utilisateurs admin avec filtres avancés.
 *
 * @example
 * const { users, isLoading, page, totalPages, setPage, filters, setFilters, toggleStatus, batchToggle } = useAdminUsers();
 */
export function useAdminUsers(size = 20) {
    const [users, setUsers] = useState<UserResponse[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [filters, setFilters] = useState<UserFiltersState>({
        search: '',
        status: 'ALL',
        dateFrom: '',
        dateTo: '',
        sortBy: 'createdAt',
        sortDir: 'desc',
    });

    const fetchPage = useCallback(async (pageNumber: number, currentFilters: UserFiltersState) => {
        setIsLoading(true);
        setError(null);
        try {
            const hasFilters =
                currentFilters.search !== '' ||
                currentFilters.status !== 'ALL' ||
                currentFilters.dateFrom !== '' ||
                currentFilters.dateTo !== '' ||
                currentFilters.sortBy !== 'createdAt' ||
                currentFilters.sortDir !== 'desc';

            let result;
            if (hasFilters) {
                result = await adminService.searchUsers(pageNumber, size, currentFilters);
            } else {
                result = await adminService.getUsers(pageNumber, size);
            }
            setUsers(result.content);
            setTotalPages(result.totalPages);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, [size]);

    useEffect(() => {
        fetchPage(page, filters);
    }, [page, filters, fetchPage]);

    const changeFilters = (newFilters: UserFiltersState) => {
        setFilters(newFilters);
        setPage(0);
    };

    /**
     * Active/désactive un utilisateur et met à jour la liste locale
     * sans recharger toute la page.
     */
    const toggleStatus = async (id: number) => {
        const updated = await adminService.toggleUserStatus(id);
        setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)));
        return updated;
    };

    /**
     * Active ou désactive plusieurs utilisateurs en masse.
     */
    const batchToggle = async (ids: number[], active: boolean) => {
        await adminService.batchToggleUsers(ids, active);
        // Recharger la page courante
        await fetchPage(page, filters);
    };

    return {
        users,
        page,
        totalPages,
        setPage,
        filters,
        setFilters: changeFilters,
        isLoading,
        error,
        toggleStatus,
        batchToggle,
    };
}
