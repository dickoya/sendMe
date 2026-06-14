import { useState, useRef, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Check, ChevronDown, Globe } from 'lucide-react';
import { cn } from '@/lib/utils';
import { LANGUAGES, type LanguageCode } from '@/utils/constants';

/**
 * Sélecteur de langue — menu déroulant avec drapeaux.
 *
 * - Affiche la langue active (drapeau + code)
 * - Liste déroulante des langues disponibles (LANGUAGES)
 * - Changement instantané via i18n.changeLanguage()
 * - La préférence est automatiquement persistée par i18next-browser-languagedetector
 *   (localStorage, clé "sendme_language" — voir i18n/config.ts)
 * - Fermeture au clic à l'extérieur (même pattern que NotificationBell)
 *
 * Pour ajouter une langue : il suffit d'ajouter une entrée dans
 * LANGUAGES (utils/constants.ts) et les fichiers de traduction
 * correspondants (i18n/locales/<code>/) — ce composant n'a rien
 * à changer.
 */
function LanguageSwitcher() {
    const { i18n, t } = useTranslation();
    const [isOpen, setIsOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

    const currentLanguage =
        LANGUAGES.find((lang) => lang.code === i18n.language) ?? LANGUAGES[0];

    useEffect(() => {
        function handleClickOutside(e: MouseEvent) {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                setIsOpen(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleSelect = (code: LanguageCode) => {
        i18n.changeLanguage(code);
        setIsOpen(false);
    };

    return (
        <div className="relative" ref={ref}>
            <button
                onClick={() => setIsOpen((prev) => !prev)}
                className="flex items-center gap-1.5 rounded-lg px-2.5 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100"
                aria-label={t('common:languageSwitcher.label')}
                aria-haspopup="listbox"
                aria-expanded={isOpen}
            >
                <Globe size={16} className="text-gray-400" />
                <span>{currentLanguage.flag}</span>
                <span className="hidden sm:inline">{currentLanguage.code.toUpperCase()}</span>
                <ChevronDown size={14} className={cn('text-gray-400 transition-transform', isOpen && 'rotate-180')} />
            </button>

            {isOpen && (
                <div
                    role="listbox"
                    className="absolute right-0 z-50 mt-2 w-44 rounded-2xl border border-gray-100 bg-white p-1.5 shadow-lg"
                >
                    {LANGUAGES.map((lang) => (
                        <button
                            key={lang.code}
                            role="option"
                            aria-selected={lang.code === i18n.language}
                            onClick={() => handleSelect(lang.code)}
                            className={cn(
                                'flex w-full items-center justify-between gap-2 rounded-xl px-3 py-2 text-left text-sm transition-colors',
                                lang.code === i18n.language
                                    ? 'bg-brand-50 text-brand-700'
                                    : 'text-gray-700 hover:bg-gray-50'
                            )}
                        >
              <span className="flex items-center gap-2">
                <span>{lang.flag}</span>
                <span>{lang.label}</span>
              </span>
                            {lang.code === i18n.language && <Check size={14} />}
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
}

export default LanguageSwitcher;