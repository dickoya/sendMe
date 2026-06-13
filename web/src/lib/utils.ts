import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { isAxiosError } from 'axios';

/**
 * Fusionne des classes Tailwind en gérant les conflits.
 * Exemple : cn('px-2 py-1', condition && 'bg-red-500', 'px-4')
 * → 'py-1 bg-red-500 px-4' (px-4 écrase px-2)
 */
export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}


/**
 * Extrait un message d'erreur lisible depuis une erreur Axios.
 * Le backend SendMe retourne toujours { success: false, message: "..." }
 * via GlobalExceptionHandler.
 *
 * @example
 * try { ... } catch (err) {
 *   showToast(getErrorMessage(err), 'error');
 * }
 */
export function getErrorMessage(error: unknown): string {
    if (isAxiosError(error)) {
        const data = error.response?.data as { message?: string } | undefined;
        if (data?.message) return data.message;
        if (error.message) return error.message;
    }
    if (error instanceof Error) return error.message;
    return 'Une erreur inattendue est survenue';
}