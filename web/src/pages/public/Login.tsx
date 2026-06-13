import { Link } from 'react-router-dom';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { Mail, Lock } from 'lucide-react';

/**
 * Page de connexion — placeholder visuel.
 * La logique d'authentification sera ajoutée à l'Étape 4.
 */
function Login() {
    return (
        <div className="flex flex-col gap-6">
            <div>
                <h2 className="text-2xl font-bold text-gray-900">Connexion</h2>
                <p className="mt-1 text-sm text-gray-500">
                    Accédez à votre espace SendMe
                </p>
            </div>

            <form className="flex flex-col gap-4">
                <Input label="Email" type="email" placeholder="jean@example.com" icon={<Mail size={16} />} required />
                <Input label="Mot de passe" type="password" placeholder="••••••••" icon={<Lock size={16} />} required />
                <Button type="submit" size="lg" fullWidth>
                    Se connecter
                </Button>
            </form>

            <p className="text-center text-sm text-gray-500">
                Pas encore de compte ?{' '}
                <Link to="/register" className="font-medium text-brand-600 hover:underline">
                    S'inscrire
                </Link>
            </p>
        </div>
    );
}

export default Login;