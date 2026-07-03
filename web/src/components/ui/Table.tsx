import { type ReactNode } from 'react';
import { ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-react';
import { cn } from '@/lib/utils';
import Spinner from './Spinner';
import EmptyState from './EmptyState';

export interface TableColumn<T> {
    key: string;
    label: string;
    sortable?: boolean;
    align?: 'left' | 'right' | 'center';
    render: (row: T) => ReactNode;
    className?: string;
}

export interface TableProps<T> {
    columns: TableColumn<T>[];
    data: T[];
    onRowClick?: (row: T) => void;
    isLoading?: boolean;
    emptyState?: {
        icon?: ReactNode;
        title: string;
        message?: string;
    };
    sortKey?: string;
    sortDirection?: 'asc' | 'desc';
    onSort?: (key: string) => void;
    className?: string;
    rowKey?: (row: T) => string | number;
}

/**
 * Tableau de données générique avec tri, chargement et état vide.
 *
 * @example
 * <Table
 *   columns={[
 *     { key: 'name', label: 'Nom', render: (u) => u.name },
 *     { key: 'role', label: 'Rôle', render: (u) => <Badge>{u.role}</Badge> },
 *   ]}
 *   data={users}
 *   onRowClick={(u) => setSelected(u)}
 *   isLoading={loading}
 * />
 */
function Table<T extends Record<string, any>>({
    columns,
    data,
    onRowClick,
    isLoading,
    emptyState,
    sortKey,
    sortDirection,
    onSort,
    className,
    rowKey,
}: TableProps<T>) {
    if (isLoading) {
        return (
            <div className="flex justify-center py-8">
                <Spinner size="md" />
            </div>
        );
    }

    if (data.length === 0 && emptyState) {
        return (
            <EmptyState
                icon={emptyState.icon}
                title={emptyState.title}
                message={emptyState.message}
            />
        );
    }

    const getSortIcon = (key: string) => {
        if (sortKey !== key) return <ChevronsUpDown size={14} className="text-gray-300" />;
        return sortDirection === 'asc' ? (
            <ChevronUp size={14} className="text-brand-600" />
        ) : (
            <ChevronDown size={14} className="text-brand-600" />
        );
    };

    return (
        <div className={cn('overflow-x-auto', className)}>
            <table className="w-full text-left text-sm">
                <thead>
                    <tr className="border-b border-gray-100 text-xs uppercase text-gray-400 dark:border-[#222222] dark:text-[#5A5E72]">
                        {columns.map((col) => (
                            <th
                                key={col.key}
                                className={cn(
                                    'py-3 font-medium',
                                    col.align === 'right' && 'text-right',
                                    col.align === 'center' && 'text-center',
                                    col.sortable && 'cursor-pointer select-none hover:text-gray-600 dark:hover:text-[#8B8FA3]',
                                    col.className
                                )}
                                onClick={() => col.sortable && onSort?.(col.key)}
                            >
                                <span className="inline-flex items-center gap-1">
                                    {col.label}
                                    {col.sortable && getSortIcon(col.key)}
                                </span>
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-50 dark:divide-[#1A1A1A]">
                    {data.map((row, index) => (
                        <tr
                            key={rowKey ? rowKey(row) : index}
                            onClick={() => onRowClick?.(row)}
                            className={cn(
                                onRowClick && 'cursor-pointer hover:bg-gray-50 dark:hover:bg-[#1A1A1A]'
                            )}
                        >
                            {columns.map((col) => (
                                <td
                                    key={col.key}
                                    className={cn(
                                        'py-3',
                                        col.align === 'right' && 'text-right',
                                        col.align === 'center' && 'text-center',
                                        col.className
                                    )}
                                >
                                    {col.render(row)}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

export default Table;
