import Badge from '@/components/ui/Badge';
import { STATUS_LABELS, STATUS_VARIANT } from '@/utils/constants';
import type { TransactionStatus } from '@/types';

export interface TransactionStatusBadgeProps {
    status: TransactionStatus;
}

/**
 * Badge coloré affichant le statut d'une transaction
 * avec le libellé français correspondant.
 */
function TransactionStatusBadge({ status }: TransactionStatusBadgeProps) {
    return <Badge variant={STATUS_VARIANT[status]}>{STATUS_LABELS[status]}</Badge>;
}

export default TransactionStatusBadge;