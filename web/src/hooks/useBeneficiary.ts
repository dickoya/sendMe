import { useState, useEffect, useCallback } from 'react';
import type { Beneficiary, BeneficiaryFormData } from '@/types';
//import { beneficiaryService } from '@/services/beneficiaryService';
import { beneficiaryService } from '@/services/beneficiaryService'
import { getErrorMessage } from '@/lib/utils';

/**
 * Hook gérant le CRUD complet des bénéficiaires.
 *
 * @example
 * const { beneficiaries, isLoading, createBeneficiary, removeBeneficiary } = useBeneficiary();
 */
export function useBeneficiary() {
    const [beneficiaries, setBeneficiaries] = useState<Beneficiary[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchAll = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await beneficiaryService.getAll();
            setBeneficiaries(data);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchAll();
    }, [fetchAll]);

    /**
     * Crée un bénéficiaire et l'ajoute à la liste locale.
     * @throws l'erreur API pour affichage dans le formulaire appelant
     */
    const createBeneficiary = async (payload: BeneficiaryFormData) => {
        const created = await beneficiaryService.create(payload);
        setBeneficiaries((prev) => [created, ...prev]);
        return created;
    };

    /**
     * Met à jour un bénéficiaire et remplace l'entrée locale.
     */
    const updateBeneficiary = async (id: number, payload: BeneficiaryFormData) => {
        const updated = await beneficiaryService.update(id, payload);
        setBeneficiaries((prev) => prev.map((b) => (b.id === id ? updated : b)));
        return updated;
    };

    /**
     * Supprime un bénéficiaire et le retire de la liste locale.
     */
    const removeBeneficiary = async (id: number) => {
        await beneficiaryService.remove(id);
        setBeneficiaries((prev) => prev.filter((b) => b.id !== id));
    };

    return {
        beneficiaries,
        isLoading,
        error,
        refetch: fetchAll,
        createBeneficiary,
        updateBeneficiary,
        removeBeneficiary,
    };
}