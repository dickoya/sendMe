import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, Lock } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import Card from '@/components/ui/Card';
import { useAuth } from '@/store/auth/authContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Page de connexion.
 *
 * Migration i18n (Phase 5) : namespace "auth" (clés auth.login.*).
 * Les messages d'erreur backend connus sont mappés vers des clés
 * traduites via mapLoginError() — voir aussi Phase 12 pour la
 * généralisation de ce pattern à getErrorMessage().
 */
function Login() {
    const { t } = useTranslation('auth');
    const navigate = useNavigate();
    const { login } = useAuth();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        try {
            await login({email, password});
            navigate('/dashboard');
        } catch (err) {
            setError(mapLoginError(err, t));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Card title={t('login.title')} className="w-full max-w-sm">
            <p className="-mt-2 mb-4 text-sm text-gray-500">{t('login.subtitle')}</p>

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <Input
                    label={t('login.email')}
                    type="email"
                    placeholder={t('login.emailPlaceholder')}
                    icon={<Mail size={16} />}
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoComplete="email"
                />
                <Input
                    label={t('login.password')}
                    type="password"
                    placeholder={t('login.passwordPlaceholder')}
                    icon={<Lock size={16} />}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="current-password"
                />

                {error && (
                    <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">
                        {error}
                    </div>
                )}

                <Button type="submit" fullWidth loading={isSubmitting}>
                    {t('login.submit')}
                </Button>
            </form>

            <p className="mt-4 text-center text-sm text-gray-500">
                {t('login.noAccount')}{' '}
                <Link to="/register" className="font-medium text-brand-600 hover:underline">
                    {t('login.registerLink')}
                </Link>
            </p>
        </Card>
    );
}

/**
 * Mappe une erreur API de connexion vers un message traduit.
 *
 * Le backend renvoie des messages en français en dur — on les
 * reconnaît par mot-clé pour leur substituer une clé i18n. Tout
 * message non reconnu retombe sur l'erreur générique traduite
 * (common.errors.generic, gérée par getErrorMessage en Phase 12).
 */
function mapLoginError(err: unknown, t: (key: string) => string): string {
    const raw = getErrorMessage(err);

    if (/suspendu|disabled|désactivé/i.test(raw)) {
        return t('login.errors.accountDisabled');
    }
    if (/trop de tentatives|too many attempts/i.test(raw)) {
        return t('login.errors.tooManyAttempts');
    }
    if (/identifiants|invalid credentials|incorrect/i.test(raw)) {
        return t('login.errors.invalidCredentials');
    }
    return raw;
}

export default Login;