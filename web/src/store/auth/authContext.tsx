import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { Role, AuthResponse } from '@/types';
import { STORAGE_KEYS } from '@/utils/constants';
import { authService, type LoginPayload, type RegisterPayload } from '@/services/authService';

export interface AuthUser {
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
}

interface AuthContextValue {
    user: AuthUser | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    /** Erreur du dernier login/register, à afficher dans le formulaire */
    error: string | null;
    login: (payload: LoginPayload) => Promise<void>;
    register: (payload: RegisterPayload) => Promise<void>;
    logout: () => void;
    clearError: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

/**
 * Construit un AuthUser à partir de la réponse du backend
 * et le persiste dans le localStorage avec les tokens.
 */
function persistSession(response: AuthResponse): AuthUser {
    const user: AuthUser = {
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        role: response.role,
    };

    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));

    return user;
}

/**
 * Provider global d'authentification.
 *
 * Au montage, restaure la session depuis le localStorage
 * (évite une déconnexion au rechargement de la page).
 *
 * Expose login() et register() qui appellent l'API,
 * persistent la session et mettent à jour le state.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Restaurer la session au montage
    useEffect(() => {
        const stored = localStorage.getItem(STORAGE_KEYS.USER);
        const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);

        if (stored && token) {
            try {
                setUser(JSON.parse(stored));
            } catch {
                // JSON corrompu → ignorer et rester déconnecté
            }
        }
        setIsLoading(false);
    }, []);

    const login = async (payload: LoginPayload) => {
        setError(null);
        try {
            const response = await authService.login(payload);
            setUser(persistSession(response));
        } catch (err) {
            setError(extractMessage(err));
            throw err;
        }
    };

    const register = async (payload: RegisterPayload) => {
        setError(null);
        try {
            const response = await authService.register(payload);
            setUser(persistSession(response));
        } catch (err) {
            setError(extractMessage(err));
            throw err;
        }
    };

    const logout = () => {
        localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER);
        setUser(null);
    };

    const clearError = () => setError(null);

    return (
        <AuthContext.Provider
            value={{ user, isAuthenticated: !!user, isLoading, error, login, register, logout, clearError }}
        >
            {children}
        </AuthContext.Provider>
    );
}

/**
 * Hook pour accéder à l'état et aux actions d'authentification.
 *
 * @example
 * const { user, login, isAuthenticated } = useAuth();
 * await login({ email, password });
 */
export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth doit être utilisé dans un AuthProvider');
    return context;
}

// Import local pour éviter une dépendance circulaire avec lib/utils
import { getErrorMessage as extractMessage } from '@/lib/utils';