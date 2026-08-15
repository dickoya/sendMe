import { useState, useEffect, useCallback, useRef } from 'react';
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
    Gift,
    Euro,
    Menu,
    X,
    Sparkles,
    ArrowUpRight,
    Clock,
    Lock,
    Repeat,
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
 * Page d'accueil publique de SendMe — Design ultra-moderne.
 * Palette indigo/turquoise, glassmorphism, animations fluides.
 */
function Home() {
    const { t } = useTranslation('home');
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    // Options de devises
    const currencyFromOptions: CurrencyOption[] = [
        { code: 'EUR', label: 'Euro', flag: 'eu' },
        { code: 'USD', label: 'US Dollar', flag: 'us' },
        { code: 'GBP', label: 'Livre Sterling', flag: 'gb' },
        { code: 'CAD', label: 'Dollar Canadien', flag: 'ca' },
        { code: 'CHF', label: 'Franc Suisse', flag: 'ch' },
    ];

    const currencyToOptions: CurrencyOption[] = [
        { code: 'XOF', label: 'Franc CFA (UEMOA)', flag: 'xof' },
        { code: 'XAF', label: 'Franc CFA (CEMAC)', flag: 'xaf' },
        { code: 'GNF', label: 'Franc Guinéen', flag: 'gn' },
        { code: 'NGN', label: 'Naira Nigérian', flag: 'ng' },
        { code: 'GHS', label: 'Cedi Ghanéen', flag: 'gh' },
        { code: 'ZAR', label: 'Rand Sud-Africain', flag: 'za' },
        { code: 'MAD', label: 'Dirham Marocain', flag: 'ma' },
        { code: 'TND', label: 'Dinar Tunisien', flag: 'tn' },
    ];

    // Simulateur
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

    // Statistiques — valeurs réelles de l'application (devises, canaux, couverture)
    const stats = [
        { value: '13', label: t('stats.currencies'), icon: Euro },
        { value: '6', label: t('stats.channels'), icon: Smartphone },
        { value: '8', label: t('stats.receiving'), icon: Globe },
        { value: '24/7', label: t('stats.available'), icon: Clock },
    ];

    // Fonctionnalités
    const features = [
        {
            icon: Zap,
            title: t('features.speed.title'),
            description: t('features.speed.description'),
        },
        {
            icon: Shield,
            title: t('features.security.title'),
            description: t('features.security.description'),
        },
        {
            icon: TrendingDown,
            title: t('features.rates.title'),
            description: t('features.rates.description'),
        },
        {
            icon: Smartphone,
            title: t('features.channels.title'),
            description: t('features.channels.description'),
        },
    ];

    // Étapes
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

    // Animation au scroll
    const observerRef = useRef<IntersectionObserver | null>(null);
    const animateRef = useCallback((node: HTMLElement | null) => {
        if (node) {
            observerRef.current = new IntersectionObserver(
                (entries) => {
                    entries.forEach((entry) => {
                        if (entry.isIntersecting) {
                            entry.target.classList.add('animate-in');
                            observerRef.current?.unobserve(entry.target);
                        }
                    });
                },
                { threshold: 0.1 }
            );
            observerRef.current.observe(node);
        }
    }, []);

    return (
        <div className="flex min-h-screen flex-col overflow-x-hidden">
            {/* ===== NAVIGATION ===== */}
            <nav className="fixed top-0 left-0 right-0 z-50 border-b border-white/10 bg-white/70 backdrop-blur-xl dark:bg-[#0A0A0F]/70 dark:border-[#1E1E30]/50">
                <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
                    <Link to="/" className="flex items-center gap-2.5 group">
                        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-600 text-sm font-bold text-white shadow-lg shadow-brand-500/20 transition-transform duration-300 group-hover:scale-110 group-hover:shadow-brand-500/30">
                            S
                        </div>
                        <span className="text-lg font-bold bg-gradient-to-r from-brand-600 to-brand-400 bg-clip-text text-transparent dark:from-brand-400 dark:to-brand-200">
                            SendMe
                        </span>
                    </Link>

                    {/* Navigation desktop */}
                    <div className="hidden items-center gap-8 md:flex">
                        <a href="#features" className="text-sm font-medium text-gray-600 hover:text-brand-500 transition-all duration-200 dark:text-gray-400 dark:hover:text-brand-400 relative after:absolute after:bottom-0 after:left-0 after:h-0.5 after:w-0 after:bg-brand-500 after:transition-all after:duration-300 hover:after:w-full">
                            {t('nav.features')}
                        </a>
                        <a href="#how-it-works" className="text-sm font-medium text-gray-600 hover:text-brand-500 transition-all duration-200 dark:text-gray-400 dark:hover:text-brand-400 relative after:absolute after:bottom-0 after:left-0 after:h-0.5 after:w-0 after:bg-brand-500 after:transition-all after:duration-300 hover:after:w-full">
                            {t('nav.howItWorks')}
                        </a>
                        <ThemeToggle />
                        <LanguageSwitcher />
                        <Link to="/login">
                            <Button variant="ghost" size="sm">{t('nav.login')}</Button>
                        </Link>
                        <Link to="/register">
                            <Button size="sm">{t('nav.register')}</Button>
                        </Link>
                    </div>

                    {/* Menu mobile */}
                    <div className="flex items-center gap-2 md:hidden">
                        <ThemeToggle />
                        <LanguageSwitcher />
                        <button
                            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                            className="flex h-10 w-10 items-center justify-center rounded-xl text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-[#1A1A26]"
                        >
                            {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
                        </button>
                    </div>
                </div>

                {/* Mobile menu dropdown */}
                {mobileMenuOpen && (
                    <div className="border-t border-gray-100 bg-white px-4 pb-4 pt-2 md:hidden dark:border-[#1E1E30] dark:bg-[#0A0A0F]">
                        <div className="flex flex-col gap-3">
                            <a href="#features" onClick={() => setMobileMenuOpen(false)} className="rounded-xl px-4 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-[#12121A]">{t('nav.features')}</a>
                            <a href="#how-it-works" onClick={() => setMobileMenuOpen(false)} className="rounded-xl px-4 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-50 dark:text-gray-400 dark:hover:bg-[#12121A]">{t('nav.howItWorks')}</a>
                            <div className="flex gap-2 pt-2">
                                <Link to="/login" className="flex-1" onClick={() => setMobileMenuOpen(false)}>
                                    <Button variant="outline" size="sm" fullWidth>{t('nav.login')}</Button>
                                </Link>
                                <Link to="/register" className="flex-1" onClick={() => setMobileMenuOpen(false)}>
                                    <Button size="sm" fullWidth>{t('nav.register')}</Button>
                                </Link>
                            </div>
                        </div>
                    </div>
                )}
            </nav>

            {/* ===== HERO SECTION ===== */}
            <section className="relative overflow-hidden pt-24">
                {/* Fond clair et épuré */}
                <div className="absolute inset-0 bg-gradient-to-b from-gray-50 via-white to-white dark:from-[#0A0A0F] dark:via-[#0A0A0F] dark:to-[#0A0A0F]">
                    {/* Halo très feutré, simple touche de lumière */}
                    <div className="absolute -top-40 -right-40 h-[400px] w-[400px] rounded-full bg-brand-500/5 blur-3xl" />
                    <div className="absolute -bottom-40 -left-40 h-[350px] w-[350px] rounded-full bg-gray-100/70 blur-3xl dark:bg-brand-500/5" />
                </div>

                <div className="relative mx-auto max-w-7xl px-4 pb-24 pt-20 sm:px-6 lg:px-8 lg:pb-32 lg:pt-28">
                    <div className="grid items-center gap-16 lg:grid-cols-2 lg:gap-20">
                        {/* Texte Hero */}
                        <div className="min-w-0 text-center lg:text-left" ref={animateRef}>
                            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-gray-200 bg-white px-4 py-1.5 text-sm text-gray-600 shadow-sm animate-fade-in dark:border-[#1E1E30] dark:bg-[#12121A] dark:text-gray-300">
                                <Sparkles size={14} className="text-brand-500" />
                                {t('hero.trustBadge')}
                            </div>
                            <h1 className="break-words text-3xl font-bold leading-tight tracking-tight text-gray-900 sm:text-4xl lg:text-5xl animate-fade-in-up dark:text-gray-100">
                                {t('hero.title')}
                            </h1>
                            <p className="mt-6 break-words text-lg leading-relaxed text-gray-500 sm:text-xl animate-fade-in-up dark:text-gray-400" style={{ animationDelay: '0.1s' }}>
                                {t('hero.subtitle')}
                            </p>
                            <div className="mt-10 flex flex-col items-center gap-4 sm:flex-row lg:justify-start animate-fade-in-up" style={{ animationDelay: '0.2s' }}>
                                <Button
                                    size="lg"
                                    className="w-full sm:w-auto bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-lg shadow-brand-500/15 hover:shadow-xl hover:shadow-brand-500/25 group"
                                    onClick={handleGetStarted}
                                >
                                    {t('hero.cta')}
                                    <ArrowUpRight size={18} className="transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
                                </Button>
                                <a href="#simulator" className="w-full sm:w-auto">
                                    <Button
                                        variant="outline"
                                        size="lg"
                                        className="w-full border-gray-200 text-gray-700 hover:border-brand-400 hover:text-brand-600"
                                    >
                                        {t('simulator.title')}
                                    </Button>
                                </a>
                            </div>

                            {/* Trust indicators */}
                            <div className="mt-12 flex flex-wrap items-center justify-center gap-6 lg:justify-start animate-fade-in-up" style={{ animationDelay: '0.3s' }}>
                                <div className="flex items-center gap-2 text-gray-400 dark:text-gray-500">
                                    <Lock size={14} />
                                    <span className="text-xs">Sécurisé</span>
                                </div>
                                <div className="flex items-center gap-2 text-gray-400 dark:text-gray-500">
                                    <Clock size={14} />
                                    <span className="text-xs">Instantané</span>
                                </div>
                                <div className="flex items-center gap-2 text-gray-400 dark:text-gray-500">
                                    <Repeat size={14} />
                                    <span className="text-xs">Taux réel</span>
                                </div>
                            </div>
                        </div>

                        {/* Simulateur dans le hero — Glassmorphism */}
                        <div id="simulator" className="mx-auto w-full min-w-0 max-w-md lg:mx-0 animate-fade-in-up" style={{ animationDelay: '0.15s' }} ref={animateRef}>
                            <div className="group relative rounded-2xl border border-gray-200 bg-white p-6 shadow-lg shadow-gray-200/60 transition-all duration-500 hover:shadow-xl hover:shadow-brand-500/5 sm:p-8 dark:border-[#1E1E30] dark:bg-[#12121A] dark:shadow-black/30">
                                {/* Halo très discret au survol */}
                                <div className="absolute -inset-0.5 rounded-2xl bg-brand-500/5 opacity-0 blur-xl transition-opacity duration-500 group-hover:opacity-100" />
                                <div className="relative">
                                    <div className="flex items-center gap-3 mb-2">
                                        <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-600 shadow-md shadow-brand-500/15">
                                            <Send size={18} className="text-white" />
                                        </div>
                                        <div>
                                            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">{t('simulator.title')}</h3>
                                            <p className="text-sm text-gray-500 dark:text-gray-400">{t('simulator.subtitle')}</p>
                                        </div>
                                    </div>

                                    <div className="mt-6 space-y-4">
                                        {/* Montant à envoyer */}
                                        <div>
                                            <label className="mb-1.5 block text-sm font-medium text-gray-600 dark:text-gray-300">
                                                {t('simulator.youSend')}
                                            </label>
                                            <div className="flex items-center gap-2">
                                                <div className="w-24 shrink-0 sm:w-28">
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
                                                    className="h-11 min-w-0 flex-1 rounded-xl bg-gray-50 px-3 text-right text-lg font-semibold text-gray-900 placeholder-gray-400 outline-none ring-1 ring-gray-200 transition-all focus:ring-2 focus:ring-brand-400/60 dark:bg-[#0A0A0F] dark:text-gray-100 dark:placeholder-gray-500 dark:ring-[#2A2A40]"
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
                                                <div className="h-6 w-6 animate-spin rounded-full border-2 border-white/20 border-t-accent-400" />
                                            </div>
                                        )}

                                        {simulation && !simulating && (
                                            <div className="space-y-3 rounded-xl bg-gray-50 p-4 ring-1 ring-gray-100 dark:bg-[#0A0A0F] dark:ring-[#1E1E30]">
                                                <div className="flex items-center justify-between text-sm">
                                                    <span className="text-gray-500 dark:text-gray-400">{t('simulator.exchangeRate')}</span>
                                                    <span className="font-medium text-gray-900 dark:text-gray-100">
                                                        1 {currencyFrom} = {simulation.exchangeRate.toLocaleString('fr-FR')} {currencyTo}
                                                    </span>
                                                </div>
                                                <div className="flex items-center justify-between text-sm">
                                                    <span className="text-gray-500 dark:text-gray-400">{t('simulator.fees')}</span>
                                                    <span className="font-medium text-gray-900 dark:text-gray-100">
                                                        {formatCurrency(simulation.fees, currencyFrom)}
                                                    </span>
                                                </div>
                                                <div className="flex items-center justify-between text-sm">
                                                    <span className="text-gray-500 dark:text-gray-400">{t('simulator.totalDebited')}</span>
                                                    <span className="font-medium text-gray-900 dark:text-gray-100">
                                                        {formatCurrency(simulation.totalDebited, currencyFrom)}
                                                    </span>
                                                </div>
                                                <div className="border-t border-gray-200 pt-3 dark:border-[#2A2A40]">
                                                    <div className="flex items-center justify-between">
                                                        <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
                                                            {t('simulator.theyReceive')}
                                                        </span>
                                                        <span className="text-xl font-bold bg-gradient-to-r from-brand-500 to-brand-600 bg-clip-text text-transparent">
                                                            {formatCurrency(simulation.amountReceived, currencyTo)}
                                                        </span>
                                                    </div>
                                                </div>
                                            </div>
                                        )}

                                        {simError && !simulating && (
                                            <p className="text-center text-sm text-red-400">{simError}</p>
                                        )}

                                        <Button
                                            fullWidth
                                            className="bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-lg shadow-brand-500/20 hover:shadow-xl hover:shadow-brand-500/30 hover:from-brand-600 hover:to-brand-700 group"
                                            onClick={handleGetStarted}
                                        >
                                            {t('simulator.cta')}
                                            <ArrowRight size={16} className="transition-transform group-hover:translate-x-1" />
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Vague décorative en bas */}
                <div className="absolute bottom-0 left-0 right-0 h-20 bg-gradient-to-t from-gray-50 to-transparent dark:from-[#0A0A0F]" />
            </section>

            {/* ===== BANDEAU PROMOTIONNEL ===== */}
            <section className="bg-gray-50 py-10 dark:bg-[#0A0A0F]">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <h2 className="mb-6 text-center text-2xl font-bold tracking-tight text-gray-900 sm:text-3xl dark:text-gray-100">
                        {t('promo.title')}
                    </h2>
                    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm dark:border-[#1E1E30] dark:bg-[#12121A]">
                        <img
                            src="/publicitesendme.jpeg"
                            alt="SendMe — promotion"
                            className="h-auto w-full object-cover"
                        />
                    </div>
                </div>
            </section>

            {/* ===== SECTION FONCTIONNALITÉS ===== */}
            <section id="features" className="bg-gray-50 py-24 sm:py-32 dark:bg-[#0A0A0F]">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="mx-auto max-w-2xl text-center" ref={animateRef}>
                        <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-400 to-brand-600 shadow-lg shadow-brand-500/20">
                            <Sparkles size={22} className="text-white" />
                        </div>
                        <h2 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl dark:text-gray-100">
                            {t('features.title')}
                        </h2>
                        <p className="mt-4 text-lg text-gray-500 dark:text-gray-400">
                            {t('features.subtitle')}
                        </p>
                    </div>

                    <div className="mt-16 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
                        {features.map((feature, index) => (
                            <div
                                key={feature.title}
                                ref={animateRef}
                                className="group relative rounded-2xl border border-gray-100 bg-white p-6 shadow-sm transition-all duration-300 hover:shadow-xl hover:shadow-brand-500/5 hover:-translate-y-1 dark:border-[#1E1E30] dark:bg-[#12121A]"
                                style={{ animationDelay: `${index * 0.1}s` }}
                            >
                                {/* Halo très discret au survol */}
                                <div className="absolute inset-0 rounded-2xl bg-brand-500/5 opacity-0 transition-opacity duration-300 group-hover:opacity-100" />
                                <div className="relative">
                                    <div className="inline-flex rounded-xl bg-gradient-to-br from-brand-500 to-brand-600 p-3 shadow-md shadow-brand-500/15">
                                        <feature.icon size={22} className="text-white" />
                                    </div>
                                    <h3 className="mt-5 text-lg font-semibold text-gray-900 dark:text-gray-100">
                                        {feature.title}
                                    </h3>
                                    <p className="mt-2 text-sm leading-relaxed text-gray-500 dark:text-gray-400">
                                        {feature.description}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* ===== SECTION COMMENT ÇA MARCHE ===== */}
            <section id="how-it-works" className="bg-white py-24 sm:py-32 dark:bg-[#12121A]">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="mx-auto max-w-2xl text-center" ref={animateRef}>
                        <h2 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl dark:text-gray-100">
                            {t('howItWorks.title')}
                        </h2>
                        <p className="mt-4 text-lg text-gray-500 dark:text-gray-400">
                            {t('howItWorks.subtitle')}
                        </p>
                    </div>

                    <div className="mt-16 grid gap-8 md:grid-cols-3">
                        {steps.map((step, index) => (
                            <div key={step.number} className="relative text-center" ref={animateRef} style={{ animationDelay: `${index * 0.15}s` }}>
                                {/* Ligne de connexion */}
                                {index < steps.length - 1 && (
                                    <div className="absolute top-12 left-[60%] hidden h-0.5 w-[80%] bg-gradient-to-r from-brand-200 to-brand-300 md:block dark:from-brand-800 dark:to-brand-700" />
                                )}
                                <div className="relative mx-auto flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-brand-50 to-brand-100 dark:from-brand-900/40 dark:to-brand-800/20 ring-1 ring-brand-200/50 dark:ring-brand-700/30">
                                    <step.icon size={36} className="text-brand-600 dark:text-brand-400" />
                                    <span className="absolute -top-2 -right-2 flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-brand-600 text-sm font-bold text-white shadow-lg shadow-brand-500/20">
                                        {step.number}
                                    </span>
                                </div>
                                <h3 className="mt-6 text-xl font-semibold text-gray-900 dark:text-gray-100">
                                    {step.title}
                                </h3>
                                <p className="mt-2 text-sm leading-relaxed text-gray-500 dark:text-gray-400">
                                    {step.description}
                                </p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* ===== SECTION PARRAINAGE ===== */}
            <section className="relative overflow-hidden bg-white py-24 sm:py-32 dark:bg-[#12121A]">
                <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="mx-auto max-w-2xl text-center" ref={animateRef}>
                        <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 to-brand-600 shadow-lg shadow-brand-500/15">
                            <Gift size={26} className="text-white" />
                        </div>
                        <h2 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl dark:text-gray-100">
                            {t('referral.title')}
                        </h2>
                        <p className="mt-4 text-lg text-gray-500 dark:text-gray-400">
                            {t('referral.subtitle')}
                        </p>
                    </div>

                    <div className="mt-16 grid gap-8 md:grid-cols-3">
                        {/* Avantage 1 */}
                        <div className="group relative rounded-2xl border border-gray-100 bg-gray-50 p-8 text-center transition-all duration-300 hover:shadow-xl hover:-translate-y-1 dark:border-[#1E1E30] dark:bg-[#0A0A0F]" ref={animateRef}>
                            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-brand-600 text-2xl font-bold text-white shadow-md shadow-brand-500/15">
                                <Euro size={24} />
                            </div>
                            <h3 className="mt-6 text-xl font-bold text-gray-900 dark:text-gray-100">
                                {t('referral.benefit1')}
                            </h3>
                            <p className="mt-2 text-sm leading-relaxed text-gray-500 dark:text-gray-400">
                                {t('referral.benefit1Desc')}
                            </p>
                        </div>

                        {/* Avantage 2 — Mise en avant */}
                        <div className="group relative rounded-2xl border-2 border-brand-100 bg-gradient-to-b from-brand-50/60 to-white p-8 text-center shadow-xl shadow-brand-500/10 transition-all duration-300 hover:shadow-2xl hover:shadow-brand-500/15 hover:-translate-y-1 dark:border-brand-700/40 dark:from-brand-900/20 dark:to-[#12121A]" ref={animateRef}>
                            {/* Badge "Populaire" */}
                            <div className="absolute -top-3 left-1/2 -translate-x-1/2">
                                <span className="inline-flex items-center gap-1 rounded-full bg-gradient-to-r from-brand-500 to-brand-600 px-4 py-1 text-xs font-semibold text-white shadow-md shadow-brand-500/20">
                                    <Gift size={12} />
                                    {t('referral.benefit2')}
                                </span>
                            </div>
                            <div className="mx-auto mt-4 flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-brand-600 text-2xl font-bold text-white shadow-md shadow-brand-500/15">
                                <Users size={24} />
                            </div>
                            <h3 className="mt-6 text-xl font-bold text-gray-900 dark:text-gray-100">
                                {t('referral.benefit2')}
                            </h3>
                            <p className="mt-2 text-sm leading-relaxed text-gray-500 dark:text-gray-400">
                                {t('referral.benefit2Desc')}
                            </p>
                        </div>

                        {/* Avantage 3 */}
                        <div className="group relative rounded-2xl border border-gray-100 bg-gray-50 p-8 text-center transition-all duration-300 hover:shadow-xl hover:-translate-y-1 dark:border-[#1E1E30] dark:bg-[#0A0A0F]" ref={animateRef}>
                            <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-brand-600 text-2xl font-bold text-white shadow-md shadow-brand-500/15">
                                <Star size={24} />
                            </div>
                            <h3 className="mt-6 text-xl font-bold text-gray-900 dark:text-gray-100">
                                {t('referral.benefit3')}
                            </h3>
                            <p className="mt-2 text-sm leading-relaxed text-gray-500 dark:text-gray-400">
                                {t('referral.benefit3Desc')}
                            </p>
                        </div>
                    </div>

                    {/* CTA */}
                    <div className="mt-12 text-center" ref={animateRef}>
                        <Link to="/register">
                            <Button size="lg" className="bg-gradient-to-r from-brand-500 to-brand-600 text-white shadow-lg shadow-brand-500/15 hover:from-brand-600 hover:to-brand-700 hover:shadow-xl hover:shadow-brand-500/25 group">
                                {t('referral.cta')}
                                <ArrowRight size={18} className="transition-transform group-hover:translate-x-1" />
                            </Button>
                        </Link>
                        <p className="mt-3 text-sm text-gray-500 dark:text-gray-400">
                            {t('referral.ctaSub')}
                        </p>
                    </div>
                </div>
            </section>

            {/* ===== SECTION STATISTIQUES ===== */}
            <section className="relative overflow-hidden border-y border-gray-100 bg-white py-20 dark:border-[#1E1E30] dark:bg-[#0A0A0F]">
                <div className="relative mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <h2 className="text-center text-2xl font-bold text-gray-900 sm:text-3xl dark:text-gray-100">
                        {t('stats.title')}
                    </h2>
                    <div className="mt-12 grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-4">
                        {stats.map((stat) => (
                            <div key={stat.label} className="text-center group" ref={animateRef}>
                                <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-xl bg-brand-50 text-brand-600 transition-all duration-300 group-hover:bg-brand-100 group-hover:scale-110 dark:bg-brand-500/10 dark:text-brand-300">
                                    <stat.icon size={24} />
                                </div>
                                <p className="mt-4 text-3xl font-bold text-gray-900 dark:text-gray-100">{stat.value}</p>
                                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{stat.label}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* ===== SECTION CTA FINALE ===== */}
            <section className="bg-gray-50 py-24 sm:py-32 dark:bg-[#0A0A0F]">
                <div className="mx-auto max-w-2xl px-4 text-center sm:px-6 lg:px-8" ref={animateRef}>
                    <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-400 to-brand-600 shadow-xl shadow-brand-500/20">
                        <Send size={28} className="text-white" />
                    </div>
                    <h2 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl dark:text-gray-100">
                        {t('cta.title')}
                    </h2>
                    <p className="mt-4 text-lg text-gray-500 dark:text-gray-400">
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

            {/* ===== FOOTER ===== */}
            <footer className="border-t border-gray-100 bg-white py-12 dark:border-[#1E1E30] dark:bg-[#0A0A0F]">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="grid gap-8 sm:grid-cols-2 lg:grid-cols-4">
                        <div>
                            <div className="flex items-center gap-2.5">
                                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-brand-600 text-sm font-bold text-white shadow-lg shadow-brand-500/20">
                                    S
                                </div>
                                <span className="text-lg font-bold bg-gradient-to-r from-brand-600 to-brand-400 bg-clip-text text-transparent dark:from-brand-400 dark:to-brand-200">
                                    SendMe
                                </span>
                            </div>
                            <p className="mt-3 break-words text-sm leading-relaxed text-gray-500 dark:text-gray-400">
                                {t('footer.description')}
                            </p>
                        </div>
                        <div>
                            <h4 className="text-sm font-semibold text-gray-900 dark:text-gray-100">{t('footer.product')}</h4>
                            <ul className="mt-4 space-y-2">
                                <li><a href="#features" className="text-sm text-gray-500 hover:text-brand-500 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.features')}</a></li>
                                <li><a href="#features" className="text-sm text-gray-500 hover:text-brand-500 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.security')}</a></li>
                                <li><a href="mailto:support@sendme.com" className="text-sm text-gray-500 hover:text-brand-500 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.support')}</a></li>
                            </ul>
                        </div>
                        <div>
                            <h4 className="text-sm font-semibold text-gray-900 dark:text-gray-100">{t('footer.company')}</h4>
                            <ul className="mt-4 space-y-2">
                                <li><a href="#how-it-works" className="text-sm text-gray-500 hover:text-brand-500 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.about')}</a></li>
                                <li><Link to="/contact" className="text-sm text-gray-500 hover:text-brand-500 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.contact')}</Link></li>
                                <li><Link to="/legal" className="text-sm text-gray-500 hover:text-brand-500 transition-colors dark:text-gray-400 dark:hover:text-brand-400">{t('footer.legal')}</Link></li>
                            </ul>
                        </div>
                        <div>
                            <h4 className="text-sm font-semibold text-gray-900 dark:text-gray-100">Support</h4>
                            <ul className="mt-4 space-y-2">
                                <li><a href="mailto:support@sendme.com" className="text-sm text-gray-500 hover:text-brand-500 transition-colors dark:text-gray-400 dark:hover:text-brand-400">support@sendme.com</a></li>
                            </ul>
                        </div>
                    </div>
                    <div className="mt-10 border-t border-gray-100 pt-6 text-center text-sm text-gray-400 dark:border-[#1E1E30] dark:text-[#5A5E72]">
                        {t('footer.copyright', { year: new Date().getFullYear() })}
                    </div>
                </div>
            </footer>
        </div>
    );
}

export default Home;
