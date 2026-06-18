import { ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface PaginationProps {
    page: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

/**
 * Pagination simple avec numéros de page et flèches.
 * `page` est 0-indexed (convention Spring Data Pageable).
 */
function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
    if (totalPages <= 1) return null;

    const pages = Array.from({ length: totalPages }, (_, i) => i);

    return (
        <div className="flex items-center justify-center gap-1 pt-4">
            <button
                onClick={() => onPageChange(page - 1)}
                disabled={page === 0}
                className="rounded-lg p-2 text-gray-500 hover:bg-gray-100 disabled:opacity-40 dark:text-[#8B8FA3] dark:hover:bg-[#222222]"
                aria-label="Page précédente"
            >
                <ChevronLeft size={16} />
            </button>

            {pages.map((p) => (
                <button
                    key={p}
                    onClick={() => onPageChange(p)}
                    className={cn(
                        'h-8 w-8 rounded-lg text-sm font-medium transition-colors',
                        p === page
                            ? 'bg-brand-600 text-white dark:bg-brand-500 dark:shadow-[0_0_12px_rgba(90,159,224,0.3)]'
                            : 'text-gray-600 hover:bg-gray-100 dark:text-[#8B8FA3] dark:hover:bg-[#222222]'
                    )}
                >
                    {p + 1}
                </button>
            ))}

            <button
                onClick={() => onPageChange(page + 1)}
                disabled={page >= totalPages - 1}
                className="rounded-lg p-2 text-gray-500 hover:bg-gray-100 disabled:opacity-40 dark:text-[#8B8FA3] dark:hover:bg-[#222222]"
                aria-label="Page suivante"
            >
                <ChevronRight size={16} />
            </button>

        </div>
    );
}

export default Pagination;
