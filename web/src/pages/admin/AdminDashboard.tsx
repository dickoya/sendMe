import { useTranslation } from 'react-i18next';
import { Users, ArrowRightLeft, TrendingUp, Wallet, Wifi, AlertCircle } from 'lucide-react';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import StatCard from '@/components/admin/StatCard';
import ChannelChart from '@/components/admin/ChannelChart';
import { useAdminDashboard } from '@/hooks/useAdminDashboard';
import { formatCurrency } from '@/utils/formatCurrency';

/**
 * Tableau de bord administrateur.
 *
 * Migration i18n (Phase 11) : namespace "admin" (clés dashboard.*),
 * "common" pour l'état de chargement.
 */
function AdminDashboard() {
    const { t } = useTranslation('admin');
    const { t: tCommon } = useTranslation('common');
    const { dashboard, isLoading, error } = useAdminDashboard();

    if (isLoading) {
        return <Spinner fullPage label={tCommon('states.loading')} />;
    }

    if (error || !dashboard) {
        return (
            <div className="flex flex-col items-center gap-2 py-12 text-center text-danger-600">
                <AlertCircle size={24} />
                <p>{error ?? t('dashboard.errors.loadFailed')}</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-6">
            {/* ── Statistiques utilisateurs ──────────────────────────────────── */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <StatCard
                    label={t('dashboard.stats.totalUsers')}
                    value={dashboard.totalUsers}
                    icon={<Users size={18} />}
                    subtext={t('dashboard.stats.activeInactive', {
                        active: dashboard.activeUsers,
                        inactive: dashboard.inactiveUsers,
                    })}
                />
                <StatCard
                    label={t('dashboard.stats.totalVolume')}
                    value={formatCurrency(dashboard.totalVolume, 'EUR')}
                    icon={<Wallet size={18} />}
                    iconClassName="bg-accent-50 text-accent-600"
                    subtext={t('dashboard.stats.totalFeesCollected', {
                        amount: formatCurrency(dashboard.totalFees, 'EUR'),
                    })}
                />
                <StatCard
                    label={t('dashboard.stats.activeConnections')}
                    value={dashboard.activeSSEConnections}
                    icon={<Wifi size={18} />}
                    iconClassName="bg-purple-50 text-purple-600"
                    subtext={t('dashboard.stats.activeConnectionsSubtext')}
                />
            </div>

            {/* ── Statistiques transactions ──────────────────────────────────── */}
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                <StatCard
                    label={t('dashboard.stats.totalTransactions')}
                    value={dashboard.totalTransactions}
                    icon={<ArrowRightLeft size={18} />}
                />
                <StatCard
                    label={t('dashboard.stats.completed')}
                    value={dashboard.completedTransactions}
                    icon={<TrendingUp size={18} />}
                    iconClassName="bg-accent-50 text-accent-600"
                />
                <StatCard
                    label={t('dashboard.stats.failed')}
                    value={dashboard.failedTransactions}
                    icon={<AlertCircle size={18} />}
                    iconClassName="bg-danger-50 text-danger-600"
                />
                <StatCard
                    label={t('dashboard.stats.successRate')}
                    value={`${dashboard.successRate.toFixed(1)}%`}
                    icon={<TrendingUp size={18} />}
                    iconClassName="bg-warning-50 text-warning-600"
                />
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
                {/* ── Activité du jour ────────────────────────────────────────────── */}
                <Card title={t('dashboard.todayActivity.title')}>
                    <div className="flex flex-col gap-4">
                        <div className="flex items-center justify-between rounded-xl bg-brand-50 px-4 py-3">
                            <span className="text-sm font-medium text-brand-700">{t('dashboard.todayActivity.volumeToday')}</span>
                            <span className="text-lg font-bold text-brand-700">
                {formatCurrency(dashboard.todayVolume, 'EUR')}
              </span>
                        </div>
                        <div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3">
                            <span className="text-sm font-medium text-gray-700">{t('dashboard.todayActivity.transactionsToday')}</span>
                            <span className="text-lg font-bold text-gray-900">{dashboard.todayTransactions}</span>
                        </div>
                        <div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3">
                            <span className="text-sm font-medium text-gray-700">{t('dashboard.todayActivity.pending')}</span>
                            <span className="text-lg font-bold text-gray-900">{dashboard.pendingTransactions}</span>
                        </div>
                        <div className="flex items-center justify-between rounded-xl bg-purple-50 px-4 py-3">
                            <span className="text-sm font-medium text-purple-700">{t('dashboard.todayActivity.refunded')}</span>
                            <span className="text-lg font-bold text-purple-700">{dashboard.refundedTransactions}</span>
                        </div>
                    </div>
                </Card>

                {/* ── Répartition par canal ────────────────────────────────────────── */}
                <Card title={t('dashboard.channelChart.title')}>
                    <ChannelChart data={dashboard.transactionsByChannel} />
                </Card>
            </div>
        </div>
    );
}

export default AdminDashboard;