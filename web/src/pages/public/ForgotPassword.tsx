import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, ArrowLeft, ArrowRight, CheckCircle } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { authService } from '@/services/authService';
import { getErrorMessage } from '@/lib/utils';

/**
 * Page « Mot de passe oublié ».
 * L'utilisateur saisit son adresse email pour recevoir un lien de
 * réinitialisation de mot de passe. Route : /forgot-password
 */
function ForgotPassword() {
    const { t } = useTranslation('auth');
    const [email, setEmail] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [sent, setSent] = useState(false);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);
        try {
            await authService.forgotPassword({ email });
            setSent(true);
        } catch (err) {
            setError(mapForgotError(err, t));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center overflow-x-hidden w-full bg-gray-50 dark:bg-gray-900 px-4">
            <div className="max-w-md w-full bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8">
                {sent ? (
                    <div className="text-center space-y-4">
                        <div className="mx-auto flex items-center justify-center h-20 w-20 rounded-full bg-green-100 dark:bg-green-900/30">
                            <CheckCircle size={40} className="text-green-600 dark:text-green-400" />
                        </div>
                        <h1 className="mt-4 text-2xl font-bold text-gray-900 dark:text-white">
                            {t('forgotPassword.sentTitle')}
                        </h1>
                        <p className="text-gray-600 dark:text-gray-300">
                            {t('forgotPassword.sentMessage')}
                        </p>
                        <div className="mt-6 rounded-lg bg-gray-50 dark:bg-gray-700/50 p-4 text-sm text-left text-gray-600 dark:text-gray-300 space-y-2">
                            <p>{t('forgotPassword.sentStep1')}</p>
                            <p>{t('forgotPassword.sentStep2')}</p>
                            <p>{t('forgotPassword.sentStep3')}</p>
                        </div>
                        <p className="text-sm text-gray-500 dark:text-gray-400">
                            {t('forgotPassword.spamHint')}
                        </p>
                        <Link
                            to="/login"
                            className="inline-flex items-center justify-center w-full px-4 py-2.5 text-sm font-medium text-brand-600 hover:text-brand-700 dark:text-brand-400 dark:hover:text-brand-300 transition-colors"
                        >
                            <ArrowLeft size={16} className="mr-1" />
                            {t('forgotPassword.backToLogin')}
                        </Link>
                    </div>
                ) : (
                    <>
                        <div className="text-center">
                            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-brand-100 dark:bg-brand-900/30">
                                <Mail size={28} className="text-brand-600 dark:text-brand-400" />
                            </div>
                            <h1 className="mt-5 text-2xl font-bold text-gray-900 dark:text-white">
                                {t('forgotPassword.title')}
                            </h1>
                            <p className="mt-2 text-gray-600 dark:text-gray-300">
                                {t('forgotPassword.subtitle')}
                            </p>
                        </div>
                        <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
                            <Input
                                label={t('login.email')}
                                type="email"
                                placeholder={t('login.emailPlaceholder')}
                                icon={<Mail size={16} />}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                autoComplete="email"
                                autoFocus
                            />
                            {error && (
                                <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600 border border-red-100 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800">
                                    {error}
                                </div>
                            )}
                            <Button type="submit" fullWidth loading={isSubmitting} size="lg">
                                {t('forgotPassword.submit')}
                                <ArrowRight size={18} />
                            </Button>
                        </form>
                        <Link
                            to="/login"
                            className="mt-6 flex items-center justify-center gap-1 text-sm font-medium text-gray-500 hover:text-brand-600 transition-colors dark:text-gray-300 dark:hover:text-brand-400"
                        >
                            <ArrowLeft size={16} />
                            {t('forgotPassword.backToLogin')}
                        </Link>
                    </>
                )}
            </div>
        </div>
    );
}

function mapForgotError(err: unknown, t: (key: string) => string): string {
    const raw = getErrorMessage(err);
    if (/vérifié|verifie|verif/i.test(raw)) return t('forgotPassword.errors.unverifiedEmail');
    return raw;
}

export default ForgotPassword;

