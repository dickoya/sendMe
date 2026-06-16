import { describe, it, expect } from 'vitest';
import { getChannelIcon, getCurrencyForChannel } from '../constants';

describe('getChannelIcon', () => {
    it('retourne 🏦 pour BANK_TRANSFER', () => {
        expect(getChannelIcon('BANK_TRANSFER')).toBe('🏦');
    });

    it('retourne 🟠 pour ORANGE_MONEY', () => {
        expect(getChannelIcon('ORANGE_MONEY')).toBe('🟠');
    });

    it('retourne 🟡 pour MTN_MOBILE_MONEY', () => {
        expect(getChannelIcon('MTN_MOBILE_MONEY')).toBe('🟡');
    });

    it('retourne 🔵 pour WAVE', () => {
        expect(getChannelIcon('WAVE')).toBe('🔵');
    });

    it('retourne 🟢 pour MOOV_MONEY', () => {
        expect(getChannelIcon('MOOV_MONEY')).toBe('🟢');
    });

    it('retourne 🔴 pour AIRTEL_MONEY', () => {
        expect(getChannelIcon('AIRTEL_MONEY')).toBe('🔴');
    });
});

describe('getCurrencyForChannel', () => {
    it('retourne EUR pour BANK_TRANSFER', () => {
        expect(getCurrencyForChannel('BANK_TRANSFER')).toBe('EUR');
    });

    it('retourne GNF pour ORANGE_MONEY', () => {
        expect(getCurrencyForChannel('ORANGE_MONEY')).toBe('GNF');
    });

    it('retourne GNF pour MTN_MOBILE_MONEY', () => {
        expect(getCurrencyForChannel('MTN_MOBILE_MONEY')).toBe('GNF');
    });

    it('retourne XOF pour WAVE', () => {
        expect(getCurrencyForChannel('WAVE')).toBe('XOF');
    });

    it('retourne XOF pour MOOV_MONEY', () => {
        expect(getCurrencyForChannel('MOOV_MONEY')).toBe('XOF');
    });

    it('retourne XOF pour AIRTEL_MONEY', () => {
        expect(getCurrencyForChannel('AIRTEL_MONEY')).toBe('XOF');
    });
});
