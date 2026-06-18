import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Send, Users, History, Star } from 'lucide-react';
import Card from '@/components/ui/Card';
import type { UserDashboardStats } from '@/services/dashboardService';

interface QuickActionsProps {
    stats: UserDashboardStats | null;
}

/**
 * Actions rapides et transferts favoris pour le tableau de bord.
 */
export default function QuickActions({ stats }: QuickActionsProps) {
    const { t } = useTranslation('dashboard');

    const quickActions = [
        {
            label: t('quickActions.newTransfer'),
            icon: <Send size={18} />,
            to: '/transfer',
            color: 'bg-blue-50 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400',
        },
        {
            label: t('quickActions.manageBeneficiaries'),
            icon: <Users size={18} />,
            to: '/beneficiaries',
            color: 'bg-purple-50 text-purple-600 dark:bg-purple-900/30 dark:text-purple-400',
        },
        {
            label: t('quickActions.viewHistory'),
            icon: <History size={18} />,
            to: '/history',
            color: 'bg-amber-50 text-amber-600 dark:bg-amber-900/30 dark:text-amber-400',
        },
    ];

    return (
        <Card title={t('quickActions.title')}>
            <div className="flex flex-col gap-3">
                {quickActions.map((action) => (
                    <Link
                        key={action.to}
                        to={action.to}
                        className="flex items-center gap-3 rounded-lg p-3 transition-colors hover:bg-gray-50 dark:hover:bg-[#1a1a1a]"
                    >
                        <div className={`flex h-9 w-9 items-center justify-center rounded-lg ${action.color}`}>
                            {action.icon}
                        </div>
                        <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                            {action.label}
                        </span>
                    </Link>
                ))}

                {/* Bénéficiaire favori */}
                {stats?.favoriteBeneficiary && (
                    <div className="mt-2 rounded-lg border border-amber-200 bg-amber-50 p-3 dark:border-amber-900/50 dark:bg-amber-900/20">
                        <div className="flex items-center gap-2">
                            <Star size={14} className="fill-amber-400 text-amber-400" />
                            <p className="text-xs font-medium text-amber-700 dark:text-amber-400">
                                {t('quickActions.favoriteBeneficiary')}
                            </p>
                        </div>
                        <p className="mt-1 text-sm font-semibold text-amber-900 dark:text-amber-300">
                            {stats.favoriteBeneficiary.firstName} {stats.favoriteBeneficiary.lastName}
                        </p>
                        <p className="text-xs text-amber-600 dark:text-amber-500">
                            {stats.favoriteBeneficiary.count} {t('quickActions.transactions')}
                        </p>
                    </div>
                )}
            </div>
        </Card>
    );
}
