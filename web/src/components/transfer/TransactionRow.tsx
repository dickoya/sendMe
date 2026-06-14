import { Download, Eye, Loader2 } from 'lucide-react';
import TransactionStatusBadge from './TransactionStatusBadge';
import { CHANNEL_LABELS, CHANNEL_ICONS } from '@/utils/constants';
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
 * Ligne de transaction dans l'historique — affiche toutes les infos
 * clés et les actions de téléchargement/prévisualisation du reçu.
 *
 * Le reçu PDF n'a de sens que pour les transactions COMPLETED
 * (le backend génère le PDF pour tout statut, mais visuellement
 * on ne propose l'action que si le transfert a abouti).
 */
function TransactionRow({ transaction, onDownload, onPreview, isDownloading, isPreviewing }: TransactionRowProps) {
    const canDownloadReceipt = transaction.status === 'COMPLETED';

    return (
        <div className="flex flex-col gap-3 rounded-xl border border-gray-100 p-4 sm:flex-row sm:items-center sm:justify-between">
            {/* Infos principales */}
            <div className="flex items-center gap-3 min-w-0">
                <div className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full bg-gray-50 text-xl">
                    {CHANNEL_ICONS[transaction.channel]}
                </div>
                <div className="min-w-0">
                    <p className="truncate font-medium text-gray-900">{transaction.reference}</p>
                    <p className="text-sm text-gray-500">
                        {CHANNEL_LABELS[transaction.channel]} • {formatDate(transaction.createdAt)}
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
                            aria-label="Prévisualiser le reçu"
                            title="Prévisualiser"
                        >
                            {isPreviewing ? <Loader2 size={16} className="animate-spin" /> : <Eye size={16} />}
                        </button>
                        <button
                            onClick={(e) => { e.stopPropagation(); onDownload(); }}
                            disabled={isDownloading}
                            className="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-brand-600 disabled:opacity-50"
                            aria-label="Télécharger le reçu"
                            title="Télécharger PDF"
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