import { describe, it, expect } from 'vitest';
import { cn, getErrorMessage } from '../utils';
import { AxiosError } from 'axios';

describe('cn', () => {
    it('fusionne des classes simples', () => {
        expect(cn('px-2', 'py-1')).toBe('px-2 py-1');
    });

    it('gère les conditions', () => {
        expect(cn('px-2', true && 'bg-red-500')).toBe('px-2 bg-red-500');
        expect(cn('px-2', false && 'bg-red-500')).toBe('px-2');
    });

    it('résout les conflits Tailwind (dernier gagne)', () => {
        expect(cn('px-2', 'px-4')).toBe('px-4');
    });

    it('ignore les valeurs null/undefined', () => {
        expect(cn('px-2', null, undefined, 'py-1')).toBe('px-2 py-1');
    });

    it('gère les classes conditionnelles avec clsx', () => {
        expect(cn('base', { active: true, disabled: false })).toBe('base active');
    });
});

describe('getErrorMessage', () => {
    it('extrait le message depuis une erreur Axios avec data.message', () => {
        const error = new AxiosError();
        error.response = {
            data: { message: 'Bénéficiaire non trouvé' },
            status: 404,
            statusText: 'Not Found',
            headers: {},
            config: {} as any,
        };
        expect(getErrorMessage(error)).toBe('Bénéficiaire non trouvé');
    });

    it('extrait le message depuis une erreur Axios sans data.message', () => {
        const error = new AxiosError('Network Error');
        expect(getErrorMessage(error)).toBe('Network Error');
    });

    it('extrait le message depuis une Error standard', () => {
        const error = new Error('Erreur inattendue');
        expect(getErrorMessage(error)).toBe('Erreur inattendue');
    });

    it('retourne un message par défaut pour les erreurs inconnues', () => {
        expect(getErrorMessage('string error')).toBe('Une erreur inattendue est survenue');
        expect(getErrorMessage(null)).toBe('Une erreur inattendue est survenue');
        expect(getErrorMessage(42)).toBe('Une erreur inattendue est survenue');
    });
});
