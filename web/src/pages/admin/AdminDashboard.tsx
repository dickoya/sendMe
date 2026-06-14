import { Users, ArrowRightLeft, TrendingUp, Wallet, Wifi, AlertCircle } from 'lucide-react';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import StatCard from '@/components/admin/StatCard';
import ChannelChart from '@/components/admin/ChannelChart';
import { useAdminDashboard } from '@/hooks/useAdminDashboard';
import { formatCurrency } from '@/utils/formatCurrency';

/**
 * Tableau de bord administrateur — vue d'ensemble de la plateforme.
 *
 * Affiche :
 * - Statistiques utilisateurs (total, actifs, inactifs)
 * - Statistiques transactions (par statut, taux de succès)
 * - Volume financier (total, frais, statistiques du jour)
 * - Répartition par canal (graphique en barres)
 * - Connexions SSE actives (monitoring temps réel)
 */
function AdminDashboard() {
    const { dashboard, isLoading, error } = useAdminDashboard();

    if (isLoading) {
        return <Spinner fullPage label="Chargement du tableau de bord..." />;
    }

    if (error || !dashboard) {
        return (
            <div className="flex flex-col items-center gap-2 py-12 text-center text-danger-600">
                <AlertCircle size={24} />
                <p>{error ?? 'Impossible de charger le tableau de bord'}</p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-6">
            {/* ── Statistiques utilisateurs ──────────────────────────────────── */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                <StatCard
                    label="Utilisateurs totaux"
                    value={dashboard.totalUsers}
                    icon={<Users size={18} />}
                    subtext={`${dashboard.activeUsers} actifs · ${dashboard.inactiveUsers} suspendus`}
                />
                <StatCard
                    label="Volume total"
                    value={formatCurrency(dashboard.totalVolume, 'EUR')}
                    icon={<Wallet size={18} />}
                    iconClassName="bg-accent-50 text-accent-600"
                    subtext={`${formatCurrency(dashboard.totalFees, 'EUR')} de frais collectés`}
                />
                <StatCard
                    label="Connexions actives"
                    value={dashboard.activeSSEConnections}
                    icon={<Wifi size={18} />}
                    iconClassName="bg-purple-50 text-purple-600"
                    subtext="Utilisateurs connectés en temps réel"
                />
            </div>

            {/* ── Statistiques transactions ──────────────────────────────────── */}
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                <StatCard
                    label="Transactions totales"
                    value={dashboard.totalTransactions}
                    icon={<ArrowRightLeft size={18} />}
                />
                <StatCard
                    label="Complétées"
                    value={dashboard.completedTransactions}
                    icon={<TrendingUp size={18} />}
                    iconClassName="bg-accent-50 text-accent-600"
                />
                <StatCard
                    label="Échouées"
                    value={dashboard.failedTransactions}
                    icon={<AlertCircle size={18} />}
                    iconClassName="bg-danger-50 text-danger-600"
                />
                <StatCard
                    label="Taux de succès"
                    value={`${dashboard.successRate.toFixed(1)}%`}
                    icon={<TrendingUp size={18} />}
                    iconClassName="bg-warning-50 text-warning-600"
                />
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
                {/* ── Activité du jour ────────────────────────────────────────────── */}
                <Card title="Activité du jour">
                    <div className="flex flex-col gap-4">
                        <div className="flex items-center justify-between rounded-xl bg-brand-50 px-4 py-3">
                            <span className="text-sm font-medium text-brand-700">Volume aujourd'hui</span>
                            <span className="text-lg font-bold text-brand-700">
                {formatCurrency(dashboard.todayVolume, 'EUR')}
              </span>
                        </div>
                        <div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3">
                            <span className="text-sm font-medium text-gray-700">Transactions aujourd'hui</span>
                            <span className="text-lg font-bold text-gray-900">{dashboard.todayTransactions}</span>
                        </div>
                        <div className="flex items-center justify-between rounded-xl bg-gray-50 px-4 py-3">
                            <span className="text-sm font-medium text-gray-700">En attente / en cours</span>
                            <span className="text-lg font-bold text-gray-900">{dashboard.pendingTransactions}</span>
                        </div>
                        <div className="flex items-center justify-between rounded-xl bg-purple-50 px-4 py-3">
                            <span className="text-sm font-medium text-purple-700">Remboursées</span>
                            <span className="text-lg font-bold text-purple-700">{dashboard.refundedTransactions}</span>
                        </div>
                    </div>
                </Card>

                {/* ── Répartition par canal ────────────────────────────────────────── */}
                <Card title="Répartition par canal">
                    <ChannelChart data={dashboard.transactionsByChannel} />
                </Card>
            </div>
        </div>
    );
}

export default AdminDashboard;