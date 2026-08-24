import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Cookie } from 'lucide-react';
import { cn } from '@/lib/utils';

/**
 * Clé localStorage qui mémorise le choix de l'utilisateur
 * (voir la convention des clés "sendme_*" dans utils/constants.ts).
 */
const COOKIE_CONSENT_KEY = 'sendme_cookie_consent';

/**
 * Bannière de consentement aux cookies.
 *
 * Elle apparaît en bas de l'écran tant que l'utilisateur n'a pas fait de choix
 * (accepter ou refuser). Le choix est ensuite mémorisé dans le localStorage
 * pour ne plus afficher la bannière lors des visites suivantes.
 */
function CookieConsent() {
    const { t } = useTranslation('common');

    // Lecture synchrone au montage : pas de bannière si un choix existe déjà.
    const [visible, setVisible] = useState<boolean>(
        () => !localStorage.getItem(COOKIE_CONSENT_KEY)
    );

    const choose = (value: 'accepted' | 'declined') => {
        localStorage.setItem(COOKIE_CONSENT_KEY, value);
        setVisible(false);
    };

    if (!visible) {
        return null;
    }

    return (
        <div
            role="dialog"
            aria-label={t('cookieConsent.label')}
            className="fixed inset-x-0 bottom-0 z-[60] border-t border-gray-100 bg-white/95 p-4 shadow-[0_-4px_20px_rgba(0,0,0,0.06)] backdrop-blur-md dark:border-[#222222] dark:bg-[#0D0D0D]/95"
        >
            <div className="mx-auto flex w-full max-w-7xl flex-col items-start gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex items-start gap-3 sm:items-center">
                    <Cookie size={20} className="mt-0.5 shrink-0 text-brand-600 dark:text-brand-400 sm:mt-0" />
                    <div>
                        <p className="text-sm leading-relaxed text-gray-600 dark:text-[#8B8FA3]">
                            {t('cookieConsent.message')}
                        </p>
                        <Link
                            to="/legal"
                            className="mt-1 inline-block text-xs font-medium text-brand-600 hover:underline dark:text-brand-400"
                        >
                            {t('cookieConsent.policyLink')}
                        </Link>
                    </div>
                </div>

                <div className="flex shrink-0 items-center gap-2">
                    <button
                        type="button"
                        onClick={() => choose('declined')}
                        className={cn(
                            'rounded-xl border border-gray-200 px-4 py-2 text-sm font-medium text-gray-700',
                            'transition-all hover:bg-gray-50 dark:border-[#333333] dark:text-[#C0C4D0] dark:hover:bg-[#1A1A1A]'
                        )}
                    >
                        {t('cookieConsent.decline')}
                    </button>
                    <button
                        type="button"
                        onClick={() => choose('accepted')}
                        className={cn(
                            'rounded-xl bg-brand-600 px-4 py-2 text-sm font-medium text-white',
                            'transition-all hover:bg-brand-700 dark:bg-brand-500 dark:hover:bg-brand-600'
                        )}
                    >
                        {t('cookieConsent.accept')}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default CookieConsent;
