import { Outlet } from 'react-router-dom';

/**
 * Layout pour les pages publiques (Login, Register).
 * Centre le contenu sur un fond dégradé bleu — esthétique fintech.
 */
function PublicLayout() {
    return (
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-brand-700 via-brand-600 to-brand-400 p-4">
            <div className="w-full max-w-md">
                {/* Logo */}
                <div className="mb-6 flex items-center justify-center gap-2">
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white text-lg font-bold text-brand-600 shadow-md">
                        S
                    </div>
                    <span className="text-2xl font-bold text-white">SendMe</span>
                </div>

                {/* Carte contenant le formulaire */}
                <div className="rounded-2xl bg-white p-8 shadow-xl">
                    <Outlet />
                </div>

                <p className="mt-6 text-center text-sm text-brand-100">
                    © 2025 SendMe — Passerelle de transfert d'argent
                </p>
            </div>
        </div>
    );
}

export default PublicLayout;