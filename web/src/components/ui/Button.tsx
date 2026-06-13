import { type ButtonHTMLAttributes, forwardRef } from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';

/**
 * Variantes de style du bouton, gérées par class-variance-authority.
 * Chaque combinaison variant/size génère les classes Tailwind appropriées.
 */
const buttonVariants = cva(
    'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg font-medium transition-all duration-150 disabled:opacity-60 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2',
    {
        variants: {
            variant: {
                primary:
                    'bg-brand-600 text-white shadow-sm hover:bg-brand-700 focus-visible:ring-brand-500 active:scale-[0.98]',
                secondary:
                    'bg-gray-100 text-gray-900 hover:bg-gray-200 focus-visible:ring-gray-300',
                outline:
                    'border border-gray-300 bg-white text-gray-700 hover:border-brand-500 hover:text-brand-600 focus-visible:ring-brand-300',
                danger:
                    'bg-danger-500 text-white shadow-sm hover:bg-danger-600 focus-visible:ring-danger-500 active:scale-[0.98]',
                success:
                    'bg-accent-500 text-white shadow-sm hover:bg-accent-600 focus-visible:ring-accent-400 active:scale-[0.98]',
                ghost:
                    'text-brand-600 hover:bg-brand-50 focus-visible:ring-brand-300',
            },
            size: {
                sm: 'h-8 px-3 text-xs',
                md: 'h-10 px-4 text-sm',
                lg: 'h-12 px-6 text-base',
                icon: 'h-10 w-10',
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
    ({ className, variant, size, fullWidth, loading, disabled, children, ...props }, ref) => {
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