import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import {
    Search, ChevronDown, ChevronUp, Mail, MessageSquare, HelpCircle,
    Info, DollarSign, Send, Users, ShieldCheck, Settings
} from 'lucide-react';
import { cn } from '@/lib/utils';

interface FaqItem {
    id: string;
    category: string;
    question: string;
    answer: string;
}

const CATEGORY_ICONS: Record<string, any> = {
    general: HelpCircle,
    transfers: Send,
    beneficiaries: Users,
    security: ShieldCheck,
    fees: DollarSign,
};

function Faq() {
    const { t } = useTranslation('faq');
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedCategory, setSelectedCategory] = useState('all');
    const [expandedItem, setExpandedItem] = useState<string | null>(null);

    // Charger les questions depuis les traductions de manière sécurisée
    const questions = useMemo<FaqItem[]>(() => {
        const list = t('questions', { returnObjects: true });
        if (Array.isArray(list)) {
            return list as FaqItem[];
        }
        return [];
    }, [t]);

    const categories = useMemo(() => {
        return [
            { id: 'all', label: t('categories.all'), icon: HelpCircle },
            { id: 'general', label: t('categories.general'), icon: Info },
            { id: 'transfers', label: t('categories.transfers'), icon: Send },
            { id: 'beneficiaries', label: t('categories.beneficiaries'), icon: Users },
            { id: 'security', label: t('categories.security'), icon: ShieldCheck },
            { id: 'fees', label: t('categories.fees'), icon: DollarSign },
        ];
    }, [t]);

    // Filtrage des questions par recherche et catégorie
    const filteredQuestions = useMemo(() => {
        return questions.filter((item) => {
            const matchesCategory = selectedCategory === 'all' || item.category === selectedCategory;
            const matchesSearch =
                item.question.toLowerCase().includes(searchQuery.toLowerCase()) ||
                item.answer.toLowerCase().includes(searchQuery.toLowerCase());
            return matchesCategory && matchesSearch;
        });
    }, [questions, selectedCategory, searchQuery]);

    const toggleItem = (id: string) => {
        setExpandedItem(expandedItem === id ? null : id);
    };

    return (
        <div className="mx-auto max-w-4xl space-y-8 py-4 animate-fade-in">
            {/* Header / Intro */}
            <div className="text-center space-y-3">
                <h1 className="text-3xl font-extrabold tracking-tight text-gray-900 dark:text-[#EDEEF0] sm:text-4xl">
                    {t('title')}
                </h1>
                <p className="mx-auto max-w-2xl text-base text-gray-500 dark:text-[#8B8FA3]">
                    {t('subtitle')}
                </p>
            </div>

            {/* Barre de recherche */}
            <div className="relative mx-auto max-w-2xl">
                <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-4 text-gray-400 dark:text-[#8B8FA3]">
                    <Search size={20} />
                </div>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder={t('searchPlaceholder')}
                    className="w-full rounded-2xl border border-gray-100 bg-white py-3.5 pl-12 pr-4 text-gray-900 shadow-sm transition-all placeholder:text-gray-400 focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20 dark:border-[#222222] dark:bg-[#121212] dark:text-[#EDEEF0] dark:placeholder:text-[#555555] dark:focus:border-brand-500"
                />
            </div>

            {/* Catégories de filtrage */}
            <div className="flex flex-wrap justify-center gap-2.5">
                {categories.map((cat) => {
                    const Icon = cat.icon;
                    const isActive = selectedCategory === cat.id;
                    return (
                        <button
                            key={cat.id}
                            onClick={() => {
                                setSelectedCategory(cat.id);
                                setExpandedItem(null);
                            }}
                            className={cn(
                                "flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-medium transition-all duration-200",
                                isActive
                                    ? "bg-brand-600 text-white shadow-md dark:bg-brand-500 dark:shadow-[0_0_16px_rgba(90,159,224,0.3)]"
                                    : "bg-white border border-gray-100 text-gray-600 hover:bg-gray-50 dark:bg-[#121212] dark:border-[#222222] dark:text-[#8B8FA3] dark:hover:bg-[#1A1A1A] dark:hover:text-[#EDEEF0]"
                            )}
                        >
                            <Icon size={16} />
                            {cat.label}
                        </button>
                    );
                })}
            </div>

            {/* Accordéon FAQ */}
            <div className="space-y-3.5">
                {filteredQuestions.length > 0 ? (
                    filteredQuestions.map((item) => {
                        const isExpanded = expandedItem === item.id;
                        const CategoryIcon = CATEGORY_ICONS[item.category] || HelpCircle;
                        return (
                            <div
                                key={item.id}
                                className={cn(
                                    "overflow-hidden rounded-2xl border transition-all duration-200 bg-white dark:bg-[#121212]",
                                    isExpanded
                                        ? "border-brand-100 shadow-sm ring-1 ring-brand-100/30 dark:border-brand-500/30"
                                        : "border-gray-100 hover:border-gray-200 hover:shadow-sm dark:border-[#222222] dark:hover:border-[#333333]"
                                )}
                            >
                                <button
                                    onClick={() => toggleItem(item.id)}
                                    className="flex w-full items-center justify-between p-5 text-left transition-colors duration-150"
                                >
                                    <div className="flex items-center gap-4 pr-4">
                                        <div className={cn(
                                            "flex h-10 w-10 shrink-0 items-center justify-center rounded-xl transition-colors",
                                            isExpanded
                                                ? "bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-400"
                                                : "bg-gray-50 text-gray-500 dark:bg-[#1A1A1A] dark:text-[#8B8FA3]"
                                        )}>
                                            <CategoryIcon size={18} />
                                        </div>
                                        <span className="font-semibold text-gray-900 dark:text-[#EDEEF0] sm:text-base">
                                            {item.question}
                                        </span>
                                    </div>
                                    <div className={cn(
                                        "text-gray-400 transition-transform duration-200 dark:text-[#8B8FA3]",
                                        isExpanded ? "rotate-180 text-brand-600 dark:text-brand-400" : ""
                                    )}>
                                        <ChevronDown size={18} />
                                    </div>
                                </button>
                                <div
                                    className={cn(
                                        "grid transition-all duration-300 ease-in-out",
                                        isExpanded ? "grid-rows-[1fr] opacity-100" : "grid-rows-[0fr] opacity-0"
                                    )}
                                >
                                    <div className="overflow-hidden">
                                        <div className="border-t border-gray-50 p-5 pt-0 text-sm leading-relaxed text-gray-600 dark:border-[#1A1A1A] dark:text-[#8B8FA3] sm:text-base">
                                            <p className="pt-4">{item.answer}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })
                ) : (
                    <div className="text-center py-12 px-4 rounded-2xl bg-gray-50 border border-gray-100 dark:bg-[#121212] dark:border-[#222222] space-y-2">
                        <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400 dark:bg-[#1A1A1A] dark:text-[#555555]">
                            <Search size={22} />
                        </div>
                        <h3 className="text-lg font-semibold text-gray-900 dark:text-[#EDEEF0]">{t('noResults')}</h3>
                        <p className="text-sm text-gray-500 dark:text-[#8B8FA3] max-w-md mx-auto">{t('noResultsSub')}</p>
                    </div>
                )}
            </div>

            {/* CTA Contact support */}
            <div className="rounded-2xl border border-gray-100 bg-gradient-to-r from-brand-50 to-indigo-50/50 p-6 sm:p-8 dark:border-[#222222] dark:from-[#111622] dark:to-[#0D0D0D]">
                <div className="flex flex-col items-center justify-between gap-6 sm:flex-row">
                    <div className="space-y-2 text-center sm:text-left">
                        <h3 className="text-lg font-bold text-gray-900 dark:text-[#EDEEF0]">
                            {t('contactTitle')}
                        </h3>
                        <p className="text-sm text-gray-600 dark:text-[#8B8FA3] max-w-md">
                            {t('contactSubtitle')}
                        </p>
                    </div>
                    <a
                        href="mailto:support@sendme.com"
                        className="flex items-center gap-2 rounded-xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white shadow-md hover:bg-brand-700 transition-colors dark:bg-brand-500 dark:hover:bg-brand-600"
                    >
                        <Mail size={16} />
                        {t('contactButton')}
                    </a>
                </div>
            </div>
        </div>
    );
}

export default Faq;
