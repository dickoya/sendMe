import { useTranslation } from 'react-i18next';
import { Download, Eye, Loader2 } from 'lucide-react';
import TransactionStatusBadge from './TransactionStatusBadge';
import { getChannelLabel, getChannelIcon } from '@/utils/constants';
import { formatCurrency } from '@/utils/formatCurrency';
import { formatDate } from '@/utils/formatDate';
import type { Transaction } from '@/types';

export interface TransactionRowProps {
    transaction: Transaction;
    onDownload: () => void;
    onPreview: () => void;
    isDownloading: boolean;
    isPreviewing: boolean;
}

/**
 * Ligne de transaction dans l'historique.
 *
 * Libellé/icône de canal via les helpers de canal, aria-labels via
 * "common", formatDate sensible à la langue.
 */
function TransactionRow({ transaction, onDownload, onPreview, isDownloading, isPreviewing }: TransactionRowProps) {
    const { t: tCommon } = useTranslation('common');
    const { t } = useTranslation('history');
    const canDownloadReceipt = transaction.status === 'COMPLETED';

    return (
        <div className="flex flex-col gap-3 rounded-xl border border-gray-100 p-4 sm:flex-row sm:items-center sm:justify-between">
            {/* Infos principales */}
            <div className="flex items-center gap-3 min-w-0">
                <div className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full bg-gray-50 text-xl">
                    {getChannelIcon(transaction.channel)}
                </div>
                <div className="min-w-0">
                    <p className="truncate font-medium text-gray-900">{transaction.reference}</p>
                    <p className="text-sm text-gray-500">
                        {getChannelLabel(transaction.channel, tCommon)} • {formatDate(transaction.createdAt)}
                    </p>
                </div>
            </div>

            {/* Montants + statut */}
            <div className="flex items-center justify-between gap-4 sm:justify-end">
                <div className="text-right">
                    <p className="font-semibold text-gray-900">
                        {formatCurrency(transaction.amountSent, transaction.currencySent)}
                    </p>
                    {transaction.status === 'COMPLETED' && (
                        <p className="text-xs text-accent-600">
                            → {formatCurrency(transaction.amountReceived, transaction.currencyReceived)}
                        </p>
                    )}
                </div>

                <TransactionStatusBadge status={transaction.status} />

                {/* Actions reçu PDF */}
                {canDownloadReceipt && (
                    <div className="flex gap-1">
                        <button
                            onClick={(e) => { e.stopPropagation(); onPreview(); }}
                            disabled={isPreviewing}
                            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-brand-600 disabled:opacity-50"
                            aria-label={tCommon('actions.preview')}
                            title={tCommon('actions.preview')}
                        >
                            {isPreviewing ? <Loader2 size={16} className="animate-spin" /> : <Eye size={16} />}
                        </button>
                        <button
                            onClick={(e) => { e.stopPropagation(); onDownload(); }}
                            disabled={isDownloading}
                            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-brand-600 disabled:opacity-50"
                            aria-label={t('actions.downloadReceipt')}
                            title={t('actions.downloadReceipt')}
                        >
                            {isDownloading ? <Loader2 size={16} className="animate-spin" /> : <Download size={16} />}
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}

export default TransactionRow;