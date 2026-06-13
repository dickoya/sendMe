import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

/**
 * Formate une date ISO en format lisible français.
 *
 * @example
 * formatDate('2025-06-09T14:32:00') // "09 juin 2025 à 14:32"
 */
export function formatDate(isoDate: string): string {
    return format(new Date(isoDate), "dd MMMM yyyy 'à' HH:mm", { locale: fr });
}

/**
 * Formate une date ISO en format court.
 *
 * @example
 * formatDateShort('2025-06-09T14:32:00') // "09/06/2025"
 */
export function formatDateShort(isoDate: string): string {
    return format(new Date(isoDate), 'dd/MM/yyyy', { locale: fr });
}