import { useState, type FormEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Lock, ArrowLeft, ArrowRight, CheckCircle } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import Alert from '@/components/ui/Alert';
import { authService } from '@/services/authService';
import { getErrorMessage } from '@/lib/utils';

/**
 * Page de réinitialisation du mot de passe.
 *
 * L'utilisateur arrive ici après avoir cliqué sur le lien reçu par email.
 * Le token et l'email sont extraits de l'URL ; il confirme son adresse
 * email (affichée en lecture seule) et définit son nouveau mot de passe.
 *
 * Route : /reset-password?token=xxx&email=yyy
 */
function ResetPassword() {
    const { t } = useTranslation('auth');
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token') ?? '';
    const email = searchParams.get('email') ?? '';

    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [done, setDone] = useState(false);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);

        if (password.length < 8) {
            setError(t('register.errors.passwordTooShort'));
            return;
        }
        if (password !== confirmPassword) {
            setError(t('resetPassword.errors.mismatch'));
            return;
        }

        setIsSubmitting(true);
        try {
            await authService.resetPassword({ token, email, newPassword: password, confirmPassword });
            setDone(true);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center overflow-x-hidden w-full bg-gray-50 dark:bg-gray-900 px-4">
            <div className="max-w-md w-full bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8">
                {done ? (
                    <div className="text-center space-y-4">
                        <div className="mx-auto flex items-center justify-center h-20 w-20 rounded-full bg-green-100 dark:bg-green-900/30">
                            <CheckCircle size={40} className="text-green-600 dark:text-green-400" />
                        </div>
                        <h1 className="mt-4 text-2xl font-bold text-gray-900 dark:text-white">
                            {t('resetPassword.successTitle')}
                        </h1>
                        <p className="text-gray-600 dark:text-gray-300">
                            {t('resetPassword.successMessage')}
                        </p>
                        <Link
                            to="/login"
                            className="inline-flex items-center justify-center w-full px-4 py-3 mt-4 bg-brand-600 hover:bg-brand-700 text-white rounded-lg font-semibold transition-colors"
                        >
                            {t('resetPassword.goToLogin')}
                            <ArrowRight size={18} className="ml-2" />
                        </Link>
                    </div>
                ) : (
                    <>
                        <div className="text-center">
                            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-brand-100 dark:bg-brand-900/30">
                                <Lock size={28} className="text-brand-600 dark:text-brand-400" />
                            </div>
                            <h1 className="mt-5 text-2xl font-bold text-gray-900 dark:text-white">
                                {t('resetPassword.title')}
                            </h1>
                            <p className="mt-2 text-gray-600 dark:text-gray-300">
                                {t('resetPassword.subtitle')}
                            </p>
                        </div>

                        {!token ? (
                            <div className="mt-6">
                                <Alert type="error" message={t('resetPassword.errors.missingToken')} />
                                <Link
                                    to="/forgot-password"
                                    className="mt-4 flex items-center justify-center gap-1 text-sm font-medium text-brand-600 hover:text-brand-700 dark:text-brand-400 transition-colors"
                                >
                                    <ArrowLeft size={16} />
                                    {t('resetPassword.requestNewLink')}
                                </Link>
                            </div>
                        ) : (
                            <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
                                <Input
                                    label={t('resetPassword.email')}
                                    type="email"
                                    value={email}
                                    readOnly
                                    disabled
                                />
                                <Input
                                    label={t('resetPassword.newPassword')}
                                    type="password"
                                    placeholder={t('resetPassword.passwordPlaceholder')}
                                    icon={<Lock size={16} />}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    minLength={8}
                                    autoComplete="new-password"
                                />
                                <p className="-mt-2 text-xs text-gray-500 dark:text-gray-400">
                                    {t('register.passwordHint')}
                                </p>
                                <Input
                                    label={t('resetPassword.confirmPassword')}
                                    type="password"
                                    placeholder={t('resetPassword.confirmPlaceholder')}
                                    icon={<Lock size={16} />}
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    minLength={8}
                                    autoComplete="new-password"
                                />
                                {error && (
                                    <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600 border border-red-100 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800">
                                        {error}
                                    </div>
                                )}
                                <Button type="submit" fullWidth loading={isSubmitting} size="lg">
                                    {t('resetPassword.submit')}
                                    <ArrowRight size={18} />
                                </Button>
                            </form>
                        )}

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

export default ResetPassword;

