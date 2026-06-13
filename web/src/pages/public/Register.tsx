import { Link } from 'react-router-dom';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { Mail, Lock, User, Phone } from 'lucide-react';

/**
 * Page d'inscription — placeholder visuel.
 * La logique d'inscription sera ajoutée à l'Étape 4.
 */
function Register() {
    return (
        <div className="flex flex-col gap-6">
            <div>
                <h2 className="text-2xl font-bold text-gray-900">Créer un compte</h2>
                <p className="mt-1 text-sm text-gray-500">
                    Rejoignez SendMe en quelques secondes
                </p>
            </div>

            <form className="flex flex-col gap-4">
                <div className="grid grid-cols-2 gap-4">
                    <Input label="Prénom" placeholder="Jean" icon={<User size={16} />} required />
                    <Input label="Nom" placeholder="Dupont" required />
                </div>
                <Input label="Email" type="email" placeholder="jean@example.com" icon={<Mail size={16} />} required />
                <Input label="Téléphone" type="tel" placeholder="+33 6 00 00 00 00" icon={<Phone size={16} />} />
                <Input label="Mot de passe" type="password" placeholder="••••••••" icon={<Lock size={16} />} required hint="Minimum 8 caractères" />
                <Button type="submit" size="lg" fullWidth>
                    Créer mon compte
                </Button>
            </form>

            <p className="text-center text-sm text-gray-500">
                Déjà un compte ?{' '}
                <Link to="/login" className="font-medium text-brand-600 hover:underline">
                    Se connecter
                </Link>
            </p>
        </div>
    );
}

export default Register;