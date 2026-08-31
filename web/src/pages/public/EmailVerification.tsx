import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { authService } from '@/services/authService';
import Spinner from '@/components/ui/Spinner';
import Alert from '@/components/ui/Alert';

type VerificationStatus = 'loading' | 'success' | 'error';

/**
 * Page de vérification d'email.
 * 
 * L'utilisateur arrive ici après avoir cliqué sur le lien reçu par email.
 * Le token est extrait de l'URL et envoyé à l'API pour validation.
 * 
 * Route : /verify-email?token=xxx
 */
function EmailVerification() {
    const { t } = useTranslation('common');
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const token = searchParams.get('token');

    const [status, setStatus] = useState<VerificationStatus>('loading');
    const [errorMessage, setErrorMessage] = useState('');

    useEffect(() => {
        if (!token) {
            setStatus('error');
            setErrorMessage(t('emailVerification.missingToken', 'Lien de vérification invalide.'));
            return;
        }

        const verifyEmail = async () => {
            try {
                const response = await authService.verifyEmail(token);
                if (response.success) {
                    setStatus('success');
                    // Stocker les tokens JWT reçus
                    if (response.data) {
                        localStorage.setItem('accessToken', response.data.accessToken);
                        localStorage.setItem('refreshToken', response.data.refreshToken);
                    }
                    // Rediriger vers le dashboard après 3 secondes
                    setTimeout(() => {
                        navigate('/dashboard', { replace: true });
                    }, 3000);
                } else {
                    setStatus('error');
                    setErrorMessage(response.message || t('emailVerification.error', 'Erreur lors de la vérification.'));
                }
            } catch (error: any) {
                setStatus('error');
                setErrorMessage(
                    error?.response?.data?.message ||
                    t('emailVerification.error', 'Erreur lors de la vérification de votre email.')
                );
            }
        };

        verifyEmail();
    }, [token, navigate, t]);

    return (
        <div className="min-h-screen flex items-center justify-center overflow-x-hidden w-full bg-gray-50 dark:bg-gray-900 px-4">
            <div className="max-w-md w-full bg-white dark:bg-gray-800 rounded-xl shadow-lg p-8 text-center">
                {status === 'loading' && (
                    <div className="space-y-4">
                        <Spinner size="lg" />
                        <p className="text-gray-600 dark:text-gray-300">
                            {t('emailVerification.verifying', 'Vérification de votre email en cours...')}
                        </p>
                    </div>
                )}

                {status === 'success' && (
                    <div className="space-y-4">
                        <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100 dark:bg-green-900">
                            <svg className="h-8 w-8 text-green-600 dark:text-green-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                            </svg>
                        </div>
                        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                            {t('emailVerification.successTitle', 'Email vérifié !')}
                        </h2>
                        <p className="text-gray-600 dark:text-gray-300">
                            {t('emailVerification.successMessage', 'Votre adresse email a été vérifiée avec succès. Vous allez être redirigé vers votre tableau de bord.')}
                        </p>
                        <Link
                            to="/dashboard"
                            className="inline-block mt-4 px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
                        >
                            {t('emailVerification.goToDashboard', 'Aller au tableau de bord')}
                        </Link>
                    </div>
                )}

                {status === 'error' && (
                    <div className="space-y-4">
                        <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-red-100 dark:bg-red-900">
                            <svg className="h-8 w-8 text-red-600 dark:text-red-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </div>
                        <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                            {t('emailVerification.errorTitle', 'Échec de la vérification')}
                        </h2>
                        <Alert type="error" message={errorMessage} />
                        <div className="space-y-2">
                            <Link
                                to="/login"
                                className="block px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
                            >
                                {t('emailVerification.goToLogin', 'Aller à la connexion')}
                            </Link>
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                                {t('emailVerification.resendHint', 'Vous pouvez demander un nouveau lien de vérification depuis la page de connexion.')}
                            </p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default EmailVerification;
