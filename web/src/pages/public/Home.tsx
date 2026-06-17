import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
    ArrowRight,
    Zap,
    Shield,
    TrendingDown,
    Smartphone,
    ChevronRight,
    Globe,
    Users,
    Send,
    CheckCircle,
    Star,
} from 'lucide-react';
import Button from '@/components/ui/Button';
import CurrencySelect from '@/components/ui/CurrencySelect';
import type { CurrencyOption } from '@/components/ui/CurrencySelect';
import LanguageSwitcher from '@/components/layout/LanguageSwitcher';
import ThemeToggle from '@/components/layout/ThemeToggle';
import { useAuth } from '@/store/auth/authContext';
import { transferService } from '@/services/transferService';
import { formatCurrency } from '@/utils/formatCurrency';
import type { ChannelType, SimulateResponse } from '@/types';

/**
 * Page d'accueil publique de SendMe.
 * Présente le service, simule un transfert, et invite à s'inscrire/se connecter.
 * Design moderne et responsive inspiré de YMO/Wise/Revolut.
 */
function Home() {
    const { t } = useTranslation('home');
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();

    // ── Options de devises ──────────────────────────────────────────────
    const currencyFromOptions: CurrencyOption[] = [
        { code: 'EUR', label: 'Euro', flag: 'eu' },
        { code: 'USD', label: 'US Dollar', flag: 'us' },
    ];

    const currencyToOptions: CurrencyOption[] = [
        { code: 'XOF', label: 'Franc CFA (UEMOA)', flag: 'xof' },
        { code: 'GNF', label: 'Franc Guinéen', flag: 'gn' },
    ];

    // ── Simulateur ──────────────────────────────────────────────────────
    const [amount, setAmount] = useState<number>(100);
    const [currencyFrom, setCurrencyFrom] = useState<string>('EUR');
    const [currencyTo, setCurrencyTo] = useState<string>('XOF');
    const [simulation, setSimulation] = useState<SimulateResponse | null>(null);
    const [simulating, setSimulating] = useState(false);
    const [simError, setSimError] = useState<string | null>(null);

    const simulate = useCallback(async (value: number, fromCurrency: string, toCurrency: string) => {
        if (value <= 0) {
            setSimulation(null);
            return;
        }
        setSimulating(true);
        setSimError(null);
        try {
            const result = await transferService.simulate({
                amount: value,
                currencyFrom: fromCurrency,
                currencyTo: toCurrency,
                channel: 'ORANGE_MONEY' as ChannelType,
            });
            setSimulation(result);
        } catch {
            setSimError(t('simulator.loading'));
        } finally {
            setSimulating(false);
        }
    }, [t]);

    useEffect(() => {
        const debounce = setTimeout(() => {
            simulate(amount, currencyFrom, currencyTo);
        }, 500);
        return () => clearTimeout(debounce);
    }, [amount, currencyFrom, currencyTo, simulate]);

    const handleGetStarted = () => {
        if (isAuthenticated) {
            navigate('/dashboard');
        } else {
            navigate('/register');
        }
    };

    // ── Statistiques (données statiques pour la démo) ───────────────────
    const stats = [
        { value: '50K+', label: t('stats.transactions'), icon: Send },
        { value: '10K+', label: t('stats.users'), icon: Users },
        { value: '8', label: t('stats.countries'), icon: Globe },
        { value: '< 30s', label: t('stats.speed'), icon: Zap },
    ];

    // ── Fonctionnalités ─────────────────────────────────────────────────
    const features = [
        {
            icon: Zap,
            title: t('features.speed.title'),
            description: t('features.speed.description'),
            color: 'text-yellow-500 bg-yellow-50 dark:bg-yellow-900/30',
        },
        {
            icon: Shield,
            title: t('features.security.title'),
            description: t('features.security.description'),
            color: 'text-green-500 bg-green-50 dark:bg-green-900/30',
        },
        {
            icon: TrendingDown,
            title: t('features.rates.title'),
            description: t('features.rates.description'),
            color: 'text-blue-500 bg-blue-50 dark:bg-blue-900/30',
        },
        {
            icon: Smartphone,
            title: t('features.channels.title'),
            description: t('features.channels.description'),
            color: 'text-purple-500 bg-purple-50 dark:bg-purple-900/30',
        },
    ];

    // ── Étapes ──────────────────────────────────────────────────────────
    const steps = [
        {
            number: '01',
            icon: CheckCircle,
            title: t('howItWorks.step1.title'),
            description: t('howItWorks.step1.description'),
        },
        {
            number: '02',
            icon: Users,
            title: t('howItWorks.step2.title'),
            description: t('howItWorks.step2.description'),
        },
        {
            number: '03',
            icon: Send,
            title: t('howItWorks.step3.title'),
            description: t('howItWorks.step3.description'),
        },
    ];

    return (
        <div className="flex min-h-screen flex-col">
            {/* ── Navigation ──────────────────────────────────────────────── */}
            <nav className="fixed top-0 left-0 right-0 z-50 border-b border-white/10 bg-white/80 backdrop-blur-lg dark:bg-gray-900/80 dark:border-gray-800/50">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <Link to="/" className="flex items-center gap-2">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                            S
                        </div>
                        <span className="text-lg font-bold text-gray-900 dark:text-gray-100">SendMe</span>
                    </Link>

                    {/* Navigation desktop */}
                    <div className="hidden items-center gap-6 md:flex">
                        <a href="#features" className="text-sm font-medium text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">
                            {t('nav.features')}
                        </a>
                        <a href="#how-it-works" className="text-sm font-medium text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">
                            {t('nav.howItWorks')}
                        </a>
                        <ThemeToggle />
                        <LanguageSwitcher />
                        <Link to="/login" className="text-sm font-medium text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">
                            {t('nav.login')}
                        </Link>
                        <Link to="/register">
                            <Button size="sm">{t('nav.register')}</Button>
                        </Link>
                    </div>

                    {/* Menu mobile */}
                    <div className="flex items-center gap-2 md:hidden">
                        <ThemeToggle />
                        <LanguageSwitcher />
                        <Link to="/login">
                            <Button variant="ghost" size="sm">{t('nav.login')}</Button>
                        </Link>
                        <Link to="/register">
                            <Button size="sm">{t('nav.register')}</Button>
                        </Link>
                    </div>
                </div>
            </nav>

            {/* ── Hero Section ────────────────────────────────────────────── */}
            <section className="relative overflow-hidden bg-gradient-to-br from-brand-900 via-brand-700 to-brand-500 pt-24">
                {/* Formes décoratives */}
                <div className="absolute -top-40 -right-40 h-80 w-80 rounded-full bg-white/5 blur-3xl" />
                <div className="absolute -bottom-20 -left-20 h-60 w-60 rounded-full bg-accent-400/10 blur-3xl" />

                <div className="relative mx-auto max-w-7xl px-4 pb-20 pt-16 sm:px-6 lg:px-8 lg:pb-28 lg:pt-24">
                    <div className="grid items-center gap-12 lg:grid-cols-2 lg:gap-16">
                        {/* Texte */}
                        <div className="text-center lg:text-left">
                            <div className="mb-6 inline-flex items-center gap-2 rounded-full bg-white/10 px-4 py-1.5 text-sm text-white/90">
                                <Star size={14} className="fill-yellow-400 text-yellow-400" />
                                {t('hero.trustBadge')}
                            </div>
                            <h1 className="text-4xl font-extrabold leading-tight tracking-tight text-white sm:text-5xl lg:text-6xl">
                                {t('hero.title')}
                            </h1>
                            <p className="mt-6 text-lg leading-relaxed text-white/80 sm:text-xl">
                                {t('hero.subtitle')}
                            </p>
                            <div className="mt-8 flex flex-col items-center gap-4 sm:flex-row lg:justify-start">
                                <Button
                                    size="lg"
                                    className="w-full sm:w-auto bg-white text-brand-700 hover:bg-gray-100 hover:text-brand-800 shadow-xl"
                                    onClick={handleGetStarted}
                                >
                                    {t('hero.cta')}
                                    <ArrowRight size={18} />
                                </Button>
                                <a href="#simulator">
                                    <Button
                                        variant="outline"
                                        size="lg"
                                        className="w-full sm:w-auto border-white/30 text-white hover:bg-white/10"
                                    >
                                        {t('simulator.title')}
                                    </Button>
                                </a>
                            </div>
                        </div>

                        {/* Simulateur dans le hero */}
                        <div id="simulator" className="mx-auto w-full max-w-md lg:mx-0">
                            <div className="rounded-2xl border border-white/10 bg-white/10 p-6 backdrop-blur-xl">
                                <h3 className="text-lg font-semibold text-white">{t('simulator.title')}</h3>
                                <p className="mt-1 text-sm text-white/70">{t('simulator.subtitle')}</p>

                                <div className="mt-6 space-y-4">
                                    {/* Montant à envoyer */}
                                    <div>
                                        <label className="mb-1.5 block text-sm font-medium text-white/80">
                                            {t('simulator.youSend')}
                                        </label>
                                        <div className="flex items-center gap-2">
                                            <div className="w-28 shrink-0">
                                                <CurrencySelect
                                                    options={currencyFromOptions}
                                                    value={currencyFrom}
                                                    onChange={setCurrencyFrom}
                                                    label=""
                                                />
                                            </div>
                                            <input
                                                type="number"
                                                min="1"
                                                value={amount || ''}
                                                onChange={(e) => setAmount(Number(e.target.value) || 0)}
                                                className="h-10 flex-1 rounded-lg bg-white/10 px-3 text-right text-lg font-semibold text-white placeholder-white/40 outline-none ring-1 ring-white/20 focus:ring-2 focus:ring-white/50 transition-all"
                                                placeholder="100"
                                            />
                                        </div>
                                    </div>

                                    {/* Devise de destination */}
                                    <CurrencySelect
                                        options={currencyToOptions}
                                        value={currencyTo}
                                        onChange={setCurrencyTo}
                                        label={t('simulator.theyReceive')}
                                    />

                                    {/* Résultat de la simulation */}
                                    {simulating && (
                                        <div className="flex items-center justify-center py-4">
                                            <div className="h-6 w-6 animate-spin rounded-full border-2 border-white/30 border-t-white" />
                                        </div>
                                    )}

                                    {simulation && !simulating && (
                                        <div className="space-y-3 rounded-xl bg-white/10 p-4">
                                            <div className="flex items-center justify-between text-sm">
                                                <span className="text-white/70">{t('simulator.exchangeRate')}</span>
                                                <span className="font-medium text-white">
                                                    1 {currencyFrom} = {simulation.exchangeRate.toLocaleString('fr-FR')} {currencyTo}
                                                </span>
                                            </div>
                                            <div className="flex items-center justify-between text-sm">
                                                <span className="text-white/70">{t('simulator.fees')}</span>
                                                <span className="font-medium text-white">
                                                    {formatCurrency(simulation.fees, currencyFrom)}
                                                </span>
                                            </div>
                                            <div className="flex items-center justify-between text-sm">
                                                <span className="text-white/70">{t('simulator.totalDebited')}</span>
                                                <span className="font-medium text-white">
                                                    {formatCurrency(simulation.totalDebited, currencyFrom)}
                                                </span>
                                            </div>
                                            <div className="border-t border-white/10 pt-3">
                                                <div className="flex items-center justify-between">
                                                    <span className="text-sm font-medium text-white/80">
                                                        {t('simulator.theyReceive')}
                                                    </span>
                                                    <span className="text-xl font-bold text-accent-300">
                                                        {formatCurrency(simulation.amountReceived, currencyTo)}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    )}

                                    {simError && !simulating && (
                                        <p className="text-center text-sm text-red-300">{simError}</p>
                                    )}

                                    <Button
                                        fullWidth
                                        className="bg-white text-brand-700 hover:bg-gray-100"
                                        onClick={handleGetStarted}
                                    >
                                        {t('simulator.cta')}
                                        <ArrowRight size={16} />
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Vague décorative en bas */}
                <div className="absolute bottom-0 left-0 right-0 h-16 bg-gradient-to-t from-gray-50 to-transparent dark:from-gray-950" />
            </section>

            {/* ── Section Fonctionnalités ─────────────────────────────────── */}
            <section id="features" className="bg-gray-50 py-20 sm:py-28 dark:bg-gray-950">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="mx-auto max-w-2xl text-center">
                        <h2 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl dark:text-gray-100">
                            {t('features.title')}
                        </h2>
                        <p className="mt-4 text-lg text-gray-600 dark:text-gray-400">
                            {t('features.subtitle')}
                        </p>
                    </div>

                    <div className="mt-16 grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
                        {features.map((feature) => (
                            <div
                                key={feature.title}
                                className="group rounded-2xl border border-gray-100 bg-white p-6 shadow-sm transition-all duration-200 hover:shadow-md hover:-translate-y-1 dark:border-gray-800 dark:bg-gray-900"
                            >
                                <div className={`inline-flex rounded-xl p-3 ${feature.color}`}>
                                    <feature.icon size={24} />
                                </div>
                                <h3 className="mt-4 text-lg font-semibold text-gray-900 dark:text-gray-100">
                                    {feature.title}
                                </h3>
                                <p className="mt-2 text-sm leading-relaxed text-gray-600 dark:text-gray-400">
                                    {feature.description}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* ── Section Comment ça marche ───────────────────────────────── */}
            <section id="how-it-works" className="bg-white py-20 sm:py-28 dark:bg-gray-900">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="mx-auto max-w-2xl text-center">
                        <h2 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl dark:text-gray-100">
                            {t('howItWorks.title')}
                        </h2>
                        <p className="mt-4 text-lg text-gray-600 dark:text-gray-400">
                            {t('howItWorks.subtitle')}
                        </p>
                    </div>

                    <div className="mt-16 grid gap-8 md:grid-cols-3">
                        {steps.map((step, index) => (
                            <div key={step.number} className="relative text-center">
                                {/* Ligne de connexion */}
                                {index < steps.length - 1 && (
                                    <div className="absolute top-12 left-[60%] hidden h-0.5 w-[80%] bg-brand-200 md:block dark:bg-brand-800" />
                                )}
                                <div className="relative mx-auto flex h-24 w-24 items-center justify-center rounded-full bg-brand-50 dark:bg-brand-900/30">
                                    <step.icon size={36} className="text-brand-600 dark:text-brand-400" />
                                    <span className="absolute -top-2 -right-2 flex h-8 w-8 items-center justify-center rounded-full bg-brand-600 text-sm font-bold text-white">
                                        {step.number}
                                    </span>
                                </div>
                                <h3 className="mt-6 text-xl font-semibold text-gray-900 dark:text-gray-100">
                                    {step.title}
                                </h3>
                                <p className="mt-2 text-sm leading-relaxed text-gray-600 dark:text-gray-400">
                                    {step.description}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* ── Section Statistiques ────────────────────────────────────── */}
            <section className="bg-gradient-to-r from-brand-700 to-brand-500 py-16">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <h2 className="text-center text-2xl font-bold text-white sm:text-3xl">
                        {t('stats.title')}
                    </h2>
                    <div className="mt-12 grid grid-cols-2 gap-8 md:grid-cols-4">
                        {stats.map((stat) => (
                            <div key={stat.label} className="text-center">
                                <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-white/10">
                                    <stat.icon size={24} className="text-white" />
                                </div>
                                <p className="mt-4 text-3xl font-bold text-white">{stat.value}</p>
                                <p className="mt-1 text-sm text-white/70">{stat.label}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* ── Section CTA finale ──────────────────────────────────────── */}
            <section className="bg-gray-50 py-20 sm:py-28 dark:bg-gray-950">
                <div className="mx-auto max-w-2xl px-4 text-center sm:px-6 lg:px-8">
                    <h2 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl dark:text-gray-100">
                        {t('cta.title')}
                    </h2>
                    <p className="mt-4 text-lg text-gray-600 dark:text-gray-400">
                        {t('cta.subtitle')}
                    </p>
                    <div className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
                        <Link to="/login" className="w-full sm:w-auto">
                            <Button variant="outline" size="lg" fullWidth>
                                {t('cta.login')}
                                <ChevronRight size={18} />
                            </Button>
                        </Link>
                        <Link to="/register" className="w-full sm:w-auto">
                            <Button size="lg" fullWidth>
                                {t('cta.register')}
                                <ArrowRight size={18} />
                            </Button>
                        </Link>
                    </div>
                </div>
            </section>

            {/* ── Footer ──────────────────────────────────────────────────── */}
            <footer className="border-t border-gray-100 bg-white py-12 dark:border-gray-800 dark:bg-gray-900">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
                        <div>
                            <div className="flex items-center gap-2">
                                <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                                    S
                                </div>
                                <span className="text-lg font-bold text-gray-900 dark:text-gray-100">SendMe</span>
                            </div>
                            <p className="mt-3 text-sm leading-relaxed text-gray-600 dark:text-gray-400">
                                {t('footer.description')}
                            </p>
                        </div>
                        <div>
                            <h4 className="text-sm font-semibold text-gray-900 dark:text-gray-100">{t('footer.product')}</h4>
                            <ul className="mt-4 space-y-2">
                                <li><a href="#features" className="text-sm text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.features')}</a></li>
                                <li><a href="#" className="text-sm text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.security')}</a></li>
                                <li><a href="#" className="text-sm text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.support')}</a></li>
                            </ul>
                        </div>
                        <div>
                            <h4 className="text-sm font-semibold text-gray-900 dark:text-gray-100">{t('footer.company')}</h4>
                            <ul className="mt-4 space-y-2">
                                <li><a href="#" className="text-sm text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.about')}</a></li>
                                <li><a href="#" className="text-sm text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.contact')}</a></li>
                                <li><a href="#" className="text-sm text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.legal')}</a></li>
                            </ul>
                        </div>
                        <div>
                            <h4 className="text-sm font-semibold text-gray-900 dark:text-gray-100">Support</h4>
                            <ul className="mt-4 space-y-2">
                                <li><a href="mailto:support@sendme.com" className="text-sm text-gray-600 hover:text-brand-600 transition-colors dark:text-gray-400 dark:hover:text-brand-400">support@sendme.com</a></li>
                            </ul>
                        </div>
                    </div>
                    <div className="mt-10 border-t border-gray-100 pt-6 text-center text-sm text-gray-500 dark:border-gray-800 dark:text-gray-500">
                        {t('footer.copyright', { year: new Date().getFullYear() })}
                    </div>
                </div>
            </footer>
        </div>
    );
}

export default Home;
