import { Search } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface SearchInputProps {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    className?: string;
}

/**
 * Champ de recherche avec icône de loupe intégrée.
 *
 * @example
 * <SearchInput value={search} onChange={setSearch} placeholder={t('searchPlaceholder')} />
 */
function SearchInput({ value, onChange, placeholder, className }: SearchInputProps) {
    return (
        <div className={cn('relative', className)}>
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4 text-gray-400 dark:text-[#8B8FA3]">
                <Search size={20} />
            </div>
            <input
                type="text"
                value={value}
                onChange={(e) => onChange(e.target.value)}
                placeholder={placeholder}
                className="w-full rounded-2xl border border-gray-100 bg-white py-3.5 pl-12 pr-4 text-gray-900 shadow-sm transition-all placeholder:text-gray-400 focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20 dark:border-[#222222] dark:bg-[#121212] dark:text-[#EDEEF0] dark:placeholder:text-[#555555] dark:focus:border-brand-500"
            />
        </div>
    );
}

export default SearchInput;
