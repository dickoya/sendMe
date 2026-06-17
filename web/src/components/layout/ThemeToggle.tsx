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
                'rounded-lg p-2 text-gray-500 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800 transition-colors',
                className
            )}
            aria-label={theme === 'dark' ? 'Activer le mode clair' : 'Activer le mode sombre'}
            title={theme === 'dark' ? 'Mode clair' : 'Mode sombre'}
        >
            {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </button>
    );
}

export default ThemeToggle;
