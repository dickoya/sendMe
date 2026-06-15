import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils';
import { getChannelLabel, getChannelIcon } from '@/utils/constants';
import type { ChannelType } from '@/types';

const CHANNELS: ChannelType[] = [
    'BANK_TRANSFER',
    'ORANGE_MONEY',
    'MTN_MOBILE_MONEY',
    'WAVE',
    'MOOV_MONEY',
    'AIRTEL_MONEY',
];

export interface ChannelSelectorProps {
    value: ChannelType;
    onChange: (channel: ChannelType) => void;
}

/**
 * Grille de sélection visuelle des canaux de réception.
 *
 * Migration i18n (Phase 7) : libellés via getChannelLabel(channel, t)
 * (namespace "common"), label du champ via "beneficiaries.form.channelLabel".
 */
function ChannelSelector({ value, onChange }: ChannelSelectorProps) {
    const { t: tCommon } = useTranslation('common');
    const { t } = useTranslation('beneficiaries');

    return (
        <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">{t('form.channelLabel')}</label>
            <div className="grid grid-cols-3 gap-2 sm:grid-cols-6">
                {CHANNELS.map((channel) => (
                    <button
                        key={channel}
                        type="button"
                        onClick={() => onChange(channel)}
                        className={cn(
                            'flex flex-col items-center gap-1.5 rounded-xl border p-3 text-center transition-all',
                            value === channel
                                ? 'border-brand-500 bg-brand-50 ring-2 ring-brand-100'
                                : 'border-gray-200 bg-white hover:border-gray-300'
                        )}
                    >
                        <span className="text-2xl">{getChannelIcon(channel)}</span>
                        <span className="text-[11px] font-medium leading-tight text-gray-700">
              {getChannelLabel(channel, tCommon)}
            </span>
                    </button>
                ))}
            </div>
        </div>
    );
}

export default ChannelSelector;