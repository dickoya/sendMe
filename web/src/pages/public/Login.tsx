import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, Lock, ArrowRight, Zap, Shield, TrendingDown } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import LanguageSwitcher from '@/components/layout/LanguageSwitcher';
import ThemeToggle from '@/components/layout/ThemeToggle';
import { useAuth } from '@/store/auth/authContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Page de connexion — design split moderne (branding à gauche, formulaire à droite).
 * Navigation et footer sur toute la largeur de la page.
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

    const benefits = [
        { icon: Zap, text: 'Transferts instantanés vers l\'Afrique' },
        { icon: Shield, text: 'Sécurité bancaire de niveau enterprise' },
        { icon: TrendingDown, text: 'Frais transparents, taux compétitifs' },
    ];

    return (
        <div className="flex min-h-screen flex-col">
            {/* ── Barre de navigation (full width) ─────────────────────────── */}
            <nav className="fixed top-0 left-0 right-0 z-50 border-b border-white/10 bg-white/80 backdrop-blur-lg dark:bg-gray-900/80 dark:border-gray-800/50">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <Link to="/" className="flex items-center gap-2">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                            S
                        </div>
                        <span className="text-lg font-bold text-gray-900 dark:text-gray-100">SendMe</span>
                    </Link>
                    <div className="flex items-center gap-6">
                        <Link to="/" className="hidden text-sm font-medium text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400 sm:block">
                            Accueil
                        </Link>
                        <Link to="/#features" className="hidden text-sm font-medium text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400 sm:block">
                            Fonctionnalités
                        </Link>
                        <ThemeToggle />
                        <LanguageSwitcher />
                        <Link to="/register">
                            <Button size="sm">{t('register.title')}</Button>
                        </Link>
                    </div>
                </div>
            </nav>

            {/* ── Contenu principal (sous la navbar) ───────────────────────── */}
            <div className="flex flex-1 pt-16">
                {/* Panneau gauche : Branding */}
                <div className="relative hidden flex-1 flex-col justify-between bg-gradient-to-br from-brand-900 via-brand-700 to-brand-500 p-12 lg:flex">
                    {/* Formes décoratives */}
                    <div className="absolute -top-40 -right-40 h-96 w-96 rounded-full bg-white/5 blur-3xl" />
                    <div className="absolute -bottom-20 -left-20 h-72 w-72 rounded-full bg-accent-400/10 blur-3xl" />

                    {/* Logo (espace réservé pour aligner avec la navbar) */}
                    <div className="relative" />

                    {/* Contenu central */}
                    <div className="relative space-y-8">
                        <div>
                            <h1 className="text-4xl font-bold leading-tight text-white sm:text-5xl">
                                Bienvenue sur<br />SendMe
                            </h1>
                            <p className="mt-4 text-lg text-white/70 max-w-md">
                                La façon la plus simple et sécurisée d'envoyer de l'argent vers l'Afrique.
                            </p>
                        </div>

                        <div className="space-y-5">
                            {benefits.map((item) => (
                                <div key={item.text} className="flex items-center gap-4">
                                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/10">
                                        <item.icon size={20} className="text-white" />
                                    </div>
                                    <span className="text-white/80">{item.text}</span>
                                </div>
                            ))}
                        </div>

                        {/* Témoignage */}
                        <div className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm max-w-sm">
                            <p className="text-sm leading-relaxed text-white/70 italic">
                                "SendMe a complètement changé ma façon d'envoyer de l'argent à ma famille. C'est rapide, fiable et les taux sont imbattables."
                            </p>
                            <div className="mt-4 flex items-center gap-3">
                                <div className="h-10 w-10 rounded-full bg-white/20 flex items-center justify-center text-white font-bold">
                                    M
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-white">Mamadou D.</p>
                                    <p className="text-xs text-white/50">Utilisateur depuis 2024</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <p className="relative text-sm text-white/40">
                        © {new Date().getFullYear()} SendMe. Tous droits réservés.
                    </p>
                </div>

                {/* Panneau droit : Formulaire */}
                <div className="flex flex-1 flex-col bg-white dark:bg-gray-900">
                    <div className="flex flex-1 items-center justify-center px-6 py-12 sm:px-10">
                        <div className="w-full max-w-sm">
                            <div className="mb-8">
                                <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">{t('login.title')}</h1>
                                <p className="mt-2 text-gray-500 dark:text-gray-400">{t('login.subtitle')}</p>
                            </div>

                            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
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
                                    <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600 border border-red-100 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800">
                                        {error}
                                    </div>
                                )}

                                <Button type="submit" fullWidth loading={isSubmitting} size="lg">
                                    {t('login.submit')}
                                    <ArrowRight size={18} />
                                </Button>
                            </form>

                            <p className="mt-8 text-center text-sm text-gray-500 dark:text-gray-400">
                                {t('login.noAccount')}{' '}
                                <Link to="/register" className="font-semibold text-brand-600 hover:text-brand-700 dark:text-brand-400 dark:hover:text-brand-300">
                                    {t('login.registerLink')}
                                </Link>
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* ── Footer (full width) ──────────────────────────────────────── */}
            <footer className="border-t border-gray-100 bg-white px-6 py-6 sm:px-10 dark:border-gray-800 dark:bg-gray-900">
                <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 sm:flex-row">
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                        © {new Date().getFullYear()} SendMe. Tous droits réservés.
                    </p>
                    <div className="flex items-center gap-6">
                        <a href="#" className="text-sm text-gray-500 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">Confidentialité</a>
                        <a href="#" className="text-sm text-gray-500 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">CGU</a>
                        <a href="mailto:support@sendme.com" className="text-sm text-gray-500 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">Aide</a>
                    </div>
                </div>
            </footer>
        </div>
    );
}

function mapLoginError(err: unknown, t: (key: string) => string): string {
    const raw = getErrorMessage(err);
    if (/suspendu|disabled|désactivé/i.test(raw)) return t('login.errors.accountDisabled');
    if (/trop de tentatives|too many attempts/i.test(raw)) return t('login.errors.tooManyAttempts');
    if (/identifiants|invalid credentials|incorrect/i.test(raw)) return t('login.errors.invalidCredentials');
    return raw;
}

export default Login;
