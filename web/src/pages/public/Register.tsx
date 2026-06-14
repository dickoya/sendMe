import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, Lock, User, Phone } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import { useAuth } from '@/store/auth/authContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Page d'inscription.
 *
 * Migration i18n (Phase 5) : namespace "auth" (clés auth.register.*).
 * Validation client (mot de passe trop court, champs requis) traduite
 * directement ; erreurs serveur mappées via mapRegisterError().
 */
function Register() {
    const { t } = useTranslation('auth');
    const navigate = useNavigate();
    const { register } = useAuth();

    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);

        if (password.length < 8) {
            setError(t('register.errors.passwordTooShort'));
            return;
        }

        setIsSubmitting(true);
        try {
            await register({ firstName, lastName, email, password, phoneNumber });
            navigate('/dashboard');
        } catch (err) {
            setError(mapRegisterError(err, t));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Card title={t('register.title')} className="w-full max-w-sm">
            <p className="-mt-2 mb-4 text-sm text-gray-500">{t('register.subtitle')}</p>

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="grid grid-cols-2 gap-3">
                    <Input
                        label={t('register.firstName')}
                        placeholder={t('register.firstNamePlaceholder')}
                        icon={<User size={16} />}
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        required
                        autoComplete="given-name"
                    />
                    <Input
                        label={t('register.lastName')}
                        placeholder={t('register.lastNamePlaceholder')}
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        required
                        autoComplete="family-name"
                    />
                </div>

                <Input
                    label={t('register.email')}
                    type="email"
                    placeholder={t('register.emailPlaceholder')}
                    icon={<Mail size={16} />}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                />

                <Input
                    label={t('register.password')}
                    type="password"
                    placeholder={t('register.passwordPlaceholder')}
                    icon={<Lock size={16} />}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    hint={t('register.passwordHint')}
                    required
                    autoComplete="new-password"
                />

                <Input
                    label={t('register.phone')}
                    type="tel"
                    placeholder={t('register.phonePlaceholder')}
                    icon={<Phone size={16} />}
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    autoComplete="tel"
                />

                {error && (
                    <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">
                        {error}
                    </div>
                )}

                <Button type="submit" fullWidth loading={isSubmitting}>
                    {t('register.submit')}
                </Button>
            </form>

            <p className="mt-4 text-center text-sm text-gray-500">
                {t('register.hasAccount')}{' '}
                <Link to="/login" className="font-medium text-brand-600 hover:underline">
                    {t('register.loginLink')}
                </Link>
            </p>
        </Card>
    );
}

/**
 * Mappe une erreur API d'inscription vers un message traduit.
 */
function mapRegisterError(err: unknown, t: (key: string) => string): string {
    const raw = getErrorMessage(err);

    if (/email.*(utilisé|use|exist)|already.*(use|exist)/i.test(raw)) {
        return t('register.errors.emailTaken');
    }
    if (/requis|required|obligatoire/i.test(raw)) {
        return t('register.errors.requiredField');
    }
    return raw;
}

export default Register;