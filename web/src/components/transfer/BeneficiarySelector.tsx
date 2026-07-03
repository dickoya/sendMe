import { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Plus, Smartphone, Landmark, ChevronDown, ChevronUp } from 'lucide-react';
import { cn } from '@/lib/utils';
import Avatar from '@/components/ui/Avatar';
import SearchInput from '@/components/ui/SearchInput';
import { getChannelLabel, getChannelIcon } from '@/utils/constants';
import type { Beneficiary } from '@/types';

const INITIAL_DISPLAY_COUNT = 5;

export interface BeneficiarySelectorProps {
    beneficiaries: Beneficiary[];
    value: number | null;
    onChange: (id: number) => void;
}

/**
 * Sélecteur de bénéficiaire avec recherche textuelle et pagination locale.
 *
 * - Barre de recherche filtrant par prénom, nom, canal, devise
 * - Affiche les 5 premiers résultats + bouton "Voir tout" si > 5
 * - Scroll fluide vers le bénéficiaire sélectionné
 */
function BeneficiarySelector({ beneficiaries, value, onChange }: BeneficiarySelectorProps) {
    const { t } = useTranslation('transfer');
    const { t: tCommon } = useTranslation('common');
    const [search, setSearch] = useState('');
    const [showAll, setShowAll] = useState(false);

    // Filtrer les bénéficiaires selon la recherche
    const filtered = useMemo(() => {
        if (!search.trim()) return beneficiaries;

        const q = search.toLowerCase().trim();
        return beneficiaries.filter((b) => {
            const fullName = `${b.firstName} ${b.lastName}`.toLowerCase();
            const channelNames = b.channels.map((ch) => ch.channel.toLowerCase()).join(' ');
            const currencies = b.channels.map((ch) => ch.currency.toLowerCase()).join(' ');
            const phones = b.channels.map((ch) => ch.phoneNumber || '').join(' ');
            const banks = b.channels.map((ch) => ch.bankName || '').join(' ');

            return (
                fullName.includes(q) ||
                channelNames.includes(q) ||
                currencies.includes(q) ||
                phones.includes(q) ||
                banks.includes(q)
            );
        });
    }, [beneficiaries, search]);

    // Bénéficiaires visibles (limités ou tout)
    const visible = showAll ? filtered : filtered.slice(0, INITIAL_DISPLAY_COUNT);
    const hasMore = filtered.length > INITIAL_DISPLAY_COUNT;

    if (beneficiaries.length === 0) {
        return (
            <div className="flex flex-col items-center gap-2 rounded-xl border border-dashed border-gray-300 p-6 text-center">
                <p className="text-sm text-gray-500">{t('form.noBeneficiary')}</p>
                <Link
                    to="/beneficiaries"
                    className="flex items-center gap-1 text-sm font-medium text-brand-600 hover:underline"
                >
                    <Plus size={14} /> {t('form.addBeneficiaryLink')}
                </Link>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">{t('form.beneficiaryLabel')}</label>

            {/* Barre de recherche */}
            <SearchInput
                value={search}
                onChange={(value) => {
                    setSearch(value);
                    setShowAll(false);
                }}
                placeholder={t('form.searchBeneficiary')}
            />

            {/* Liste filtrée */}
            <div className="flex flex-col gap-2">
                {filtered.length === 0 ? (
                    <p className="py-3 text-center text-sm text-gray-400">{t('form.noSearchResults')}</p>
                ) : (
                    <>
                        {visible.map((b) => {
                            const primaryChannel = b.channels.find((ch) => ch.isPrimary) || b.channels[0];
                            const channelCount = b.channels.length;

                            return (
                                <button
                                    key={b.id}
                                    type="button"
                                    onClick={() => onChange(b.id)}
                                    className={cn(
                                        'flex items-center gap-3 rounded-xl border p-3 text-left transition-all',
                                        value === b.id
                                            ? 'border-brand-500 bg-brand-50 ring-2 ring-brand-100'
                                            : 'border-gray-200 bg-white hover:border-gray-300'
                                    )}
                                >
                                    <Avatar firstName={b.firstName} lastName={b.lastName} size="sm" />
                                    <div className="min-w-0 flex-1">
                                        <p className="truncate text-sm font-medium text-gray-900">
                                            {b.firstName} {b.lastName}
                                        </p>
                                        <div className="flex items-center gap-1.5 text-xs text-gray-500">
                                            {primaryChannel ? (
                                                <>
                                                    <span>{getChannelIcon(primaryChannel.channel)}</span>
                                                    <span>{getChannelLabel(primaryChannel.channel, tCommon)}</span>
                                                    {primaryChannel.channel === 'BANK_TRANSFER' ? (
                                                        <Landmark size={10} />
                                                    ) : (
                                                        <Smartphone size={10} />
                                                    )}
                                                    <span>•</span>
                                                    <span className="font-mono">{primaryChannel.currency}</span>
                                                </>
                                            ) : (
                                                <span>{getChannelIcon(b.channel)} {getChannelLabel(b.channel, tCommon)}</span>
                                            )}
                                            {channelCount > 1 && (
                                                <span className="ml-1 rounded-full bg-gray-100 px-1.5 py-0.5 text-[10px] text-gray-500">
                                                    +{channelCount - 1}
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                </button>
                            );
                        })}

                        {/* Bouton Voir plus / Voir moins */}
                        {hasMore && (
                            <button
                                type="button"
                                onClick={() => setShowAll(!showAll)}
                                className="flex items-center justify-center gap-1 rounded-xl border border-gray-200 py-2 text-sm text-gray-500 hover:bg-gray-50 transition-colors"
                            >
                                {showAll ? (
                                    <>
                                        <ChevronUp size={16} />
                                        {t('form.showLess')}
                                    </>
                                ) : (
                                    <>
                                        <ChevronDown size={16} />
                                        {t('form.showMore', { count: filtered.length - INITIAL_DISPLAY_COUNT })}
                                    </>
                                )}
                            </button>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}

export default BeneficiarySelector;
