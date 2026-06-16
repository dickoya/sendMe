import { describe, it, expect } from 'vitest';
import { formatDate, formatDateShort } from '../formatDate';

describe('formatDate', () => {
    it('formate une date ISO en français', () => {
        const result = formatDate('2025-06-09T14:32:00');
        expect(result).toContain('juin');
        expect(result).toContain('2025');
        expect(result).toContain('14:32');
    });

    it('inclut le mot "à" entre date et heure', () => {
        const result = formatDate('2025-06-09T14:32:00');
        expect(result).toContain('à');
    });

    it('gère une date en début de mois', () => {
        const result = formatDate('2025-01-01T00:00:00');
        expect(result).toContain('janvier');
    });
});

describe('formatDateShort', () => {
    it('formate en JJ/MM/AAAA', () => {
        expect(formatDateShort('2025-06-09T14:32:00')).toBe('09/06/2025');
    });

    it('gère le premier jour du mois', () => {
        expect(formatDateShort('2025-01-01T00:00:00')).toBe('01/01/2025');
    });

    it('gère le dernier jour de l\'année', () => {
        expect(formatDateShort('2025-12-31T23:59:59')).toBe('31/12/2025');
    });
});
