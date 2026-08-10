import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { History as HistoryIcon } from 'lucide-react';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import EmptyState from '@/components/ui/EmptyState';
import Pagination from '@/components/ui/Pagination';
import StatusFilter from '@/components/transfer/StatusFilter';
import TransactionRow from '@/components/transfer/TransactionRow';
import TransactionDetailModal from '@/components/transfer/TransactionDetailModal';
import { useTransferHistory } from '@/hooks/useTransfer';
import { useToast } from '@/store/notification/toastContext';
import { transferService } from '@/services/transferService';
import { getErrorMessage } from '@/lib/utils';
import type { Transaction } from '@/types';

/**
 * Page d'historique des transactions.
 *
 * Namespace "history" pour le contenu propre à la page,
 * "common" pour les états de chargement.
 */
function History() {
    const { t } = useTranslation('history');
    const { t: tCommon } = useTranslation('common');

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
            <Card title={t('pageTitle')}>
                <div className="flex flex-col gap-4">
                    <StatusFilter value={statusFilter} onChange={setStatusFilter} />

                    {isLoading ? (
                        <Spinner label={tCommon('states.loading')} />
                    ) : transactions.length === 0 ? (
                        <EmptyState
                            icon={<HistoryIcon size={22} />}
                            title={t('empty.title')}
                            message={statusFilter === 'ALL' ? t('empty.message') : t('empty.filteredMessage')}
                        />
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

export default History;
