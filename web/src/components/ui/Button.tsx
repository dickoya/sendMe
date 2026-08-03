import { type ButtonHTMLAttributes, forwardRef } from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

/**
 * Variantes de style du bouton, gérées par class-variance-authority.
 * Chaque combinaison variant/size génère les classes Tailwind appropriées.
 */
const buttonVariants = cva(
    'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-xl font-medium transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 active:scale-[0.97]',
    {
        variants: {
            variant: {
                primary:
                    'bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-lg shadow-brand-500/20 hover:shadow-xl hover:shadow-brand-500/30 hover:from-brand-600 hover:to-brand-700 focus-visible:ring-brand-400 dark:from-brand-400 dark:to-brand-500 dark:shadow-brand-400/15 dark:hover:shadow-brand-400/25',
                secondary:
                    'bg-gray-100 text-gray-900 hover:bg-gray-200 focus-visible:ring-gray-300 dark:bg-[#1A1A26] dark:text-[#EDEEF0] dark:hover:bg-[#222233] dark:border dark:border-[#2A2A40]',
                outline:
                    'border-2 border-gray-200 bg-white text-gray-700 hover:border-brand-400 hover:text-brand-600 hover:bg-brand-50/50 focus-visible:ring-brand-300 dark:border-[#2A2A40] dark:bg-transparent dark:text-[#A8ACBD] dark:hover:border-brand-400 dark:hover:text-brand-400 dark:hover:bg-brand-500/5',
                danger:
                    'bg-gradient-to-r from-danger-500 to-danger-600 text-white shadow-lg shadow-danger-500/20 hover:shadow-xl hover:shadow-danger-500/30 focus-visible:ring-danger-400 dark:from-danger-500 dark:to-danger-600 dark:shadow-danger-500/15',
                success:
                    'bg-gradient-to-r from-accent-500 to-accent-600 text-white shadow-lg shadow-accent-500/20 hover:shadow-xl hover:shadow-accent-500/30 focus-visible:ring-accent-400 dark:from-accent-400 dark:to-accent-500 dark:shadow-accent-400/15',
                ghost:
                    'text-brand-600 hover:bg-brand-50/80 focus-visible:ring-brand-300 dark:text-brand-400 dark:hover:bg-[#1A1A26]',
            },
            size: {
                sm: 'h-9 px-4 text-xs',
                md: 'h-11 px-5 text-sm',
                lg: 'h-13 px-8 text-base',
                icon: 'h-11 w-11',
            },
            fullWidth: {
                true: 'w-full',
            },
        },
        defaultVariants: {
            variant: 'primary',
            size: 'md',
        },
    }
);


export interface ButtonProps
    extends ButtonHTMLAttributes<HTMLButtonElement>,
        VariantProps<typeof buttonVariants> {
    /** Affiche un spinner et désactive le bouton */
    icon?: React.ReactNode;
    loading?: boolean;
}

/**
 * Bouton réutilisable avec variantes Tailwind.
 *
 * @example
 * <Button variant="primary" size="lg" loading={isSubmitting}>
 *   Envoyer
 * </Button>
 */
const Button = forwardRef<HTMLButtonElement, ButtonProps>(
    ({ className, variant, size, fullWidth, loading, disabled, icon, children, ...props }, ref) => {
        return (
            <button
                ref={ref}
                className={cn(buttonVariants({ variant, size, fullWidth }), className)}
                disabled={disabled || loading}
                {...props}
            >
                {loading && <Loader2 className="h-4 w-4 animate-spin" />}
                {children}
            </button>
        );
    }
);

Button.displayName = 'Button';

export default Button;
