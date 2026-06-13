import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Phone } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { useAuth } from '@/store/auth/authContext';
import { useToast } from '@/store/notification/toastContext';

/**
 * Page d'inscription.
 *
 * Validation côté client :
 * - Tous les champs obligatoires (sauf téléphone) doivent être remplis
 * - Le mot de passe doit faire au moins 8 caractères
 *   (le backend valide aussi, mais on évite un aller-retour inutile)
 */
function Register() {
    const { register, error, clearError } = useAuth();
    const { showToast } = useToast();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        firstName: '',
        lastName: '',
        email: '',
        phoneNumber: '',
        password: '',
    });
    const [passwordHint, setPasswordHint] = useState<string | undefined>(
        'Minimum 8 caractères'
    );
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (field: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm((prev) => ({ ...prev, [field]: e.target.value }));
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        clearError();

        // Validation locale rapide du mot de passe
        if (form.password.length < 8) {
            setPasswordHint(undefined);
            return;
        }

        setIsSubmitting(true);
        try {
            await register({
                firstName: form.firstName,
                lastName: form.lastName,
                email: form.email,
                password: form.password,
                phoneNumber: form.phoneNumber || undefined,
            });
            showToast('Compte créé avec succès. Bienvenue sur SendMe !', 'success');
            navigate('/dashboard');
        } catch {
            // Erreur affichée via le context
        } finally {
            setIsSubmitting(false);
        }
    };

    const passwordError =
        form.password.length > 0 && form.password.length < 8
            ? 'Le mot de passe doit contenir au moins 8 caractères'
            : undefined;

    return (
        <div className="flex flex-col gap-6">
            <div>
                <h2 className="text-2xl font-bold text-gray-900">Créer un compte</h2>
                <p className="mt-1 text-sm text-gray-500">
                    Rejoignez SendMe en quelques secondes
                </p>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="grid grid-cols-2 gap-4">
                    <Input
                        label="Prénom"
                        placeholder="Jean"
                        icon={<User size={16} />}
                        value={form.firstName}
                        onChange={handleChange('firstName')}
                        required
                    />
                    <Input
                        label="Nom"
                        placeholder="Dupont"
                        value={form.lastName}
                        onChange={handleChange('lastName')}
                        required
                    />
                </div>

                <Input
                    label="Email"
                    type="email"
                    placeholder="jean@example.com"
                    icon={<Mail size={16} />}
                    value={form.email}
                    onChange={handleChange('email')}
                    required
                />

                <Input
                    label="Téléphone"
                    type="tel"
                    placeholder="+33 6 00 00 00 00"
                    icon={<Phone size={16} />}
                    value={form.phoneNumber}
                    onChange={handleChange('phoneNumber')}
                />

                <Input
                    label="Mot de passe"
                    type="password"
                    placeholder="••••••••"
                    icon={<Lock size={16} />}
                    value={form.password}
                    onChange={handleChange('password')}
                    error={passwordError}
                    hint={passwordError ? undefined : passwordHint}
                    required
                />

                {error && (
                    <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">
                        {error}
                    </div>
                )}

                <Button type="submit" size="lg" fullWidth loading={isSubmitting}>
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