import { type InputHTMLAttributes, forwardRef, useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    error?: string;
    hint?: string;
    /** Icône affichée à gauche du champ (composant lucide-react) */
    icon?: React.ReactNode;
}

/**
 * Champ de saisie avec label, gestion d'erreur et toggle mot de passe.
 *
 * @example
 * <Input label="Email" type="email" icon={<Mail size={16} />} error={errors.email} />
 */
const Input = forwardRef<HTMLInputElement, InputProps>(
    ({ className, label, error, hint, icon, type = 'text', required, id, ...props }, ref) => {
        const [showPassword, setShowPassword] = useState(false);
        const isPassword = type === 'password';
        const inputType = isPassword && showPassword ? 'text' : type;
        const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');

        return (
            <div className="flex w-full flex-col gap-1.5">
                {label && (
                    <label htmlFor={inputId} className="text-sm font-medium text-gray-700 dark:text-gray-300">
                        {label}
                        {required && <span className="ml-0.5 text-danger-500">*</span>}
                    </label>
                )}

                <div
                    className={cn(
                        'flex items-center rounded-lg border border-gray-300 bg-white transition-all duration-200 focus-within:border-brand-500 focus-within:ring-2 focus-within:ring-brand-100 dark:border-[#333333] dark:bg-[#141414] dark:focus-within:border-brand-400 dark:focus-within:ring-brand-500/20',
                        error && 'border-danger-500 focus-within:border-danger-500 focus-within:ring-danger-100 dark:border-danger-500 dark:focus-within:ring-danger-500/20'
                    )}
                >
                    {icon && <span className="pl-3 text-gray-400 dark:text-[#5A5E72]">{icon}</span>}

                    <input
                        ref={ref}
                        id={inputId}
                        type={inputType}
                        className={cn(
                            'w-full bg-transparent px-3 py-2.5 text-sm text-gray-900 outline-none placeholder:text-gray-400 dark:text-[#EDEEF0] dark:placeholder:text-[#5A5E72]',
                            icon && 'pl-2',
                            className
                        )}
                        {...props}
                    />

                    {isPassword && (
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="px-3 text-gray-400 hover:text-gray-600 dark:text-[#5A5E72] dark:hover:text-[#8B8FA3]"
                            aria-label={showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                        >
                            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                        </button>
                    )}
                </div>


                {error && <span className="text-xs text-danger-500">{error}</span>}
                {hint && !error && <span className="text-xs text-gray-400 dark:text-gray-500">{hint}</span>}
            </div>
        );
    }
);

Input.displayName = 'Input';

export default Input;
