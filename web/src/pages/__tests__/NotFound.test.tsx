import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import NotFound from '../NotFound';

// Mock du composant Button pour éviter les dépendances
vi.mock('@/components/ui/Button', () => ({
    default: ({ children }: { children: React.ReactNode }) => (
        <button>{children}</button>
    ),
}));

describe('NotFound', () => {
    it('affiche le code 404', () => {
        render(
            <BrowserRouter>
                <NotFound />
            </BrowserRouter>
        );
        expect(screen.getByText('404')).toBeInTheDocument();
    });

    it('affiche le message "Page introuvable"', () => {
        render(
            <BrowserRouter>
                <NotFound />
            </BrowserRouter>
        );
        expect(screen.getByText('Page introuvable')).toBeInTheDocument();
    });

    it('affiche un lien vers le tableau de bord', () => {
        render(
            <BrowserRouter>
                <NotFound />
            </BrowserRouter>
        );
        const link = screen.getByRole('link', { name: /retour au tableau de bord/i });
        expect(link).toBeInTheDocument();
        expect(link).toHaveAttribute('href', '/dashboard');
    });
});
