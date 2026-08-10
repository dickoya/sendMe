import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils';
import { getStatusLabel } from '@/utils/constants';
import type { TransactionStatus } from '@/types';

export interface StatusFilterProps {
    value: TransactionStatus | 'ALL';
    onChange: (status: TransactionStatus | 'ALL') => void;
}

const STATUSES: (TransactionStatus | 'ALL')[] = [
    'ALL',
    'COMPLETED',
    'PENDING',
    'PROCESSING',
    'FAILED',
    'REFUNDED',
];

/**
 * Barre de filtres par statut sous forme de pills cliquables.
 *
 * "ALL" est traité comme une clé de statut à part entière dans
 * common.json (status.ALL = "Toutes"/"All"), donc getStatusLabel()
 * fonctionne uniformément pour toutes les valeurs, sans cas particulier.
 */
function StatusFilter({ value, onChange }: StatusFilterProps) {
    const { t } = useTranslation('common');

    return (
        <div className="flex flex-wrap gap-2">
            {STATUSES.map((status) => (
                <button
                    key={status}
                    onClick={() => onChange(status)}
                    className={cn(
                        'rounded-full px-3 py-1.5 text-xs font-medium transition-colors',
                        value === status
                            ? 'bg-brand-600 text-white'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                    )}
                >
                    {getStatusLabel(status, t)}
                </button>
            ))}
        </div>
    );
}

export default StatusFilter;