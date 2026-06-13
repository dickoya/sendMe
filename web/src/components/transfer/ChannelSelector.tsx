import { cn } from '@/lib/utils';
import { CHANNEL_LABELS, CHANNEL_ICONS } from '@/utils/constants';
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
 * Affiche les 6 canaux sous forme de cartes cliquables avec icône.
 */
function ChannelSelector({ value, onChange }: ChannelSelectorProps) {
    return (
        <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">Canal de réception</label>
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
                        <span className="text-2xl">{CHANNEL_ICONS[channel]}</span>
                        <span className="text-[11px] font-medium leading-tight text-gray-700">
              {CHANNEL_LABELS[channel]}
            </span>
                    </button>
                ))}
            </div>
        </div>
    );
}

export default ChannelSelector;