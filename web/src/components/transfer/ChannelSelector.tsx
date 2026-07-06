import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils';
import { getChannelLabel } from '@/utils/constants';
import { channelService } from '@/services/channelService';
import PaymentChannelLogo from '@/components/admin/PaymentChannelLogo';
import type { ChannelType } from '@/types';
import type { PaymentChannel } from '@/types/paymentChannel';

/**
 * Liste statique de secours (fallback) si l'appel API échoue.
 */
const FALLBACK_CHANNELS: ChannelType[] = [
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
 * Récupère dynamiquement la liste des canaux actifs depuis l'API.
 * Si l'appel échoue, utilise une liste statique de secours.
 * Affiche le vrai logo de chaque opérateur.
 */
function ChannelSelector({ value, onChange }: ChannelSelectorProps) {
    const { t: tCommon } = useTranslation('common');
    const { t } = useTranslation('beneficiaries');
    const [channels, setChannels] = useState<ChannelType[]>(FALLBACK_CHANNELS);

    useEffect(() => {
        let cancelled = false;

        channelService.getActive()
            .then((activeChannels) => {
                if (!cancelled && activeChannels.length > 0) {
                    const types = activeChannels.map((ch: PaymentChannel) => ch.channelType as ChannelType);
                    setChannels(types);
                }
            })
            .catch(() => {
                // Fallback sur la liste statique en cas d'erreur
                if (!cancelled) {
                    setChannels(FALLBACK_CHANNELS);
                }
            });

        return () => {
            cancelled = true;
        };
    }, []);

    return (
        <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">{t('form.channelLabel')}</label>
            <div className="grid grid-cols-3 gap-2 sm:grid-cols-6">
                {channels.map((channel) => (
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
                        <PaymentChannelLogo channelType={channel} size={36} />
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
