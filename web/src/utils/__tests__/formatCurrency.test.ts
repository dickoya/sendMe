import { describe, it, expect } from 'vitest';
import { formatCurrency } from '../formatCurrency';

// Intl.NumberFormat('fr-FR') utilise un espace fin insécable (\u202f) comme séparateur
const NBSP = '\u202f';

describe('formatCurrency', () => {
    it('formate EUR avec 2 décimales', () => {
        expect(formatCurrency(203.5, 'EUR')).toBe('203,50 EUR');
    });

    it('formate EUR avec montant entier', () => {
        expect(formatCurrency(200, 'EUR')).toBe('200,00 EUR');
    });

    it('formate XOF sans décimale', () => {
        expect(formatCurrency(128745, 'XOF')).toBe(`128${NBSP}745 XOF`);
    });

    it('formate XAF sans décimale', () => {
        expect(formatCurrency(50000, 'XAF')).toBe(`50${NBSP}000 XAF`);
    });

    it('formate GNF sans décimale', () => {
        expect(formatCurrency(100000, 'GNF')).toBe(`100${NBSP}000 GNF`);
    });

    it('formate USD avec 2 décimales', () => {
        expect(formatCurrency(99.99, 'USD')).toBe('99,99 USD');
    });

    it('formate GBP avec 2 décimales', () => {
        expect(formatCurrency(150.75, 'GBP')).toBe('150,75 GBP');
    });

    it('formate CAD avec 2 décimales', () => {
        expect(formatCurrency(200.50, 'CAD')).toBe('200,50 CAD');
    });

    it('formate CHF avec 2 décimales', () => {
        expect(formatCurrency(100.00, 'CHF')).toBe('100,00 CHF');
    });

    it('formate NGN avec 2 décimales', () => {
        expect(formatCurrency(50000.50, 'NGN')).toBe(`50${NBSP}000,50 NGN`);
    });

    it('formate GHS avec 2 décimales', () => {
        expect(formatCurrency(1000.99, 'GHS')).toBe(`1${NBSP}000,99 GHS`);
    });

    it('formate ZAR avec 2 décimales', () => {
        expect(formatCurrency(2500.00, 'ZAR')).toBe(`2${NBSP}500,00 ZAR`);
    });

    it('formate MAD avec 2 décimales', () => {
        expect(formatCurrency(1500.50, 'MAD')).toBe(`1${NBSP}500,50 MAD`);
    });

    it('formate TND avec 3 décimales', () => {
        expect(formatCurrency(50.123, 'TND')).toBe('50,123 TND');
    });

    it('formate zéro', () => {

        expect(formatCurrency(0, 'EUR')).toBe('0,00 EUR');
    });

    it('formate montant négatif', () => {
        expect(formatCurrency(-50, 'EUR')).toBe('-50,00 EUR');
    });

    it('utilise le séparateur de milliers français (espace fin insécable)', () => {
        expect(formatCurrency(1000000, 'EUR')).toBe(`1${NBSP}000${NBSP}000,00 EUR`);
    });
});
