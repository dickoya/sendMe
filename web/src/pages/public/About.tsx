import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import {
    Target,
    Zap,
    Eye,
    ShieldCheck,
    Globe2,
    HeartHandshake,
    Sparkles,
    Lightbulb,
    ArrowRight,
    Mail,
    Scale,
} from 'lucide-react';
import Button from '@/components/ui/Button';

/**
 * Icônes Lucide associées aux fonctionnalités (mission) et aux valeurs.
 * L'ordre doit correspondre à l'ordre des entrées dans les fichiers i18n.
 */
const missionIcons = [Zap, Eye, ShieldCheck, Globe2];
const valueIcons = [HeartHandshake, Eye, Sparkles, Lightbulb];

/**
 * Page « À propos » — design moderne et professionnel, cohérent
 * avec la page d'accueil et les pages Legal / Contact.
 *
 * Accessible depuis le lien « À propos » du pied de page (route /about).
 */
function About() {
    const { t } = useTranslation('about');

    const missionFeatures = t('mission.features', { returnObjects: true }) as Array<{
        title: string;
        description: string;
    }>;

    const stats = t('stats.items', { returnObjects: true }) as Array<{
        value: string;
        label: string;
    }>;

    const values = t('values.items', { returnObjects: true }) as Array<{
        title: string;
        description: string;
    }>;

    return (
        <div className="min-h-screen bg-gradient-to-b from-gray-50 to-white dark:from-[#0D0D0D] dark:to-[#111111]">
            {/* ===== HERO ===== */}
            <section className="relative overflow-hidden border-b border-gray-100 bg-white pb-16 pt-14 dark:border-[#222222] dark:bg-[#0D0D0D]">
                {/* Fond décoratif */}
                <div className="pointer-events-none absolute inset-0 overflow-hidden">
                    <div className="absolute -right-40 -top-40 h-96 w-96 rounded-full bg-brand-50/60 blur-3xl dark:bg-brand-900/10" />
                    <div className="absolute -bottom-40 -left-40 h-96 w-96 rounded-full bg-blue-50/60 blur-3xl dark:bg-blue-900/10" />
                </div>

                <div className="relative mx-auto max-w-4xl px-4 text-center sm:px-6 lg:px-8">
                    <div className="mx-auto mb-6 inline-flex items-center gap-2 rounded-full border border-brand-100 bg-brand-50/70 px-4 py-1.5 text-xs font-medium text-brand-700 dark:border-brand-900/30 dark:bg-brand-500/10 dark:text-brand-300">
                        <Sparkles size={14} />
                        {t('hero.badge')}
                    </div>
                    <h1 className="text-3xl font-bold tracking-tight text-gray-900 dark:text-[#EDEEF0] sm:text-4xl">
                        {t('hero.title')}
                    </h1>
                    <p className="mx-auto mt-5 max-w-2xl text-base leading-relaxed text-gray-500 dark:text-[#8B8FA3] sm:text-lg">
                        {t('hero.subtitle')}
                    </p>
                </div>
            </section>


            {/* ===== MISSION ===== */}
            <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
                <div className="grid items-start gap-12 lg:grid-cols-2">
                    {/* Texte de mission */}
                    <div>
                        <span className="text-sm font-semibold uppercase tracking-wider text-brand-600 dark:text-brand-400">
                            {t('mission.badge')}
                        </span>
                        <h2 className="mt-3 text-2xl font-bold tracking-tight text-gray-900 dark:text-[#EDEEF0] sm:text-3xl">
                            {t('mission.title')}
                        </h2>
                        <p className="mt-4 text-base leading-relaxed text-gray-600 dark:text-[#8B8FA3]">
                            {t('mission.p1')}
                        </p>
                        <p className="mt-3 text-base leading-relaxed text-gray-600 dark:text-[#8B8FA3]">
                            {t('mission.p2')}
                        </p>

                        <div className="mt-8 flex flex-wrap gap-3">
                            <Link to="/register">
                                <Button variant="primary" size="lg">
                                    {t('cta.register')}
                                    <ArrowRight size={18} />
                                </Button>
                            </Link>
                            <Link to="/contact">
                                <Button variant="outline" size="lg">
                                    <Mail size={18} />
                                    {t('cta.contact')}
                                </Button>
                            </Link>
                        </div>
                    </div>

                    {/* Ce qui nous distingue */}
                    <div>
                        <div className="grid gap-4 sm:grid-cols-2">
                            {missionFeatures.map((feature, idx) => {
                                const Icon = missionIcons[idx] || Zap;
                                return (
                                    <div
                                        key={feature.title}
                                        className="group rounded-2xl border border-gray-100 bg-white p-6 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-lg hover:shadow-brand-500/5 dark:border-[#222222] dark:bg-[#0D0D0D] dark:hover:border-[#333333]"
                                    >
                                        <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-600 transition-colors group-hover:bg-brand-600 group-hover:text-white dark:bg-[#222222] dark:text-brand-400 dark:group-hover:bg-brand-500 dark:group-hover:text-white">
                                            <Icon size={22} />
                                        </div>
                                        <h3 className="text-base font-semibold text-gray-900 dark:text-[#EDEEF0]">
                                            {feature.title}
                                        </h3>
                                        <p className="mt-1.5 text-sm leading-relaxed text-gray-500 dark:text-[#8B8FA3]">
                                            {feature.description}
                                        </p>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>
            </section>


            {/* ===== STATISTIQUES ===== */}
            <section className="border-y border-gray-100 bg-white py-16 dark:border-[#1E1E30] dark:bg-[#0A0A0F]">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="mx-auto max-w-2xl text-center">
                        <h2 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-[#EDEEF0] sm:text-3xl">
                            {t('stats.title')}
                        </h2>
                        <p className="mt-3 text-base text-gray-500 dark:text-[#8B8FA3]">
                            {t('stats.subtitle')}
                        </p>
                    </div>
                    <div className="mt-10 grid grid-cols-2 gap-6 lg:grid-cols-4">
                        {stats.map((stat) => (
                            <div
                                key={stat.label}
                                className="rounded-2xl border border-gray-100 bg-gradient-to-b from-gray-50 to-white p-6 text-center dark:border-[#222222] dark:from-[#111118] dark:to-[#0D0D0D]"
                            >
                                <p className="bg-gradient-to-r from-brand-600 to-brand-400 bg-clip-text text-3xl font-bold text-transparent dark:from-brand-400 dark:to-brand-200">
                                    {stat.value}
                                </p>
                                <p className="mt-2 text-sm text-gray-500 dark:text-[#8B8FA3]">{stat.label}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>


            {/* ===== VALEURS ===== */}
            <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
                <div className="mx-auto max-w-2xl text-center">
                    <span className="text-sm font-semibold uppercase tracking-wider text-brand-600 dark:text-brand-400">
                        {t('values.badge')}
                    </span>
                    <h2 className="mt-3 text-2xl font-bold tracking-tight text-gray-900 dark:text-[#EDEEF0] sm:text-3xl">
                        {t('values.title')}
                    </h2>
                    <p className="mt-3 text-base text-gray-500 dark:text-[#8B8FA3]">{t('values.subtitle')}</p>
                </div>

                <div className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
                    {values.map((value, idx) => {
                        const Icon = valueIcons[idx] || HeartHandshake;
                        return (
                            <div
                                key={value.title}
                                className="flex flex-col rounded-2xl border border-gray-100 bg-white p-6 shadow-sm transition-all duration-200 hover:border-brand-200 hover:shadow-md dark:border-[#222222] dark:bg-[#0D0D0D] dark:hover:border-brand-900/40"
                            >
                                <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 to-brand-600 shadow-lg shadow-brand-500/15">
                                    <Icon size={22} className="text-white" />
                                </div>
                                <h3 className="text-base font-semibold text-gray-900 dark:text-[#EDEEF0]">
                                    {value.title}
                                </h3>
                                <p className="mt-2 text-sm leading-relaxed text-gray-500 dark:text-[#8B8FA3]">
                                    {value.description}
                                </p>
                            </div>
                        );
                    })}
                </div>
            </section>


            {/* ===== CTA FINAL ===== */}
            <section className="border-t border-gray-100 bg-white py-16 dark:border-[#1E1E30] dark:bg-[#0A0A0F]">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-brand-600 to-brand-500 px-6 py-14 text-center shadow-2xl shadow-brand-500/20 sm:px-16">
                        <div className="pointer-events-none absolute inset-0 overflow-hidden">
                            <div className="absolute -left-20 -top-20 h-64 w-64 rounded-full bg-white/10 blur-2xl" />
                            <div className="absolute -bottom-24 -right-16 h-72 w-72 rounded-full bg-white/10 blur-2xl" />
                        </div>
                        <div className="relative">
                            <Target className="mx-auto mb-5 h-12 w-12 text-white/90" />
                            <h2 className="text-2xl font-bold tracking-tight text-white sm:text-3xl">
                                {t('cta.title')}
                            </h2>
                            <p className="mx-auto mt-3 max-w-xl text-base text-brand-50">
                                {t('cta.subtitle')}
                            </p>
                            <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
                                <Link to="/register">
                                    <Button
                                        size="lg"
                                        className="border-2 border-white/30 bg-white text-brand-700 shadow-lg hover:bg-brand-50 hover:shadow-xl dark:bg-white dark:text-brand-700"
                                    >
                                        {t('cta.register')}
                                        <ArrowRight size={18} />
                                    </Button>
                                </Link>
                                <Link
                                    to="/legal"
                                    className="inline-flex items-center gap-2 rounded-xl px-5 py-2.5 text-base font-medium text-white/90 transition-colors hover:text-white"
                                >
                                    <Scale size={18} />
                                    {t('cta.legal')}
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
}

export default About;
