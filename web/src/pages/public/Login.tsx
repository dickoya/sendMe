import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { useAuth } from '@/store/auth/authContext';
import { useToast } from '@/store/notification/toastContext';

/**
 * Page de connexion.
 *
 * Flux :
 * 1. Soumission du formulaire → useAuth().login()
 * 2. En cas de succès → redirection vers /dashboard
 * 3. En cas d'échec → message d'erreur affiché sous le formulaire
 */
function Login() {
    const { login, error, clearError } = useAuth();
    const { showToast } = useToast();
    const navigate = useNavigate();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        clearError();
        setIsSubmitting(true);

        try {
            // 1. On récupère l'utilisateur connecté
            const loggedUser = await login({ email, password });
            showToast('Connexion réussie. Bienvenue !', 'success');

            // 2. Redirection conditionnelle selon le rôle
            if (loggedUser && loggedUser.role === 'ROLE_ADMIN') {
                navigate('/admin'); // Redirection vers l'espace administration
            } else {
                navigate('/dashboard'); // Redirection vers l'espace utilisateur standard
            }
        } catch {
            // L'erreur est capturée et gérée par le contexte
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="flex flex-col gap-6">
            <div>
                <h2 className="text-2xl font-bold text-gray-900">Connexion</h2>
                <p className="mt-1 text-sm text-gray-500">
                    Accédez à votre espace SendMe
                </p>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <Input
                    label="Email"
                    type="email"
                    placeholder="jean@example.com"
                    icon={<Mail size={16} />}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />
                <Input
                    label="Mot de passe"
                    type="password"
                    placeholder="••••••••"
                    icon={<Lock size={16} />}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                {error && (
                    <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">
                        {error}
                    </div>
                )}

                <Button type="submit" size="lg" fullWidth loading={isSubmitting}>
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