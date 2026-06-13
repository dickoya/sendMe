import { Link } from 'react-router-dom';
import Button from '@/components/ui/Button';

/**
 * Page 404 — affichée pour toute route inconnue.
 */
function NotFound() {
    return (
        <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-gray-50 text-center">
            <h1 className="text-6xl font-bold text-brand-600">404</h1>
            <p className="text-lg text-gray-600">Page introuvable</p>
            <Link to="/dashboard">
                <Button>Retour au tableau de bord</Button>
            </Link>
        </div>
    );
}

export default NotFound;