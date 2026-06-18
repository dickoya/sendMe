import { useState, useEffect, useCallback } from 'react';
import { referralService } from '@/services/referralService';
import type { ReferralCodeResponse, ReferralCouponResponse } from '@/types';

/**
 * Hook React pour gérer les données de parrainage.
 */
export function useReferral() {
    const [referralInfo, setReferralInfo] = useState<ReferralCodeResponse | null>(null);
    const [coupons, setCoupons] = useState<ReferralCouponResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchData = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const [info, couponsData] = await Promise.all([
                referralService.getMyCode(),
                referralService.getCoupons(),
            ]);
            setReferralInfo(info);
            setCoupons(couponsData);
        } catch (err) {
            setError('Impossible de charger les informations de parrainage');
            console.error('[PARRAINAGE] Erreur de chargement:', err);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    /**
     * Copie le code de parrainage dans le presse-papier.
     */
    const copyReferralCode = useCallback(async () => {
        if (!referralInfo?.referralCode) return;
        try {
            await navigator.clipboard.writeText(referralInfo.referralCode);
        } catch {
            // Fallback pour les navigateurs qui ne supportent pas l'API Clipboard
            const textarea = document.createElement('textarea');
            textarea.value = referralInfo.referralCode;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
        }
    }, [referralInfo]);

    /**
     * Partage le lien de parrainage via WhatsApp.
     */
    const shareWhatsApp = useCallback(() => {
        if (!referralInfo?.shareLink) return;
        const message = encodeURIComponent(
            `💰 Envoie de l'argent facilement avec SendMe ! Utilise mon code de parrainage et reçois 5€ de réduction sur ton premier transfert : ${referralInfo.shareLink}`
        );
        window.open(`https://wa.me/?text=${message}`, '_blank');
    }, [referralInfo]);

    /**
     * Partage le lien de parrainage par email.
     */
    const shareEmail = useCallback(() => {
        if (!referralInfo?.shareLink) return;
        const subject = encodeURIComponent('Rejoins-moi sur SendMe !');
        const body = encodeURIComponent(
            `Salut !\n\nJ'utilise SendMe pour envoyer de l'argent facilement. \n\nAvec mon code de parrainage, tu reçois 5€ de réduction sur ton premier transfert :\n${referralInfo.shareLink}\n\nÀ bientôt !`
        );
        window.open(`mailto:?subject=${subject}&body=${body}`, '_blank');
    }, [referralInfo]);

    return {
        referralInfo,
        coupons,
        loading,
        error,
        refresh: fetchData,
        copyReferralCode,
        shareWhatsApp,
        shareEmail,
    };
}
