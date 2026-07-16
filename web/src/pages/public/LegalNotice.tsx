import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import {
    Scale,
    Search,
    Building2,
    Landmark,
    ShieldCheck,
    FileCheck,
    Cookie,
    FileText,
    Euro,
    Lock,
    ShieldAlert,
    MessageSquare,
    ChevronDown,
    ChevronUp,
    ExternalLink,
    Mail,
    AlertTriangle,
} from 'lucide-react';

/**
 * Icônes Lucide associées à chaque section.
 */
const iconMap: Record<string, React.ElementType> = {
    Building2,
    Landmark,
    ShieldCheck,
    FileCheck,
    Cookie,
    FileText,
    Euro,
    Lock,
    ShieldAlert,
    MessageSquare,
};

/**
 * Page "Informations légales" — design moderne et professionnel
 * pour un service de transfert d'argent (type Wise / Revolut).
 */
function LegalNotice() {
    const { t, i18n } = useTranslation('legal');
    const [search, setSearch] = useState('');
    const [activeCategory, setActiveCategory] = useState('all');
    const [expandedSections, setExpandedSections] = useState<Set<string>>(new Set());

    const sections = t('sections', { returnObjects: true }) as Array<{
        id: string;
        category: string;
        title: string;
        icon: string;
        content: Array<{ subtitle: string; text: string }>;
    }>;

    const categories = [
        { key: 'all', label: t('categories.all') },
        { key: 'general', label: t('categories.general') },
        { key: 'personalData', label: t('categories.personalData') },
        { key: 'cookies', label: t('categories.cookies') },
        { key: 'terms', label: t('categories.terms') },
        { key: 'privacy', label: t('categories.privacy') },
        { key: 'aml', label: t('categories.aml') },
        { key: 'complaints', label: t('categories.complaints') },
    ];

    const filteredSections = useMemo(() => {
        return sections.filter((section) => {
            const matchesCategory = activeCategory === 'all' || section.category === activeCategory;
            const matchesSearch =
                search === '' ||
                section.title.toLowerCase().includes(search.toLowerCase()) ||
                section.content.some(
                    (c) =>
                        c.subtitle.toLowerCase().includes(search.toLowerCase()) ||
                        c.text.toLowerCase().includes(search.toLowerCase())
                );
            return matchesCategory && matchesSearch;
        });
    }, [sections, activeCategory, search]);

    const toggleSection = (id: string) => {
        setExpandedSections((prev) => {
            const next = new Set(prev);
            if (next.has(id)) {
                next.delete(id);
            } else {
                next.add(id);
            }
            return next;
        });
    };

    return (
        <div className="min-h-screen bg-gradient-to-b from-gray-50 to-white dark:from-[#0D0D0D] dark:to-[#111111]">
            {/* Hero */}
            <section className="relative overflow-hidden border-b border-gray-100 bg-white pb-16 pt-12 dark:border-[#222222] dark:bg-[#0D0D0D]">
                {/* Decorative background */}
                <div className="pointer-events-none absolute inset-0 overflow-hidden">
                    <div className="absolute -right-40 -top-40 h-80 w-80 rounded-full bg-brand-50/50 blur-3xl dark:bg-brand-900/10" />
                    <div className="absolute -bottom-40 -left-40 h-80 w-80 rounded-full bg-blue-50/50 blur-3xl dark:bg-blue-900/10" />
                </div>

                <div className="relative mx-auto max-w-4xl px-4 text-center sm:px-6 lg:px-8">
                    <div className="mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-50 shadow-[0_0_20px_rgba(90,159,224,0.15)] dark:bg-[#222222] dark:shadow-[0_0_20px_rgba(90,159,224,0.08)]">
                        <Scale className="h-8 w-8 text-brand-600 dark:text-brand-400" />
                    </div>
                    <h1 className="text-3xl font-bold tracking-tight text-gray-900 dark:text-[#EDEEF0] sm:text-4xl">
                        {t('page.title')}
                    </h1>
                    <p className="mx-auto mt-4 max-w-2xl text-base leading-relaxed text-gray-500 dark:text-[#8B8FA3]">
                        {t('page.subtitle')}
                    </p>
                    <p className="mt-3 text-sm text-gray-400 dark:text-[#5A5E72]">
                        {t('page.lastUpdate')}
                    </p>
                </div>
            </section>

            {/* Barre de recherche + Filtres */}
            <section className="sticky top-0 z-30 border-b border-gray-100 bg-white/80 backdrop-blur-lg dark:border-[#222222] dark:bg-[#0D0D0D]/80">
                <div className="mx-auto max-w-4xl px-4 py-4 sm:px-6 lg:px-8">
                    {/* Search */}
                    <div className="relative mb-4">
                        <Search className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 dark:text-[#5A5E72]" size={18} />
                        <input
                            type="text"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            placeholder={t('page.searchPlaceholder')}
                            className="w-full rounded-xl border border-gray-200 bg-gray-50 py-2.5 pl-10 pr-4 text-sm text-gray-900 placeholder-gray-400 transition-colors focus:border-brand-400 focus:outline-none focus:ring-2 focus:ring-brand-100 dark:border-[#333333] dark:bg-[#1A1A1A] dark:text-[#EDEEF0] dark:placeholder-[#5A5E72] dark:focus:border-brand-500 dark:focus:ring-brand-900/30"
                        />
                    </div>

                    {/* Category pills */}
                    <div className="flex flex-wrap gap-2">
                        {categories.map((cat) => (
                            <button
                                key={cat.key}
                                onClick={() => setActiveCategory(cat.key)}
                                className={`rounded-full px-3.5 py-1.5 text-xs font-medium transition-all duration-200 ${
                                    activeCategory === cat.key
                                        ? 'bg-brand-600 text-white shadow-sm dark:bg-brand-500'
                                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-[#222222] dark:text-[#8B8FA3] dark:hover:bg-[#333333]'
                                }`}
                            >
                                {cat.label}
                            </button>
                        ))}
                    </div>
                </div>
            </section>

            {/* Contenu principal */}
            <section className="mx-auto max-w-4xl px-4 py-8 sm:px-6 lg:px-8">
                {filteredSections.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-20 text-center">
                        <AlertTriangle className="mb-4 h-12 w-12 text-gray-300 dark:text-[#333333]" />
                        <p className="text-lg font-medium text-gray-900 dark:text-[#EDEEF0]">
                            {t('page.noResults')}
                        </p>
                        <p className="mt-1 text-sm text-gray-500 dark:text-[#8B8FA3]">
                            {t('page.noResultsSub')}
                        </p>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {filteredSections.map((section) => {
                            const Icon = iconMap[section.icon] || FileText;
                            const isExpanded = expandedSections.has(section.id);

                            return (
                                <div
                                    key={section.id}
                                    className="group overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-sm transition-all duration-200 hover:shadow-md dark:border-[#222222] dark:bg-[#0D0D0D] dark:hover:border-[#333333]"
                                >
                                    {/* Header (cliquable) */}
                                    <button
                                        onClick={() => toggleSection(section.id)}
                                        className="flex w-full items-center gap-4 px-6 py-5 text-left transition-colors hover:bg-gray-50/50 dark:hover:bg-[#1A1A1A]/50"
                                    >
                                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-600 dark:bg-[#222222] dark:text-brand-400">
                                            <Icon size={20} />
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <h2 className="text-base font-semibold text-gray-900 dark:text-[#EDEEF0]">
                                                {section.title}
                                            </h2>
                                            <p className="mt-0.5 text-xs text-gray-400 dark:text-[#5A5E72]">
                                                {categories.find((c) => c.key === section.category)?.label}
                                            </p>
                                        </div>
                                        {isExpanded ? (
                                            <ChevronUp size={18} className="shrink-0 text-gray-400 dark:text-[#5A5E72]" />
                                        ) : (
                                            <ChevronDown size={18} className="shrink-0 text-gray-400 dark:text-[#5A5E72]" />
                                        )}
                                    </button>

                                    {/* Contenu déplié */}
                                    {isExpanded && (
                                        <div className="border-t border-gray-100 px-6 py-5 dark:border-[#222222]">
                                            <div className="space-y-5">
                                                {section.content.map((item, idx) => (
                                                    <div key={idx}>
                                                        <h3 className="mb-2 text-sm font-semibold text-gray-700 dark:text-[#C0C4D0]">
                                                            {item.subtitle}
                                                        </h3>
                                                        <p
                                                            className="text-sm leading-relaxed text-gray-600 dark:text-[#8B8FA3] [&_strong]:font-semibold [&_strong]:text-gray-900 dark:[&_strong]:text-[#EDEEF0]"
                                                            dangerouslySetInnerHTML={{ __html: item.text }}
                                                        />
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                )}

                {/* Disclaimer */}
                <div className="mt-10 rounded-2xl border border-amber-100 bg-amber-50/50 px-6 py-5 dark:border-amber-900/30 dark:bg-amber-900/10">
                    <div className="flex items-start gap-3">
                        <AlertTriangle size={18} className="mt-0.5 shrink-0 text-amber-600 dark:text-amber-400" />
                        <p className="text-sm leading-relaxed text-amber-800 dark:text-amber-300">
                            {t('footer.disclaimer')}
                        </p>
                    </div>
                </div>

                {/* CTA Contact */}
                <div className="mt-8 rounded-2xl border border-gray-100 bg-white p-8 text-center shadow-sm dark:border-[#222222] dark:bg-[#0D0D0D]">
                    <Mail className="mx-auto mb-4 h-10 w-10 text-brand-600 dark:text-brand-400" />
                    <h3 className="text-lg font-semibold text-gray-900 dark:text-[#EDEEF0]">
                        {t('footer.contact')}
                    </h3>
                    <p className="mt-1 text-sm text-gray-500 dark:text-[#8B8FA3]">
                        {t('footer.contactSub')}
                    </p>
                    <a
                        href="mailto:legal@sendme.com"
                        className="mt-5 inline-flex items-center gap-2 rounded-xl bg-brand-600 px-6 py-2.5 text-sm font-medium text-white shadow-sm transition-all hover:bg-brand-700 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2 dark:bg-brand-500 dark:hover:bg-brand-600"
                    >
                        {t('footer.contactButton')}
                        <ExternalLink size={16} />
                    </a>
                </div>
            </section>
        </div>
    );
}

export default LegalNotice;
