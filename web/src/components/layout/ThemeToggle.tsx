import { Moon, Sun } from 'lucide-react';
import { useTheme } from '@/store/theme/themeContext';
import { cn } from '@/lib/utils';

export interface ThemeToggleProps {
    className?: string;
}

/**
 * Bouton de basculement entre le mode clair et le mode sombre.
 * Affiche une icône de lune en mode clair et une icône de soleil en mode sombre.
 */
function ThemeToggle({ className }: ThemeToggleProps) {
    const { theme, toggleTheme } = useTheme();

    return (
        <button
            onClick={toggleTheme}
            className={cn(
                'relative rounded-lg p-2 text-gray-500 hover:bg-gray-100 dark:text-[#8B8FA3] dark:hover:bg-[#222222] transition-all duration-300',
                'dark:hover:text-brand-400 dark:hover:shadow-[0_0_12px_rgba(90,159,224,0.15)]',
                className
            )}
            aria-label={theme === 'dark' ? 'Activer le mode clair' : 'Activer le mode sombre'}
            title={theme === 'dark' ? 'Mode clair' : 'Mode sombre'}
        >
            <span className="block transition-transform duration-500 ease-in-out rotate-0 dark:-rotate-90">
                {theme === 'dark' ? <Sun size={18} className="animate-[spin_0.5s_ease-in-out]" /> : <Moon size={18} />}
            </span>
        </button>
    );
}


export default ThemeToggle;
