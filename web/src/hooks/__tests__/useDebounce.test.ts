import { describe, it, expect, vi, afterEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useDebounce } from '../useDebounce';

describe('useDebounce', () => {
    afterEach(() => {
        vi.useRealTimers();
    });

    it('retourne la valeur initiale immédiatement', () => {
        const { result } = renderHook(() => useDebounce('initial', 400));
        expect(result.current).toBe('initial');
    });

    it('retourne la valeur mise à jour après le délai', () => {
        vi.useFakeTimers();
        const { result, rerender } = renderHook(
            ({ value, delay }) => useDebounce(value, delay),
            { initialProps: { value: 'initial', delay: 400 } }
        );

        // Mettre à jour la valeur
        rerender({ value: 'updated', delay: 400 });

        // Avant le délai, la valeur doit encore être l'ancienne
        expect(result.current).toBe('initial');

        // Avancer le temps
        act(() => {
            vi.advanceTimersByTime(400);
        });

        // Après le délai, la valeur doit être mise à jour
        expect(result.current).toBe('updated');
    });

    it('annule le timer si la valeur change avant la fin du délai', () => {
        vi.useFakeTimers();
        const { result, rerender } = renderHook(
            ({ value, delay }) => useDebounce(value, delay),
            { initialProps: { value: 'initial', delay: 400 } }
        );

        // Changer la valeur rapidement plusieurs fois
        rerender({ value: 'intermediate', delay: 400 });
        act(() => {
            vi.advanceTimersByTime(200);
        });

        rerender({ value: 'final', delay: 400 });
        act(() => {
            vi.advanceTimersByTime(400);
        });

        // Seule la dernière valeur doit être prise en compte
        expect(result.current).toBe('final');
    });

    it('utilise le délai par défaut de 400ms', () => {
        vi.useFakeTimers();
        const { result, rerender } = renderHook(
            ({ value }) => useDebounce(value),
            { initialProps: { value: 'v1' } }
        );

        rerender({ value: 'v2' });

        act(() => {
            vi.advanceTimersByTime(399);
        });
        expect(result.current).toBe('v1');

        act(() => {
            vi.advanceTimersByTime(1);
        });
        expect(result.current).toBe('v2');
    });

    it('gère les changements de délai', () => {
        vi.useFakeTimers();
        const { result, rerender } = renderHook(
            ({ value, delay }) => useDebounce(value, delay),
            { initialProps: { value: 'v1', delay: 400 } }
        );

        rerender({ value: 'v2', delay: 200 });

        act(() => {
            vi.advanceTimersByTime(200);
        });

        expect(result.current).toBe('v2');
    });
});
