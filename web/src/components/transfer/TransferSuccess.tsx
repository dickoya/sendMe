import { Link } from 'react-router-dom';
import { CheckCircle2, Download, Plus } from 'lucide-react';
import Button from '@/components/ui/Button';
import { formatCurrency } from '@/utils/formatCurrency';
import type { Transaction } from '@/types';

export interface TransferSuccessProps {
    transaction: Transaction;
    onNewTransfer: () => void;
}

/**
 * Écran de confirmation affiché après un transfert COMPLETED.
 * Le téléchargement du reçu PDF sera branché à l'Étape 8.
 */
function TransferSuccess({ transaction, onNewTransfer }: TransferSuccessProps) {
    return (
        <div className="flex flex-col items-center gap-4 py-4 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-accent-50 text-accent-500">
                <CheckCircle2 size={36} />
            </div>

            <div>
                <h3 className="text-lg font-bold text-gray-900">Transfert réussi !</h3>
                <p className="mt-1 text-sm text-gray-500">
                    Référence : <span className="font-medium text-gray-700">{transaction.reference}</span>
                </p>
            </div>

            <div className="w-full rounded-xl bg-accent-50 px-4 py-3">
                <p className="text-xs text-accent-600">Montant reçu par le bénéficiaire</p>
                <p className="text-xl font-bold text-accent-600">
                    {formatCurrency(transaction.amountReceived, transaction.currencyReceived)}
                </p>
            </div>

            <div className="flex w-full flex-col gap-2 sm:flex-row">
                <Link to={`/history`} className="flex-1">
                    <Button variant="outline" fullWidth icon={<Download size={16} />}>
                        Voir le reçu
                    </Button>
                </Link>
                <Button fullWidth icon={<Plus size={16} />} onClick={onNewTransfer}>
                    Nouveau transfert
                </Button>
            </div>
        </div>
    );
}

export default TransferSuccess;