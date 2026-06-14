import { useState } from 'react';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import Pagination from '@/components/ui/Pagination';
import StatusFilter from '@/components/transfer/StatusFilter';
import TransactionRow from '@/components/transfer/TransactionRow';
import TransactionDetailModal from '@/components/transfer/TransactionDetailModal';
import { useTransferHistory } from '@/hooks/useTransfer';
import { useToast } from '@/store/notification/toastContext';
import { transferService } from '@/services/transferService';
import { getErrorMessage } from '@/lib/utils';
import { History as HistoryIcon } from 'lucide-react';
import type { Transaction } from '@/types';

/**
 * Page d'historique des transactions.
 *
 * - Filtrage par statut (pills)
 * - Pagination serveur (page/size)
 * - Clic sur une ligne → modale de détail
 * - Téléchargement et prévisualisation du reçu PDF (transactions COMPLETED)
 */
function History() {
    const {
        transactions,
        totalPages,
        page,
        setPage,
        statusFilter,
        setStatusFilter,
        isLoading,
    } = useTransferHistory(10);
    const { showToast } = useToast();

    const [selected, setSelected] = useState<Transaction | null>(null);
    const [downloadingId, setDownloadingId] = useState<number | null>(null);
    const [previewingId, setPreviewingId] = useState<number | null>(null);

    const handleDownload = async (transaction: Transaction) => {
        setDownloadingId(transaction.id);
        try {
            await transferService.downloadReceipt(transaction.id, transaction.reference);
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        } finally {
            setDownloadingId(null);
        }
    };

    const handlePreview = async (transaction: Transaction) => {
        setPreviewingId(transaction.id);
        try {
            const url = await transferService.previewReceipt(transaction.id);
            window.open(url, '_blank');
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        } finally {
            setPreviewingId(null);
        }
    };

    return (
        <div className="flex flex-col gap-4">
            <Card title="Historique des transactions">
                <div className="flex flex-col gap-4">
                    <StatusFilter value={statusFilter} onChange={setStatusFilter} />

                    {isLoading ? (
                        <Spinner label="Chargement de l'historique..." />
                    ) : transactions.length === 0 ? (
                        <EmptyHistory />
                    ) : (
                        <div className="flex flex-col gap-2">
                            {transactions.map((tx) => (
                                <div key={tx.id} onClick={() => setSelected(tx)} className="cursor-pointer">
                                    <TransactionRow
                                        transaction={tx}
                                        onDownload={() => handleDownload(tx)}
                                        onPreview={() => handlePreview(tx)}
                                        isDownloading={downloadingId === tx.id}
                                        isPreviewing={previewingId === tx.id}
                                    />
                                </div>
                            ))}
                        </div>
                    )}

                    <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
                </div>
            </Card>

            <TransactionDetailModal
                transaction={selected}
                onClose={() => setSelected(null)}
                onDownload={handleDownload}
                onPreview={handlePreview}
                isDownloading={selected ? downloadingId === selected.id : false}
                isPreviewing={selected ? previewingId === selected.id : false}
            />
        </div>
    );
}

function EmptyHistory() {
    return (
        <div className="flex flex-col items-center gap-2 py-12 text-center">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400">
                <HistoryIcon size={22} />
            </div>
            <p className="font-medium text-gray-900">Aucune transaction</p>
            <p className="text-sm text-gray-500">
                Vos transferts apparaîtront ici une fois effectués.
            </p>
        </div>
    );
}

export default History;