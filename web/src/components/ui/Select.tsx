import { type SelectHTMLAttributes, forwardRef } from 'react';
import { ChevronDown } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface SelectOption {
    value: string;
    label: string;
}

export interface SelectProps extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'children'> {
    label?: string;
    options: SelectOption[];
    error?: string;
    hint?: string;
    placeholder?: string;
}

/**
 * Menu déroulant avec label, gestion d'erreur et icône.
 *
 * @example
 * <Select
 *   label="Pays"
 *   options={[
 *     { value: 'FR', label: 'France' },
 *     { value: 'SN', label: 'Sénégal' },
 *   ]}
 *   value={country}
 *   onChange={handleChange}
 *   error={errors.country}
 * />
 */
const Select = forwardRef<HTMLSelectElement, SelectProps>(
    ({ className, label, options, error, hint, placeholder, id, required, ...props }, ref) => {
        const selectId = id || label?.toLowerCase().replace(/\s+/g, '-');

        return (
            <div className="flex w-full flex-col gap-1.5">
                {label && (
                    <label htmlFor={selectId} className="text-sm font-medium text-gray-700 dark:text-gray-300">
                        {label}
                        {required && <span className="ml-0.5 text-danger-500">*</span>}
                    </label>
                )}

                <div className="relative">
                    <select
                        ref={ref}
                        id={selectId}
                        className={cn(
                            'w-full appearance-none rounded-lg border border-gray-300 bg-white px-3 py-2.5 pr-10 text-sm text-gray-900 outline-none transition-all focus:border-brand-500 focus:ring-2 focus:ring-brand-100 dark:border-[#333333] dark:bg-[#141414] dark:text-[#EDEEF0] dark:focus:border-brand-400 dark:focus:ring-brand-500/20',
                            error && 'border-danger-500 focus:border-danger-500 focus:ring-danger-100 dark:border-danger-500 dark:focus:ring-danger-500/20',
                            className
                        )}
                        {...props}
                    >
                        {placeholder && (
                            <option value="" disabled>
                                {placeholder}
                            </option>
                        )}
                        {options.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>
                    <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 dark:text-[#5A5E72]">
                        <ChevronDown size={16} />
                    </div>
                </div>

                {error && <span className="text-xs text-danger-500">{error}</span>}
                {hint && !error && <span className="text-xs text-gray-400 dark:text-gray-300">{hint}</span>}
            </div>
        );
    }
);

Select.displayName = 'Select';

export default Select;
