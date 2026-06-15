import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import Pagination from '@/components/ui/Pagination';
import StatusFilter from '@/components/transfer/StatusFilter';
import TransactionStatusBadge from '@/components/transfer/TransactionStatusBadge';
import Modal from '@/components/ui/Modal';
import { useAdminTransactions } from '@/hooks/useAdminTransactions';
import { getChannelLabel, getChannelIcon } from '@/utils/constants';
import { formatCurrency } from '@/utils/formatCurrency';
import { formatDate } from '@/utils/formatDate';
import { ArrowRightLeft } from 'lucide-react';
import type { Transaction } from '@/types';

/**
 * Page admin — toutes les transactions de la plateforme.
 *
 * Migration i18n (Phase 11) : namespace "admin" (clés transactions.*),
 * "transfer" pour les libellés financiers réutilisés dans la modale
 * de détail (cohérence avec History — Phase 9).
 */
function AdminTransactions() {
    const { t } = useTranslation('admin');
    const { t: tCommon } = useTranslation('common');
    const { t: tTransfer } = useTranslation('transfer');

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
            title={t('transactions.pageTitle')}
            actions={<span className="text-sm text-gray-400">{t('transactions.totalCount', { count: totalElements })}</span>}
        >
            <div className="flex flex-col gap-4">
                <StatusFilter value={statusFilter} onChange={setStatusFilter} />

                {isLoading ? (
                    <Spinner label={tCommon('states.loading')} />
                ) : transactions.length === 0 ? (
                    <EmptyState title={t('transactions.empty.title')} message={t('transactions.empty.message')} />
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead>
                            <tr className="border-b border-gray-100 text-xs uppercase text-gray-400">
                                <th className="py-3 font-medium">{t('transactions.table.reference')}</th>
                                <th className="py-3 font-medium">{t('transactions.table.channel')}</th>
                                <th className="py-3 font-medium text-right">{t('transactions.table.amountSent')}</th>
                                <th className="py-3 font-medium text-right">{t('transactions.table.amountReceived')}</th>
                                <th className="py-3 font-medium">{t('transactions.table.status')}</th>
                                <th className="py-3 font-medium">{t('transactions.table.date')}</th>
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
                                        {getChannelIcon(tx.channel)} {getChannelLabel(tx.channel, tCommon)}
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
            <Modal isOpen={!!selected} onClose={() => setSelected(null)} title={t('transactions.pageTitle')} size="md">
                {selected && (
                    <div className="flex flex-col gap-3 text-sm">
                        <Row label={t('transactions.table.reference')} value={selected.reference} />
                        <Row
                            label={t('transactions.table.channel')}
                            value={`${getChannelIcon(selected.channel)} ${getChannelLabel(selected.channel, tCommon)}`}
                        />
                        <Row label={t('transactions.table.fees')} value={formatCurrency(selected.fees, selected.currencySent)} />
                        <Row label={t('transactions.table.amountSent')} value={formatCurrency(selected.fees, selected.currencySent)} />
                        <Row
                            label={tTransfer('simulator.exchangeRate')}
                            value={tTransfer('simulator.exchangeRateValue', {
                                from: selected.currencySent,
                                rate: selected.exchangeRate,
                                to: selected.currencyReceived,
                            })}
                        />
                        {selected.status === 'COMPLETED' && (
                            <Row
                                label={t('transactions.table.amountReceived')}
                                value={formatCurrency(selected.amountReceived, selected.currencyReceived)}
                                highlight
                            />
                        )}
                        {selected.stripePaymentIntentId && (
                            <Row label="Stripe" value={selected.stripePaymentIntentId} mono />
                        )}
                        <Row label={t('transactions.table.date')} value={formatDate(selected.createdAt)} />
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

function EmptyState({ title, message }: { title: string; message: string }) {
    return (
        <div className="flex flex-col items-center gap-2 py-12 text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400">
                <ArrowRightLeft size={22} />
            </div>
            <p className="font-medium text-gray-900">{title}</p>
            <p className="text-sm text-gray-500">{message}</p>
        </div>
    );
}

export default AdminTransactions;