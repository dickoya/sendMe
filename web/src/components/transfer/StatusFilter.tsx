import { cn } from '@/lib/utils';
import { STATUS_LABELS } from '@/utils/constants';
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
 */
function StatusFilter({ value, onChange }: StatusFilterProps) {
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
                    {status === 'ALL' ? 'Toutes' : STATUS_LABELS[status]}
                </button>
            ))}
        </div>
    );
}

export default StatusFilter;