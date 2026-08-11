import axios, { type InternalAxiosRequestConfig, type AxiosError } from 'axios';
import { API_BASE_URL, STORAGE_KEYS } from '@/utils/constants';

/**
 * Instance Axios centrale pour tous les appels au backend SendMe.
 *
 * Intercepteurs configurés :
 * - Request  : ajoute automatiquement le header Authorization: Bearer <token>
 * - Response : si 401 reçu, tente un refresh automatique du token.
 *   Si le refresh réussit, la requête originale est rejouée avec le nouveau token.
 *   Si le refresh échoue, l'utilisateur est déconnecté et redirigé vers /login.
 *
 * Le refresh token lui-même n'est pas intercepté (évite les boucles infinies).
 */
const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// État du refresh
/** Flag pour éviter les refresh concurrents */
let isRefreshing = false;

/** File d'attente des requêtes en attente pendant le refresh */
interface QueueItem {
    resolve: (token: string) => void;
    reject: (error: unknown) => void;
}
let failedQueue: QueueItem[] = [];

/**
 * Traite la file d'attente après un refresh réussi ou échoué.
 */
function processQueue(error: unknown, token: string | null = null) {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) {
            reject(error);
        } else if (token) {
            resolve(token);
        }
    });
    failedQueue = [];
}

// Intercepteur de requête
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN);
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Intercepteur de réponse
api.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        // Ne pas intercepter si :
        // - Pas de réponse (erreur réseau)
        // - Status différent de 401
        // - C'est déjà une tentative de refresh
        // - C'est la requête de refresh elle-même
        if (
            !error.response ||
            error.response.status !== 401 ||
            originalRequest._retry ||
            originalRequest.url === '/auth/refresh'
        ) {
            return Promise.reject(error);
        }

        // Si un refresh est déjà en cours, mettre en attente
        if (isRefreshing) {
            return new Promise<string>((resolve, reject) => {
                failedQueue.push({ resolve, reject });
            }).then((newToken) => {
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
                return api(originalRequest);
            });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
            const refreshToken = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
            if (!refreshToken) {
                throw new Error('Aucun refresh token disponible');
            }

            // Appel à l'API pour rafraîchir les tokens
            const { authService } = await import('@/services/authService');
            const response = await authService.refresh(refreshToken);

            // Mettre à jour le localStorage
            localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, response.accessToken);
            localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);

            // Mettre à jour l'utilisateur si présent
            const storedUser = localStorage.getItem(STORAGE_KEYS.USER);
            if (storedUser) {
                const user = JSON.parse(storedUser);
                localStorage.setItem(
                    STORAGE_KEYS.USER,
                    JSON.stringify({
                        ...user,
                        email: response.email,
                        firstName: response.firstName,
                        lastName: response.lastName,
                        role: response.role,
                    })
                );
            }

            // Traiter la file d'attente
            processQueue(null, response.accessToken);

            // Rejouer la requête originale avec le nouveau token
            originalRequest.headers.Authorization = `Bearer ${response.accessToken}`;
            return api(originalRequest);
        } catch (refreshError) {
            // Le refresh a échoué → déconnecter l'utilisateur
            processQueue(refreshError, null);

            localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN);
            localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
            localStorage.removeItem(STORAGE_KEYS.USER);

            // Évite une boucle de redirection si déjà sur /login
            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }

            return Promise.reject(refreshError);
        } finally {
            isRefreshing = false;
        }
    }
);

export default api;
