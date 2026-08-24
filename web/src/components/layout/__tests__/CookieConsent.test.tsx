import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import CookieConsent from '../CookieConsent';

// Mock de la traduction pour éviter l'initialisation d'i18next dans les tests.
vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key: string) => {
            const map: Record<string, string> = {
                'cookieConsent.label': 'Consentement aux cookies',
                'cookieConsent.message': 'Ce site utilise uniquement les cookies nécessaires.',
                'cookieConsent.accept': 'Accepter',
                'cookieConsent.decline': 'Refuser',
                'cookieConsent.policyLink': 'Consulter la politique des cookies',
            };
            return map[key] ?? key;
        },
    }),
}));

const CONSENT_KEY = 'sendme_cookie_consent';

describe('CookieConsent', () => {
    beforeEach(() => {
        localStorage.clear();
    });

    it('affiche la bannière quand aucun choix n\'est enregistré', () => {
        render(
            <BrowserRouter>
                <CookieConsent />
            </BrowserRouter>
        );
        expect(screen.getByRole('button', { name: /accepter/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /refuser/i })).toBeInTheDocument();
    });

    it('ne s\'affiche pas quand un choix est déjà enregistré', () => {
        localStorage.setItem(CONSENT_KEY, 'accepted');
        render(
            <BrowserRouter>
                <CookieConsent />
            </BrowserRouter>
        );
        expect(screen.queryByRole('button', { name: /accepter/i })).not.toBeInTheDocument();
    });

    it('masque la bannière et mémorise le choix après clic sur Accepter', async () => {
        const user = userEvent.setup();
        render(
            <BrowserRouter>
                <CookieConsent />
            </BrowserRouter>
        );
        await user.click(screen.getByRole('button', { name: /accepter/i }));
        expect(screen.queryByRole('button', { name: /accepter/i })).not.toBeInTheDocument();
        expect(localStorage.getItem(CONSENT_KEY)).toBe('accepted');
    });

    it('mémorise le refus après clic sur Refuser', async () => {
        const user = userEvent.setup();
        render(
            <BrowserRouter>
                <CookieConsent />
            </BrowserRouter>
        );
        await user.click(screen.getByRole('button', { name: /refuser/i }));
        expect(localStorage.getItem(CONSENT_KEY)).toBe('declined');
    });

    it('propose un lien vers la page des mentions légales', () => {
        render(
            <BrowserRouter>
                <CookieConsent />
            </BrowserRouter>
        );
        const link = screen.getByRole('link', { name: /politique des cookies/i });
        expect(link).toHaveAttribute('href', '/legal');
    });
});
