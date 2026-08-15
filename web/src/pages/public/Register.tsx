import { useState, useEffect, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, Lock, User, Phone, Gift, ArrowRight, Zap, Shield, TrendingDown, CheckCircle, XCircle, Loader2 } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import LanguageSwitcher from '@/components/layout/LanguageSwitcher';
import ThemeToggle from '@/components/layout/ThemeToggle';
import { useAuth } from '@/store/auth/authContext';
import { getErrorMessage } from '@/lib/utils';
import { referralService } from '@/services/referralService';

/**
 * Page d'inscription — design split moderne (branding à gauche, formulaire à droite).
 * Navigation et footer sur toute la largeur de la page.
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
    const [referralCode, setReferralCode] = useState('');
    const [referralCodeStatus, setReferralCodeStatus] = useState<'idle' | 'checking' | 'valid' | 'invalid'>('idle');
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Vérification en temps réel du code de parrainage avec debounce
    useEffect(() => {
        if (!referralCode || referralCode.length < 5) {
            setReferralCodeStatus('idle');
            return;
        }

        const timer = setTimeout(async () => {
            setReferralCodeStatus('checking');
            try {
                const isValid = await referralService.verifyCode(referralCode);
                setReferralCodeStatus(isValid ? 'valid' : 'invalid');
            } catch {
                setReferralCodeStatus('idle');
            }
        }, 400); // debounce 400ms

        return () => clearTimeout(timer);
    }, [referralCode]);

    const handleSubmit = async (e: FormEvent) => {

        e.preventDefault();
        setError(null);

        if (password.length < 8) {
            setError(t('register.errors.passwordTooShort'));
            return;
        }

        setIsSubmitting(true);
        try {
            await register({ firstName, lastName, email, password, phoneNumber, referralCode: referralCode || undefined });
            // Rediriger vers la page de confirmation de vérification d'email
            navigate('/verify-email-sent', { state: { email } });
        } catch (err) {
            setError(mapRegisterError(err, t));
        } finally {
            setIsSubmitting(false);
        }
    };

    const benefits = [
        { icon: Zap, text: 'Inscription gratuite en moins d\'une minute' },
        { icon: Shield, text: 'Vos données sont chiffrées et protégées' },
        { icon: TrendingDown, text: 'Profitez des meilleurs taux du marché' },
    ];

    return (
        <div className="flex min-h-screen flex-col">
            {/* Barre de navigation (full width) */}
            <nav className="fixed top-0 left-0 right-0 z-50 border-b border-white/10 bg-white/80 backdrop-blur-lg dark:bg-[#0D0D0D]/80 dark:border-[#222222]/50">

                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <Link to="/" className="flex items-center gap-2">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                            S
                        </div>
                        <span className="text-lg font-bold text-gray-900 dark:text-gray-100">SendMe</span>
                    </Link>
                    <div className="flex items-center gap-6">
                        <Link to="/" className="hidden text-sm font-medium text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-300 dark:hover:text-brand-400 sm:block">
                            Accueil
                        </Link>
                        <Link to="/#features" className="hidden text-sm font-medium text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-300 dark:hover:text-brand-400 sm:block">
                            Fonctionnalités
                        </Link>
                        <ThemeToggle />
                        <LanguageSwitcher />
                        <Link to="/login">
                            <Button size="sm">{t('login.title')}</Button>
                        </Link>
                    </div>
                </div>
            </nav>

            {/* Contenu principal (sous la navbar) */}
            <div className="flex flex-1 pt-16">
                {/* Panneau gauche : Branding */}
                <div className="relative hidden flex-1 flex-col justify-between bg-gradient-to-br from-brand-900 via-brand-700 to-brand-500 p-12 lg:flex">
                    {/* Formes décoratives */}
                    <div className="absolute -top-40 -right-40 h-96 w-96 rounded-full bg-white/5 blur-3xl" />
                    <div className="absolute -bottom-20 -left-20 h-72 w-72 rounded-full bg-accent-400/10 blur-3xl" />

                    <div className="relative" />

                    {/* Contenu central */}
                    <div className="relative space-y-8">
                        <div>
                            <h1 className="text-4xl font-bold leading-tight text-white sm:text-5xl">
                                Rejoignez<br />SendMe
                            </h1>
                            <p className="mt-4 text-lg text-white/70 max-w-md">
                                Créez votre compte gratuitement et commencez à transférer en toute simplicité.
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

                        {/* Publicité */}
                        <div className="rounded-2xl border border-white/10 bg-white/5 p-2 backdrop-blur-sm max-w-sm">
                            <img
                                src="/publicitesendme.jpeg"
                                alt="SendMe — publicité"
                                className="h-auto w-full rounded-xl object-cover"
                            />
                        </div>
                    </div>

                    <p className="relative text-sm text-white/40">
                        © {new Date().getFullYear()} SendMe. Tous droits réservés.
                    </p>
                </div>

                {/* Panneau droit : Formulaire */}
                <div className="flex flex-1 flex-col bg-white dark:bg-[#141414]">

                    <div className="flex flex-1 items-center justify-center px-6 py-12 sm:px-10">
                        <div className="w-full max-w-sm">
                            <div className="mb-8">
                                <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">{t('register.title')}</h1>
                                <p className="mt-2 text-gray-500 dark:text-gray-300">{t('register.subtitle')}</p>
                            </div>

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

                                <div className="relative">
                                    <Input
                                        label={t('referral:registerReferralLabel')}
                                        placeholder={t('referral:registerReferralPlaceholder')}
                                        icon={<Gift size={16} />}
                                        value={referralCode}
                                        onChange={(e) => setReferralCode(e.target.value.toUpperCase())}
                                        hint={
                                            referralCodeStatus === 'idle'
                                                ? t('referral:registerReferralHelp')
                                                : referralCodeStatus === 'checking'
                                                    ? t('referral:codeChecking')
                                                    : referralCodeStatus === 'valid'
                                                        ? t('referral:codeValid')
                                                        : t('referral:codeInvalid')
                                        }
                                        error={
                                            referralCodeStatus === 'invalid'
                                                ? t('referral:codeInvalid')
                                                : undefined
                                        }
                                        autoComplete="off"
                                    />
                                    {/* Indicateur visuel à droite du champ */}
                                    {referralCode.length >= 5 && (
                                        <span className="absolute right-3 top-[38px]">
                                            {referralCodeStatus === 'checking' && (
                                                <Loader2 size={18} className="animate-spin text-gray-400" />
                                            )}
                                            {referralCodeStatus === 'valid' && (
                                                <CheckCircle size={18} className="text-success-500" />
                                            )}
                                            {referralCodeStatus === 'invalid' && (
                                                <XCircle size={18} className="text-danger-500" />
                                            )}
                                        </span>
                                    )}
                                </div>

                                {error && (
                                    <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-600 border border-red-100 dark:bg-red-900/30 dark:text-red-400 dark:border-red-800">
                                        {error}
                                    </div>
                                )}

                                <Button type="submit" fullWidth loading={isSubmitting} size="lg" className="mt-2">
                                    {t('register.submit')}
                                    <ArrowRight size={18} />
                                </Button>
                            </form>

                            <p className="mt-8 text-center text-sm text-gray-500 dark:text-gray-300">
                                {t('register.hasAccount')}{' '}
                                <Link to="/login" className="font-semibold text-brand-600 hover:text-brand-700 dark:text-brand-400 dark:hover:text-brand-300">
                                    {t('register.loginLink')}
                                </Link>
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            {/* Footer (full width) */}
            <footer className="border-t border-gray-100 bg-white px-6 py-6 sm:px-10 dark:border-[#222222] dark:bg-[#0D0D0D]">

                <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 sm:flex-row">
                    <p className="text-sm text-gray-500 dark:text-gray-300">
                        © {new Date().getFullYear()} SendMe. Tous droits réservés.
                    </p>
                    <div className="flex items-center gap-6">
                        <Link to="/legal" className="text-sm text-gray-500 hover:text-brand-600 transition-colors dark:text-gray-300 dark:hover:text-brand-400">Confidentialité</Link>
                        <Link to="/legal" className="text-sm text-gray-500 hover:text-brand-600 transition-colors dark:text-gray-300 dark:hover:text-brand-400">CGU</Link>
                        <a href="mailto:support@sendme.com" className="text-sm text-gray-500 hover:text-brand-600 transition-colors dark:text-gray-300 dark:hover:text-brand-400">Aide</a>
                    </div>
                </div>
            </footer>
        </div>
    );
}

function mapRegisterError(err: unknown, t: (key: string) => string): string {
    const raw = getErrorMessage(err);
    if (/email.*(utilisé|use|exist)|already.*(use|exist)/i.test(raw)) return t('register.errors.emailTaken');
    if (/requis|required|obligatoire/i.test(raw)) return t('register.errors.requiredField');
    return raw;
}

export default Register;
