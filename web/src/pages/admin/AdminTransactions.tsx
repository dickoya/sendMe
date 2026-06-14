import { useState } from 'react';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import Pagination from '@/components/ui/Pagination';
import StatusFilter from '@/components/transfer/StatusFilter';
import TransactionStatusBadge from '@/components/transfer/TransactionStatusBadge';
import Modal from '@/components/ui/Modal';
import { useAdminTransactions } from '@/hooks/useAdminTransactions';
import { CHANNEL_LABELS, CHANNEL_ICONS } from '@/utils/constants';
import { formatCurrency } from '@/utils/formatCurrency';
import { formatDate } from '@/utils/formatDate';
import { ArrowRightLeft } from 'lucide-react';
import type { Transaction } from '@/types';

/**
 * Page admin — toutes les transactions de la plateforme.
 *
 * Différences avec History.tsx (utilisateur) :
 * - Filtre par statut envoyé au backend (paramètre `status`)
 * - Affiche l'expéditeur (non disponible côté utilisateur, inutile pour lui)
 * - Pas de téléchargement de reçu (action réservée au propriétaire de la transaction)
 */
function AdminTransactions() {
    const {
        transactions,
        totalPages,
        totalElements,
        page,
        setPage,
        statusFilter,
        setStatusFilter,
        isLoading,
    } = useAdminTransactions(20);

    const [selected, setSelected] = useState<Transaction | null>(null);

    return (
        <Card
            title="Toutes les transactions"
            actions={<span className="text-sm text-gray-400">{totalElements} au total</span>}
        >
            <div className="flex flex-col gap-4">
                <StatusFilter value={statusFilter} onChange={setStatusFilter} />

                {isLoading ? (
                    <Spinner label="Chargement des transactions..." />
                ) : transactions.length === 0 ? (
                    <EmptyState />
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead>
                            <tr className="border-b border-gray-100 text-xs uppercase text-gray-400">
                                <th className="py-3 font-medium">Référence</th>
                                <th className="py-3 font-medium">Canal</th>
                                <th className="py-3 font-medium text-right">Montant envoyé</th>
                                <th className="py-3 font-medium text-right">Montant reçu</th>
                                <th className="py-3 font-medium">Statut</th>
                                <th className="py-3 font-medium">Date</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50">
                            {transactions.map((tx) => (
                                <tr
                                    key={tx.id}
                                    onClick={() => setSelected(tx)}
                                    className="cursor-pointer hover:bg-gray-50"
                                >
                                    <td className="py-3 font-medium text-gray-900">{tx.reference}</td>
                                    <td className="py-3 text-gray-700">
                                        {CHANNEL_ICONS[tx.channel]} {CHANNEL_LABELS[tx.channel]}
                                    </td>
                                    <td className="py-3 text-right text-gray-900">
                                        {formatCurrency(tx.amountSent, tx.currencySent)}
                                    </td>
                                    <td className="py-3 text-right text-gray-700">
                                        {tx.status === 'COMPLETED'
                                            ? formatCurrency(tx.amountReceived, tx.currencyReceived)
                                            : '—'}
                                    </td>
                                    <td className="py-3">
                                        <TransactionStatusBadge status={tx.status} />
                                    </td>
                                    <td className="py-3 text-gray-500">{formatDate(tx.createdAt)}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}

                <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
            </div>

            {/* Modale de détail simplifiée (lecture seule, pas de reçu) */}
            <Modal isOpen={!!selected} onClose={() => setSelected(null)} title="Détail de la transaction" size="md">
                {selected && (
                    <div className="flex flex-col gap-3 text-sm">
                        <Row label="Référence" value={selected.reference} />
                        <Row label="Canal" value={`${CHANNEL_ICONS[selected.channel]} ${CHANNEL_LABELS[selected.channel]}`} />
                        <Row label="Montant envoyé" value={formatCurrency(selected.amountSent, selected.currencySent)} />
                        <Row label="Frais" value={formatCurrency(selected.fees, selected.currencySent)} />
                        <Row label="Taux de change" value={`1 ${selected.currencySent} = ${selected.exchangeRate} ${selected.currencyReceived}`} />
                        {selected.status === 'COMPLETED' && (
                            <Row label="Montant reçu" value={formatCurrency(selected.amountReceived, selected.currencyReceived)} highlight />
                        )}
                        {selected.stripePaymentIntentId && (
                            <Row label="Référence Stripe" value={selected.stripePaymentIntentId} mono />
                        )}
                        <Row label="Date" value={formatDate(selected.createdAt)} />
                        <div className="pt-2">
                            <TransactionStatusBadge status={selected.status} />
                        </div>
                    </div>
                )}
            </Modal>
        </Card>
    );
}

function Row({ label, value, highlight, mono }: { label: string; value: string; highlight?: boolean; mono?: boolean }) {
    return (
        <div className="flex items-center justify-between">
            <span className="text-gray-500">{label}</span>
            <span className={highlight ? 'font-bold text-accent-600' : mono ? 'font-mono text-xs text-gray-700' : 'font-medium text-gray-900'}>
        {value}
      </span>
        </div>
    );
}

function EmptyState() {
    return (
        <div className="flex flex-col items-center gap-2 py-12 text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400">
                <ArrowRightLeft size={22} />
            </div>
            <p className="font-medium text-gray-900">Aucune transaction</p>
            <p className="text-sm text-gray-500">Aucune transaction ne correspond à ce filtre.</p>
        </div>
    );
}

export default AdminTransactions;