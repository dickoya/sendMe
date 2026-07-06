import { useTranslation } from 'react-i18next';
import { Users, ArrowRightLeft, TrendingUp, Wallet, Wifi, AlertCircle } from 'lucide-react';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import StatCard from '@/components/admin/StatCard';
import ChannelPieChart from '@/components/admin/ChannelPieChart';
import VolumeChart from '@/components/admin/VolumeChart';
import SuccessRateChart from '@/components/admin/SuccessRateChart';
import { useAdminDashboard } from '@/hooks/useAdminDashboard';
import { useAdminAnalytics, type PeriodOption } from '@/hooks/useAdminAnalytics';
import { formatCurrency } from '@/utils/formatCurrency';
import { cn } from '@/lib/utils';

const PERIOD_OPTIONS: PeriodOption[] = [7, 30, 90];

/**
 * Tableau de bord administrateur.
 *
 * Migration i18n (Phase 11) : namespace "admin" (clés dashboard.*),
 * "common" pour l'état de chargement.
 *
 * Étape : ajout des graphiques interactifs (volume,
 * répartition par canal, taux de succès) avec filtres de période.
 */
function AdminDashboard() {
    const { t } = useTranslation('admin');
    const { t: tCommon } = useTranslation('common');
    const { dashboard, exchangeRateDebug, allExchangeRates, exchangeRateConfig, isLoading, error } = useAdminDashboard();
    const { volumeData, successRateData, period, setPeriod, isLoading: isAnalyticsLoading } = useAdminAnalytics(30);

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
        <div className="flex flex-col gap-6 ">
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
                <ChannelPieChart data={dashboard.transactionsByChannel} />
            </div>

            {/* ── Graphiques d'évolution (volume + taux de succès) ────────────────── */}
            <div className="flex flex-col gap-4">
                <div className="flex items-center justify-end gap-2">
                    <span className="text-sm text-gray-500">{t('dashboard.periodFilter.label')}</span>
                    {PERIOD_OPTIONS.map((opt) => (
                        <button
                            key={opt}
                            onClick={() => setPeriod(opt)}
                            className={cn(
                                'rounded-lg px-3 py-1.5 text-sm font-medium transition-colors',
                                period === opt
                                    ? 'bg-accent-600 text-white'
                                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700'
                            )}
                        >
                            {t('dashboard.periodFilter.days', { count: opt })}
                        </button>
                    ))}
                </div>

                {isAnalyticsLoading ? (
                    <Spinner label={tCommon('states.loading')} />
                ) : (
                    <div className="grid gap-6 lg:grid-cols-2">
                        <VolumeChart data={volumeData} />
                        <SuccessRateChart data={successRateData} />
                    </div>
                )}
            </div>


                {/* ── Taux de change : toutes les paires ──────────────────────────────── */}
                {allExchangeRates.length > 0 && (
                    <Card title={t('dashboard.allExchangeRates.title')}>
                        <div className="overflow-x-auto max-h-96 overflow-y-auto">
                            <table className="w-full text-left text-sm">
                                <thead>
                                <tr className="border-b border-gray-200 text-xs font-medium uppercase text-gray-500">
                                    <th className="px-3 py-2">{t('dashboard.allExchangeRates.pair')}</th>
                                    <th className="px-3 py-2">{t('dashboard.allExchangeRates.rawRate')}</th>
                                    <th className="px-3 py-2">{t('dashboard.allExchangeRates.marginRate')}</th>
                                    <th className="px-3 py-2">{t('dashboard.allExchangeRates.cacheTimestamp')}</th>
                                </tr>
                                </thead>
                                <tbody>
                                {allExchangeRates.map((rate) => (
                                    <tr key={`${rate.from}-${rate.to}`} className="border-b border-gray-100 hover:bg-gray-50">
                                        <td className="whitespace-nowrap px-3 py-2 font-medium text-gray-900">
                                            {rate.from} → {rate.to}
                                        </td>
                                        <td className="whitespace-nowrap px-3 py-2 text-gray-700">
                                            {formatRateDebug(rate.rawRate)}
                                        </td>
                                        <td className="whitespace-nowrap px-3 py-2 text-accent-700">
                                            {formatRateDebug(rate.marginRate)}
                                        </td>
                                        <td className="whitespace-nowrap px-3 py-2 text-xs text-gray-500">
                                            {formatCacheTimestamp(rate.cacheTimestamp)}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </Card>
                )}


            {/* ── Debug EUR → XOF (carte existante) ──────────────────────────────── */}
            {exchangeRateDebug && (
                <Card title={t('dashboard.exchangeRateDebug.title')}>
                    <div className="grid gap-4 md:grid-cols-3">
                        <div className="rounded-lg bg-gray-50 px-4 py-3">
                            <p className="text-xs font-medium uppercase text-gray-500">
                                {t('dashboard.exchangeRateDebug.rawRate')}
                            </p>
                            <p className="mt-1 text-lg font-semibold text-gray-900">
                                {formatRateDebug(exchangeRateDebug.rawRate)}
                            </p>
                        </div>
                        <div className="rounded-lg bg-accent-50 px-4 py-3">
                            <p className="text-xs font-medium uppercase text-accent-700">
                                {t('dashboard.exchangeRateDebug.marginRate')}
                            </p>
                            <p className="mt-1 text-lg font-semibold text-accent-700">
                                {formatRateDebug(exchangeRateDebug.marginRate)}
                            </p>
                        </div>
                        <div className="rounded-lg bg-brand-50 px-4 py-3">
                            <p className="text-xs font-medium uppercase text-brand-700">
                                {t('dashboard.exchangeRateDebug.cacheTimestamp')}
                            </p>
                            <p className="mt-1 break-words text-sm font-semibold text-brand-700">
                                {formatCacheTimestamp(exchangeRateDebug.cacheTimestamp)}
                            </p>
                        </div>
                    </div>
                    <p className="mt-3 text-sm text-gray-500">
                        {t('dashboard.exchangeRateDebug.pair', {
                            from: exchangeRateDebug.from,
                            to: exchangeRateDebug.to,
                        })}
                    </p>

                    {/* ── Configuration actuelle ──────────────────────────────────── */}
                    {exchangeRateConfig && (
                        <div className="mt-4 grid gap-4 md:grid-cols-2">
                            <div className="rounded-lg bg-orange-50 px-4 py-3">
                                <p className="text-xs font-medium uppercase text-orange-700">
                                    {t('dashboard.exchangeRateDebug.cacheDuration')}
                                </p>
                                <p className="mt-1 text-lg font-semibold text-orange-700">
                                    {exchangeRateConfig.cacheDurationMinutes} min
                                </p>
                            </div>
                            <div className="rounded-lg bg-teal-50 px-4 py-3">
                                <p className="text-xs font-medium uppercase text-teal-700">
                                    {t('dashboard.exchangeRateDebug.marginPercent')}
                                </p>
                                <p className="mt-1 text-lg font-semibold text-teal-700">
                                    {exchangeRateConfig.marginPercent}%
                                </p>
                            </div>
                        </div>
                    )}
                </Card>
            )}


        </div>
    );
}

function formatRateDebug(rate: number | null) {
    if (rate == null) return 'N/A';
    return rate.toLocaleString(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 6,
    });
}

function formatCacheTimestamp(timestamp: string) {
    const date = new Date(timestamp);
    if (Number.isNaN(date.getTime())) return timestamp;
    return date.toLocaleString();
}

export default AdminDashboard;
