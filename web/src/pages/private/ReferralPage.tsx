import { useTranslation } from 'react-i18next';
import ReferralCard from '@/components/referral/ReferralCard';

/**
 * Page dédiée au parrainage.
 * Accessible via /app/parrainage
 */
export default function ReferralPage() {
    const { t } = useTranslation('referral');

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            {/* En-tête de page */}
            <div>
                <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
                    {t('pageTitle', 'Parrainage')}
                </h1>
                <p className="text-gray-500 dark:text-gray-400 mt-1">
                    {t('pageSubtitle', 'Invitez vos proches et gagnez 5€ de réduction chacun')}
                </p>
            </div>

            {/* Carte de parrainage */}
            <ReferralCard />

            {/* Explication du fonctionnement */}
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6">
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
                    {t('howItWorks', 'Comment ça marche ?')}
                </h2>
                <div className="space-y-4">
                    <div className="flex gap-4">
                        <div className="flex-shrink-0 w-8 h-8 bg-indigo-100 dark:bg-indigo-900/50 rounded-full flex items-center justify-center text-indigo-600 dark:text-indigo-400 font-bold text-sm">
                            1
                        </div>
                        <div>
                            <p className="text-sm font-medium text-gray-900 dark:text-white">
                                {t('step1Title', 'Partagez votre code')}
                            </p>
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                                {t('step1Desc', 'Copiez votre code de parrainage et partagez-le avec vos proches')}
                            </p>
                        </div>
                    </div>
                    <div className="flex gap-4">
                        <div className="flex-shrink-0 w-8 h-8 bg-indigo-100 dark:bg-indigo-900/50 rounded-full flex items-center justify-center text-indigo-600 dark:text-indigo-400 font-bold text-sm">
                            2
                        </div>
                        <div>
                            <p className="text-sm font-medium text-gray-900 dark:text-white">
                                {t('step2Title', 'Ils s\'inscrivent avec votre code')}
                            </p>
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                                {t('step2Desc', 'Vos proches saisissent votre code lors de leur inscription')}
                            </p>
                        </div>
                    </div>
                    <div className="flex gap-4">
                        <div className="flex-shrink-0 w-8 h-8 bg-indigo-100 dark:bg-indigo-900/50 rounded-full flex items-center justify-center text-indigo-600 dark:text-indigo-400 font-bold text-sm">
                            3
                        </div>
                        <div>
                            <p className="text-sm font-medium text-gray-900 dark:text-white">
                                {t('step3Title', 'Recevez 5€ de réduction')}
                            </p>
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                                {t('step3Desc', 'Dès leur premier transfert, vous recevez chacun un coupon de 5€ de réduction')}
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
