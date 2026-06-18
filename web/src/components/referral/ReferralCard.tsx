import { useReferral } from '@/hooks/useReferral';
import { useTranslation } from 'react-i18next';
import { Gift, Copy, Share2, Users, Ticket } from 'lucide-react';

/**
 * Carte de parrainage — affiche le code de parrainage et les statistiques.
 * Peut être intégrée dans le tableau de bord ou dans une page dédiée.
 */
export default function ReferralCard() {
    const { t } = useTranslation('referral');
    const { referralInfo, coupons, loading, copyReferralCode, shareWhatsApp, shareEmail } =
        useReferral();

    if (loading) {
        return (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 animate-pulse">
                <div className="h-6 bg-gray-200 dark:bg-gray-700 rounded w-1/3 mb-4" />
                <div className="h-10 bg-gray-200 dark:bg-gray-700 rounded w-full mb-3" />
                <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-2/3" />
            </div>
        );
    }

    return (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6">
            {/* En-tête */}
            <div className="flex items-center gap-3 mb-5">
                <div className="p-2.5 bg-indigo-100 dark:bg-indigo-900/50 rounded-lg">
                    <Gift className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
                </div>
                <div>
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
                        {t('title', 'Parrainage')}
                    </h3>
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                        {t('subtitle', 'Parrainez vos proches et gagnez des réductions')}
                    </p>
                </div>
            </div>

            {/* Code de parrainage */}
            {referralInfo && (
                <>
                    <div className="mb-5">
                        <label className="block text-sm font-medium text-gray-600 dark:text-gray-400 mb-1.5">
                            {t('yourCode', 'Votre code de parrainage')}
                        </label>
                        <div className="flex gap-2">
                            <div className="flex-1 px-4 py-2.5 bg-gray-50 dark:bg-gray-700/50 border border-dashed border-gray-300 dark:border-gray-600 rounded-lg font-mono text-lg font-bold text-indigo-600 dark:text-indigo-400 tracking-wider select-all">
                                {referralInfo.referralCode}
                            </div>
                            <button
                                onClick={copyReferralCode}
                                className="px-3 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg transition-colors"
                                title={t('copy', 'Copier')}
                            >
                                <Copy className="w-5 h-5" />
                            </button>
                        </div>
                    </div>

                    {/* Statistiques */}
                    <div className="grid grid-cols-3 gap-3 mb-5">
                        <div className="text-center p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
                            <Users className="w-5 h-5 text-green-600 dark:text-green-400 mx-auto mb-1" />
                            <p className="text-xl font-bold text-gray-900 dark:text-white">
                                {referralInfo.totalReferred}
                            </p>
                            <p className="text-xs text-gray-500 dark:text-gray-400">
                                {t('referred', 'Filleuls')}
                            </p>
                        </div>
                        <div className="text-center p-3 bg-amber-50 dark:bg-amber-900/20 rounded-lg">
                            <Ticket className="w-5 h-5 text-amber-600 dark:text-amber-400 mx-auto mb-1" />
                            <p className="text-xl font-bold text-gray-900 dark:text-white">
                                {referralInfo.availableCoupons}
                            </p>
                            <p className="text-xs text-gray-500 dark:text-gray-400">
                                {t('coupons', 'Coupons')}
                            </p>
                        </div>
                        <div className="text-center p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                            <Gift className="w-5 h-5 text-blue-600 dark:text-blue-400 mx-auto mb-1" />
                            <p className="text-xl font-bold text-gray-900 dark:text-white">
                                {referralInfo.totalCouponsEarned}
                            </p>
                            <p className="text-xs text-gray-500 dark:text-gray-400">
                                {t('earned', 'Gagnés')}
                            </p>
                        </div>
                    </div>

                    {/* Boutons de partage */}
                    <div className="flex gap-2">
                        <button
                            onClick={shareWhatsApp}
                            className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-green-500 hover:bg-green-600 text-white rounded-lg transition-colors text-sm font-medium"
                        >
                            <Share2 className="w-4 h-4" />
                            WhatsApp
                        </button>
                        <button
                            onClick={shareEmail}
                            className="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 bg-gray-600 hover:bg-gray-700 text-white rounded-lg transition-colors text-sm font-medium"
                        >
                            <Share2 className="w-4 h-4" />
                            Email
                        </button>
                    </div>
                </>
            )}

            {/* Coupons disponibles */}
            {coupons.length > 0 && (
                <div className="mt-5 pt-5 border-t border-gray-200 dark:border-gray-700">
                    <h4 className="text-sm font-semibold text-gray-900 dark:text-white mb-3">
                        {t('availableCoupons', 'Mes coupons de réduction')}
                    </h4>
                    <div className="space-y-2">
                        {coupons.map((coupon) => (
                            <div
                                key={coupon.id}
                                className={`flex items-center justify-between p-3 rounded-lg border ${
                                    coupon.isUsed
                                        ? 'border-gray-200 dark:border-gray-700 opacity-50'
                                        : 'border-green-200 dark:border-green-800 bg-green-50 dark:bg-green-900/20'
                                }`}
                            >
                                <div className="flex items-center gap-3">
                                    <Ticket className="w-4 h-4 text-indigo-500" />
                                    <div>
                                        <p className="text-sm font-medium text-gray-900 dark:text-white">
                                            {coupon.discountAmount} {coupon.currency}
                                        </p>
                                        <p className="text-xs text-gray-500 dark:text-gray-400">
                                            {coupon.isUsed
                                                ? t('used', 'Utilisé')
                                                : `${t('expires', 'Expire le')} ${new Date(coupon.expiresAt).toLocaleDateString()}`}
                                        </p>
                                    </div>
                                </div>
                                <span
                                    className={`text-xs font-medium px-2 py-1 rounded-full ${
                                        coupon.couponType === 'SPONSOR'
                                            ? 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300'
                                            : 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300'
                                    }`}
                                >
                                    {coupon.couponType === 'SPONSOR'
                                        ? t('sponsor', 'Parrain')
                                        : t('referred', 'Filleul')}
                                </span>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}
