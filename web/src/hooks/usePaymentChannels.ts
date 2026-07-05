import { useState, useEffect, useCallback } from 'react';
import { paymentChannelService } from '@/services/paymentChannelService';
import type { PaymentChannel, PaymentChannelRequest } from '@/types/paymentChannel';

/**
 * Hook de gestion des canaux de paiement pour l'admin.
 *
 * Fournit :
 * - La liste des canaux avec rechargement
 * - L'ajout d'un nouveau canal
 * - L'activation/désactivation d'un canal
 * - Les états de chargement et d'erreur
 */
export function usePaymentChannels() {
    const [channels, setChannels] = useState<PaymentChannel[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const loadChannels = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await paymentChannelService.getAll();
            setChannels(data);
        } catch (err: any) {
            setError(err?.response?.data?.message || err?.message || 'Erreur de chargement');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        loadChannels();
    }, [loadChannels]);

    /**
     * Recharge la liste des canaux sans afficher le spinner de chargement.
     * Utilisé après un toggle pour éviter de cacher la modale de confirmation.
     */
    const reloadChannelsSilently = useCallback(async () => {
        try {
            const data = await paymentChannelService.getAll();
            setChannels(data);
        } catch (err: any) {
            setError(err?.response?.data?.message || err?.message || 'Erreur de chargement');
        }
    }, []);

    /**
     * Ajoute un nouveau canal de paiement.
     */
    const addChannel = async (request: PaymentChannelRequest): Promise<boolean> => {
        try {
            setError(null);
            await paymentChannelService.add(request);
            await loadChannels();
            return true;
        } catch (err: any) {
            setError(err?.response?.data?.message || err?.message || 'Erreur lors de l\'ajout');
            return false;
        }
    };

    /**
     * Active ou désactive un canal.
     */
    const toggleChannel = async (id: number): Promise<boolean> => {
        try {
            setError(null);
            await paymentChannelService.toggle(id);
            await reloadChannelsSilently();
            return true;
        } catch (err: any) {
            setError(err?.response?.data?.message || err?.message || 'Erreur lors de la modification');
            return false;
        }
    };

    return {
        channels,
        loading,
        error,
        addChannel,
        toggleChannel,
        reload: loadChannels,
    };
}
