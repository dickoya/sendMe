import { Link } from 'react-router-dom';
import { Send, Users, ArrowRight, Wallet, History as HistoryIcon } from 'lucide-react';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import TransactionStatusBadge from '@/components/transfer/TransactionStatusBadge';
import { useAuth } from '@/store/auth/authContext';
import { useBeneficiary } from '@/hooks/useBeneficiary';
import { useTransferHistory } from '@/hooks/useTransfer';
import { formatCurrency } from '@/utils/formatCurrency';
import { formatDateShort } from '@/utils/formatDate';
import { CHANNEL_LABELS, CHANNEL_ICONS } from '@/utils/constants';


/**
 * Tableau de bord principal de l'utilisateur.
 *
 * Affiche :
 * - Message de bienvenue
 * - Raccourcis d'action (nouveau transfert, ajouter bénéficiaire)
 * - Aperçu des bénéficiaires (3 premiers)
 * - 5 dernières transactions
 */
function Dashboard() {
    const { user } = useAuth();
    const { beneficiaries, isLoading: beneficiariesLoading } = useBeneficiary();
    const { transactions, isLoading: transactionsLoading } = useTransferHistory(5);

    return (
        <div className="flex flex-col gap-6">
            {/* Bannière de bienvenue */}
            <div className="rounded-2xl bg-gradient-to-br from-brand-600 to-brand-400 p-6 text-white shadow-md">
                <h2 className="text-xl font-bold">
                    Bonjour, {user?.firstName} 👋
                </h2>
                <p className="mt-1 text-sm text-brand-50">
                    Envoyez de l'argent instantanément vers 6 canaux différents.
                </p>
                <div className="mt-4 flex flex-wrap gap-2">
                    <Link to="/transfer">
                        <Button variant="secondary" size="sm" icon={<Send size={16} />} className="bg-white text-brand-700 hover:bg-brand-50">
                            Nouveau transfert
                        </Button>
                    </Link>
                    <Link to="/beneficiaries">
                        <Button
                            size="sm"
                            icon={<Users size={16} />}
                            className="bg-brand-700/40 text-white hover:bg-brand-700/60"
                        >
                            Gérer mes bénéficiaires
                        </Button>
                    </Link>
                </div>
            </div>

            <div className="grid gap-6 lg:grid-cols-3">
                {/* Bénéficiaires */}
                <Card
                    title="Mes bénéficiaires"
                    actions={
                        <Link to="/beneficiaries" className="text-sm font-medium text-brand-600 hover:underline">
                            Tout voir
                        </Link>
                    }
                    className="lg:col-span-1"
                >
                    {beneficiariesLoading ? (
                        <Spinner size="sm" />
                    ) : beneficiaries.length === 0 ? (
                        <EmptyState
                            icon={<Users size={20} />}
                            text="Aucun bénéficiaire enregistré"
                            actionLabel="Ajouter un bénéficiaire"
                            actionTo="/beneficiaries"
                        />
                    ) : (
                        <div className="flex flex-col gap-3">
                            {beneficiaries.slice(0, 3).map((b) => (
                                <div key={b.id} className="flex items-center gap-3">
                                    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-50 text-sm font-semibold text-brand-700">
                                        {b.firstName[0]}{b.lastName[0]}
                                    </div>
                                    <div className="min-w-0">
                                        <p className="truncate text-sm font-medium text-gray-900">
                                            {b.firstName} {b.lastName}
                                        </p>
                                        <p className="text-xs text-gray-500">
                                            {CHANNEL_ICONS[b.channel]} {CHANNEL_LABELS[b.channel]}
                                        </p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </Card>

                {/* Dernières transactions */}
                <Card
                    title="Transactions récentes"
                    actions={
                        <Link to="/history" className="text-sm font-medium text-brand-600 hover:underline">
                            Historique complet
                        </Link>
                    }
                    className="lg:col-span-2"
                >
                    {transactionsLoading ? (
                        <Spinner size="sm" />
                    ) : transactions.length === 0 ? (
                        <EmptyState
                            icon={<Wallet size={20} />}
                            text="Aucune transaction pour le moment"
                            actionLabel="Effectuer un transfert"
                            actionTo="/transfer"
                        />
                    ) : (
                        <div className="flex flex-col divide-y divide-gray-100">
                            {transactions.map((tx) => (
                                <div key={tx.id} className="flex items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                                    <div className="flex items-center gap-3 min-w-0">
                                        <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-gray-50 text-lg">
                                            {CHANNEL_ICONS[tx.channel]}
                                        </div>
                                        <div className="min-w-0">
                                            <p className="truncate text-sm font-medium text-gray-900">
                                                {tx.reference}
                                            </p>
                                            <p className="text-xs text-gray-500">{formatDateShort(tx.createdAt)}</p>
                                        </div>
                                    </div>
                                    <div className="flex flex-shrink-0 items-center gap-3">
                    <span className="text-sm font-semibold text-gray-900">
                      {formatCurrency(tx.amountSent, tx.currencySent)}
                    </span>
                                        <TransactionStatusBadge status={tx.status} />
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </Card>
            </div>
        </div>
    );
}

/**
 * État vide réutilisable pour les cartes du dashboard.
 */
function EmptyState({
                        icon,
                        text,
                        actionLabel,
                        actionTo,
                    }: {
    icon: React.ReactNode;
    text: string;
    actionLabel: string;
    actionTo: string;
}) {
    return (
        <div className="flex flex-col items-center gap-2 py-6 text-center">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gray-100 text-gray-400">
                {icon}
            </div>
            <p className="text-sm text-gray-500">{text}</p>
            <Link to={actionTo} className="flex items-center gap-1 text-sm font-medium text-brand-600 hover:underline">
                {actionLabel} <ArrowRight size={14} />
            </Link>
        </div>
    );
}

export default Dashboard;