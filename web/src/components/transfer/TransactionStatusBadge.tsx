import { useTranslation } from 'react-i18next';
import Badge from '@/components/ui/Badge';
import { getStatusLabel, STATUS_VARIANT } from '@/utils/constants';
import type { TransactionStatus } from '@/types';

export interface TransactionStatusBadgeProps {
    status: TransactionStatus;
}

/**
 * Badge coloré affichant le statut d'une transaction.
 *
 * Migration i18n (Phase 9) : libellé via getStatusLabel(status, t)
 * (namespace "common"). STATUS_VARIANT reste inchangé — purement
 * visuel, indépendant de la langue.
 */
function TransactionStatusBadge({ status }: TransactionStatusBadgeProps) {
    const { t } = useTranslation('common');
    return <Badge variant={STATUS_VARIANT[status]}>{getStatusLabel(status, t)}</Badge>;
}

export default TransactionStatusBadge;