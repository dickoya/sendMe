import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

type Theme = 'light' | 'dark';
type ThemePreference = Theme | 'system';

interface ThemeContextValue {
    theme: Theme;
    toggleTheme: () => void;
    setTheme: (theme: Theme) => void;
}

const ThemeContext = createContext<ThemeContextValue | null>(null);

const STORAGE_KEY = 'sendme-theme';
const SYSTEM_THEME_QUERY = '(prefers-color-scheme: dark)';

/**
 * Provider du thème sombre/clair.
 * Persiste le choix dans localStorage et applique la classe "dark" sur <html>.
 */
export function ThemeProvider({ children }: { children: ReactNode }) {
    const getSystemTheme = (): Theme =>
        window.matchMedia(SYSTEM_THEME_QUERY).matches ? 'dark' : 'light';

    const [theme, setThemeState] = useState<Theme>(() => {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored === 'dark' || stored === 'light') return stored;
        // Aucun choix explicite : on suit la préférence du système.
        return getSystemTheme();
    });

    const [preference, setPreference] = useState<ThemePreference>(() => {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored === 'dark' || stored === 'light') return stored;
        return 'system';
    });

    useEffect(() => {
        const root = document.documentElement;
        if (theme === 'dark') {
            root.classList.add('dark');
        } else {
            root.classList.remove('dark');
        }
    }, [theme]);

    useEffect(() => {
        if (preference === 'system') {
            localStorage.removeItem(STORAGE_KEY);
        } else {
            localStorage.setItem(STORAGE_KEY, preference);
        }
    }, [preference]);

    useEffect(() => {
        const mediaQuery = window.matchMedia(SYSTEM_THEME_QUERY);

        const handleChange = (event: MediaQueryListEvent) => {
            if (preference === 'system') {
                setThemeState(event.matches ? 'dark' : 'light');
            }
        };

        mediaQuery.addEventListener('change', handleChange);
        return () => mediaQuery.removeEventListener('change', handleChange);
    }, [preference]);

    const toggleTheme = () => {
        const next = theme === 'light' ? 'dark' : 'light';
        setPreference(next);
        setThemeState(next);
    };

    const setTheme = (newTheme: Theme) => {
        setPreference(newTheme);
        setThemeState(newTheme);
    };

    return (
        <ThemeContext.Provider value={{ theme, toggleTheme, setTheme }}>
            {children}
        </ThemeContext.Provider>
    );
}

export function useTheme() {
    const context = useContext(ThemeContext);
    if (!context) throw new Error('useTheme doit être utilisé dans un ThemeProvider');
    return context;
}
