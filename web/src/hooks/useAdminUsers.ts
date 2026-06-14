import { useState, useEffect, useCallback } from 'react';
import type { UserResponse } from '@/types';
import { adminService } from '@/services/adminService';
import { getErrorMessage } from '@/lib/utils';

/**
 * Hook pour la gestion paginée des utilisateurs admin.
 *
 * @example
 * const { users, isLoading, page, totalPages, setPage, toggleStatus } = useAdminUsers();
 */
export function useAdminUsers(size = 20) {
    const [users, setUsers] = useState<UserResponse[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPage = useCallback(async (pageNumber: number) => {
        setIsLoading(true);
        setError(null);
        try {
            const result = await adminService.getUsers(pageNumber, size);
            setUsers(result.content);
            setTotalPages(result.totalPages);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, [size]);

    useEffect(() => {
        fetchPage(page);
    }, [page, fetchPage]);

    /**
     * Active/désactive un utilisateur et met à jour la liste locale
     * sans recharger toute la page.
     */
    const toggleStatus = async (id: number) => {
        const updated = await adminService.toggleUserStatus(id);
        setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)));
        return updated;
    };

    return { users, page, totalPages, setPage, isLoading, error, toggleStatus };
}