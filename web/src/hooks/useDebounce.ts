import { useState, useEffect } from 'react';

/**
 * Retourne une version "debouncée" d'une valeur — ne change
 * qu'après `delay` ms sans nouvelle mise à jour.
 *
 * Utilisé pour limiter les appels à /transfers/simulate
 * pendant que l'utilisateur tape le montant.
 *
 * @example
 * const debouncedAmount = useDebounce(amount, 400);
 */
export function useDebounce<T>(value: T, delay = 400): T {
    const [debounced, setDebounced] = useState(value);

    useEffect(() => {
        const timer = setTimeout(() => setDebounced(value), delay);
        return () => clearTimeout(timer);
    }, [value, delay]);

    return debounced;
}