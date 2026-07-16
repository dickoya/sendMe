import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { Role, AuthResponse } from '@/types';
import { STORAGE_KEYS } from '@/utils/constants';
import { getErrorMessage as extractMessage } from '@/lib/utils';
import { authService, type LoginPayload, type RegisterPayload } from '@/services/authService';

export interface AuthUser {
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
    referralCode?: string;
}

interface AuthContextValue {
    user: AuthUser | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    /** Erreur du dernier login/register, à afficher dans le formulaire */
    error: string | null;
    login: (payload: LoginPayload) => Promise<AuthUser>;
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
        referralCode: response.referralCode,
    };

    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.accessToken);
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
    localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));

    return user;
}

/**
 * Provider global d'authentification.
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

    const login = async (payload: LoginPayload): Promise<AuthUser> => {
        setError(null);
        try {
            const response = await authService.login(payload);
            const sessionUser = persistSession(response);
            setUser(sessionUser);
            return sessionUser;
        } catch (err) {
            setError(extractMessage(err));
            throw err;
        }
    };

    const register = async (payload: RegisterPayload) => {
        setError(null);
        try {
            const response = await authService.register(payload);
            // Après inscription, l'utilisateur doit vérifier son email avant de pouvoir se connecter.
            // L'API ne retourne pas de tokens JWT tant que l'email n'est pas vérifié.
            // On ne persiste donc pas la session ici.
            // La redirection vers la page "vérification envoyée" est gérée par Register.tsx
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

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth doit être utilisé dans un AuthProvider');
    return context;
}

