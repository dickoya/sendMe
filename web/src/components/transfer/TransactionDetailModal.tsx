import { Download, Eye, Loader2 } from 'lucide-react';
import Modal from '@/components/ui/Modal';
import Button from '@/components/ui/Button';
import TransactionStatusBadge from './TransactionStatusBadge';
import { CHANNEL_LABELS, CHANNEL_ICONS } from '@/utils/constants';
import { formatCurrency } from '@/utils/formatCurrency';
import { formatDate } from '@/utils/formatDate';
import type { Transaction } from '@/types';

export interface TransactionDetailModalProps {
    transaction: Transaction | null;
    onClose: () => void;
    onDownload: (transaction: Transaction) => void;
    onPreview: (transaction: Transaction) => void;
    isDownloading: boolean;
    isPreviewing: boolean;
}

/**
 * Modale de détail complet d'une transaction —
 * tous les champs renvoyés par GET /api/transactions/{id}.
 */
function TransactionDetailModal({
                                    transaction,
                                    onClose,
                                    onDownload,
                                    onPreview,
                                    isDownloading,
                                    isPreviewing,
                                }: TransactionDetailModalProps) {
    if (!transaction) return null;

    const canDownloadReceipt = transaction.status === 'COMPLETED';

    return (
        <Modal isOpen={!!transaction} onClose={onClose} title="Détail de la transaction" size="md">
            <div className="flex flex-col gap-4">
                {/* En-tête */}
                <div className="flex items-center justify-between">
                    <div>
                        <p className="text-lg font-bold text-gray-900">{transaction.reference}</p>
                        <p className="text-sm text-gray-500">{formatDate(transaction.createdAt)}</p>
                    </div>
                    <TransactionStatusBadge status={transaction.status} />
                </div>

                {/* Montants */}
                <div className="flex flex-col gap-2 rounded-xl bg-gray-50 p-4 text-sm">
                    <DetailRow label="Montant envoyé" value={formatCurrency(transaction.amountSent, transaction.currencySent)} />
                    <DetailRow label="Frais plateforme" value={formatCurrency(transaction.fees, transaction.currencySent)} muted />
                    <DetailRow
                        label="Taux de change"
                        value={`1 ${transaction.currencySent} = ${transaction.exchangeRate} ${transaction.currencyReceived}`}
                        muted
                    />
                    <div className="h-px bg-gray-200" />
                    <DetailRow
                        label="Montant reçu"
                        value={formatCurrency(transaction.amountReceived, transaction.currencyReceived)}
                        highlight
                    />
                </div>

                {/* Canal et référence Stripe */}
                <div className="flex flex-col gap-2 text-sm">
                    <DetailRow label="Canal de réception" value={`${CHANNEL_ICONS[transaction.channel]} ${CHANNEL_LABELS[transaction.channel]}`} />
                    {transaction.stripePaymentIntentId && (
                        <DetailRow label="Référence Stripe" value={transaction.stripePaymentIntentId} mono />
                    )}
                    {transaction.updatedAt && transaction.updatedAt !== transaction.createdAt && (
                        <DetailRow label="Dernière mise à jour" value={formatDate(transaction.updatedAt)} />
                    )}
                </div>

                {/* Actions reçu PDF */}
                {canDownloadReceipt && (
                    <div className="flex gap-2 border-t border-gray-100 pt-4">
                        <Button
                            variant="outline"
                            fullWidth
                            icon={isPreviewing ? <Loader2 size={16} className="animate-spin" /> : <Eye size={16} />}
                            onClick={() => onPreview(transaction)}
                            disabled={isPreviewing}
                        >
                            Prévisualiser
                        </Button>
                        <Button
                            fullWidth
                            icon={isDownloading ? <Loader2 size={16} className="animate-spin" /> : <Download size={16} />}
                            onClick={() => onDownload(transaction)}
                            disabled={isDownloading}
                        >
                            Télécharger le reçu
                        </Button>
                    </div>
                )}
            </div>
        </Modal>
    );
}

function DetailRow({ label, value, muted, highlight, mono }: { label: string; value: string; muted?: boolean; highlight?: boolean; mono?: boolean }) {
    return (
        <div className="flex items-center justify-between">
            <span className={muted ? 'text-gray-500' : 'text-gray-700'}>{label}</span>
            <span
                className={
                    highlight
                        ? 'font-bold text-accent-600'
                        : muted
                            ? 'text-gray-500'
                            : mono
                                ? 'font-mono text-xs text-gray-700'
                                : 'font-medium text-gray-900'
                }
            >
        {value}
      </span>
        </div>
    );
}

export default TransactionDetailModal;