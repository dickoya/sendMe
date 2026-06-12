import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Fusionne des classes Tailwind en gérant les conflits.
 * Exemple : cn('px-2 py-1', condition && 'bg-red-500', 'px-4')
 * → 'py-1 bg-red-500 px-4' (px-4 écrase px-2)
 */
export function cn(...inputs: ClassValue[]) {
    return twMerge(clsx(inputs));
}