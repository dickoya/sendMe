import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Button from '../Button';

describe('Button', () => {
    it('affiche le texte du bouton', () => {
        render(<Button>Cliquez ici</Button>);
        expect(screen.getByRole('button', { name: /cliquez ici/i })).toBeInTheDocument();
    });

    it('applique la variante primary par défaut', () => {
        render(<Button>Primary</Button>);
        const button = screen.getByRole('button');
        expect(button.className).toContain('bg-brand-600');
    });

    it('applique la variante danger', () => {
        render(<Button variant="danger">Danger</Button>);
        const button = screen.getByRole('button');
        expect(button.className).toContain('bg-danger-500');
    });

    it('applique la taille sm', () => {
        render(<Button size="sm">Small</Button>);
        const button = screen.getByRole('button');
        expect(button.className).toContain('h-8');
    });

    it('applique fullWidth', () => {
        render(<Button fullWidth>Full</Button>);
        const button = screen.getByRole('button');
        expect(button.className).toContain('w-full');
    });

    it('désactive le bouton avec la prop disabled', () => {
        render(<Button disabled>Désactivé</Button>);
        expect(screen.getByRole('button')).toBeDisabled();
    });

    it('désactive le bouton avec la prop loading', () => {
        render(<Button loading>Chargement</Button>);
        expect(screen.getByRole('button')).toBeDisabled();
    });

    it('affiche un spinner quand loading est true', () => {
        render(<Button loading>Chargement</Button>);
        const button = screen.getByRole('button');
        const spinner = button.querySelector('svg.animate-spin');
        expect(spinner).toBeInTheDocument();
    });

    it('déclenche onClick au clic', async () => {
        const user = userEvent.setup();
        let clicked = false;
        render(<Button onClick={() => { clicked = true; }}>Cliquer</Button>);

        await user.click(screen.getByRole('button'));
        expect(clicked).toBe(true);
    });

    it('ne déclenche pas onClick quand désactivé', async () => {
        const user = userEvent.setup();
        let clicked = false;
        render(<Button disabled onClick={() => { clicked = true; }}>Cliquer</Button>);

        await user.click(screen.getByRole('button'));
        expect(clicked).toBe(false);
    });

    it('passe les classes supplémentaires', () => {
        render(<Button className="extra-class">Custom</Button>);
        const button = screen.getByRole('button');
        expect(button.className).toContain('extra-class');
    });
});
