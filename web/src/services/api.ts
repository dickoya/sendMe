import axios, { type InternalAxiosRequestConfig, type AxiosError } from 'axios';
import { API_BASE_URL, STORAGE_KEYS } from '@/utils/constants';

/**
 * Instance Axios centrale pour tous les appels au backend SendMe.
 *
 * Intercepteurs configurés :
 * - Request  : ajoute automatiquement le header Authorization: Bearer <token>
 * - Response : si 401 reçu, déconnecte l'utilisateur et redirige vers /login
 *   (le refresh token n'est pas implémenté côté backend pour l'instant —
 *   l'utilisateur doit simplement se reconnecter)
 */
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// ── Intercepteur de requête ────────────────────────────────────────────
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// ── Intercepteur de réponse ────────────────────────────────────────────
api.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        if (error.response?.status === 401) {
            // Token invalide ou expiré → nettoyer le storage et rediriger
            localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
            localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
            localStorage.removeItem(STORAGE_KEYS.USER);

            // Évite une boucle de redirection si déjà sur /login
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;