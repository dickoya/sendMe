import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, ArrowLeft, RefreshCw, CheckCircle } from 'lucide-react';
import Button from '@/components/ui/Button';
import { authService } from '@/services/authService';

/**
 * Page affichée après l'inscription pour informer l'utilisateur
 * qu'un email de vérification lui a été envoyé.
 * 
 * Route : /verify-email-sent
 * State : { email: string }
 */
function VerificationSent() {
    const { t } = useTranslation('common');
    const location = useLocation();
    const navigate = useNavigate();
    const email = (location.state as { email?: string })?.email || '';

    const [resendStatus, setResendStatus] = useState<'idle' | 'sending' | 'sent' | 'error'>('idle');
    const [resendMessage, setResendMessage] = useState('');

    const handleResend = async () => {
        if (!email) return;
        setResendStatus('sending');
        setResendMessage('');
        try {
            await authService.resendVerification(email);
            setResendStatus('sent');
            setResendMessage(t('emailVerification.resendSuccess', 'Un nouvel email de vérification vous a été envoyé.'));
        } catch (error: any) {
            setResendStatus('error');
            setResendMessage(
                error?.response?.data?.message ||
                t('emailVerification.resendError', 'Erreur lors de l\'envoi. Veuillez réessayer.')
            );
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 px-4">
            <div className="max-w-md w-full bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 text-center">
                {/* Icône email */}
                <div className="mx-auto flex items-center justify-center h-20 w-20 rounded-full bg-primary-100 dark:bg-primary-900/30">
                    <Mail size={40} className="text-primary-600 dark:text-primary-300" />
                </div>

                <h1 className="mt-6 text-2xl font-bold text-gray-900 dark:text-white">
                    {t('emailVerification.sentTitle', 'Vérifiez votre adresse email')}
                </h1>

                <p className="mt-3 text-gray-600 dark:text-gray-300">
                    {t('emailVerification.sentMessage', 'Un email de vérification a été envoyé à')}
                </p>

                <p className="mt-1 font-semibold text-gray-900 dark:text-white break-all">
                    {email || t('emailVerification.yourEmail', 'votre adresse email')}
                </p>

                <div className="mt-6 space-y-3 text-left bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                    <h3 className="text-sm font-semibold text-gray-700 dark:text-gray-200">
                        {t('emailVerification.instructions', 'Prochaines étapes :')}
                    </h3>
                    <ol className="text-sm text-gray-600 dark:text-gray-300 space-y-2 list-decimal list-inside">
                        <li>{t('emailVerification.step1', 'Ouvrez votre boîte de réception')}</li>
                        <li>{t('emailVerification.step2', 'Cliquez sur le lien de vérification dans l\'email')}</li>
                        <li>{t('emailVerification.step3', 'Vous serez automatiquement connecté à votre compte')}</li>
                    </ol>
                </div>

                {/* Message de succès / erreur pour le renvoi */}
                {resendStatus === 'sent' && (
                    <div className="mt-4 flex items-center justify-center gap-2 text-sm text-green-600 dark:text-green-400">
                        <CheckCircle size={16} />
                        <span>{resendMessage}</span>
                    </div>
                )}
                {resendStatus === 'error' && (
                    <div className="mt-4 text-sm text-red-600 dark:text-red-400">
                        {resendMessage}
                    </div>
                )}

                {/* Boutons d'action */}
                <div className="mt-6 space-y-3">
                    <Button
                        onClick={handleResend}
                        fullWidth
                        variant="outline"
                        loading={resendStatus === 'sending'}
                        disabled={resendStatus === 'sending'}
                    >
                        <RefreshCw size={16} className="mr-2" />
                        {t('emailVerification.resendButton', 'Renvoyer l\'email')}
                    </Button>

                    <Link
                        to="/login"
                        className="block w-full px-4 py-2.5 text-sm font-medium text-primary-600 hover:text-primary-700 dark:text-primary-400 dark:hover:text-primary-300 transition-colors"
                    >
                        <ArrowLeft size={16} className="inline mr-1" />
                        {t('emailVerification.backToLogin', 'Retour à la connexion')}
                    </Link>
                </div>

                <p className="mt-6 text-xs text-gray-400 dark:text-gray-500">
                    {t('emailVerification.spamHint', 'Si vous ne trouvez pas l\'email, vérifiez vos spams ou demandez un nouveau lien.')}
                </p>
            </div>
        </div>
    );
}

export default VerificationSent;
