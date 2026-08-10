import { useTranslation } from 'react-i18next';
import { getChannelLabel, getChannelIcon } from '@/utils/constants';
import type { ChannelType } from '@/types';

export interface ChannelChartProps {
    data: Record<string, number>;
}

const ALL_CHANNELS: ChannelType[] = [
    'BANK_TRANSFER',
    'ORANGE_MONEY',
    'MTN_MOBILE_MONEY',
    'WAVE',
    'MOOV_MONEY',
    'AIRTEL_MONEY',
];

/**
 * Graphique en barres horizontales de la répartition par canal.
 *
 * Libellés via getChannelLabel (namespace "common", déjà utilisé
 * auparavant) ; "Aucune donnée disponible" via common.states.noData
 * (clé existante depuis le début — pas de nouvelle clé nécessaire).
 */
function ChannelChart({ data }: ChannelChartProps) {
    const { t } = useTranslation('common');
    const maxValue = Math.max(...Object.values(data), 1);
    const total = Object.values(data).reduce((sum, v) => sum + v, 0);

    if (total === 0) {
        return <p className="py-6 text-center text-sm text-gray-400 dark:text-[#5A5E72]">{t('states.noData')}</p>;
    }

    return (
        <div className="flex flex-col gap-3">
            {ALL_CHANNELS.map((channel) => {
                const count = data[channel] ?? 0;
                const percentage = maxValue > 0 ? (count / maxValue) * 100 : 0;

                return (
                    <div key={channel} className="flex items-center gap-3">
                        <div className="flex w-36 flex-shrink-0 items-center gap-2 text-sm text-gray-700 dark:text-[#C0C4D0]">
                            <span>{getChannelIcon(channel)}</span>
                            <span className="truncate">{getChannelLabel(channel, t)}</span>
                        </div>
                        <div className="flex-1">
                            <div className="h-2.5 w-full overflow-hidden rounded-full bg-gray-100 dark:bg-[#222222]">
                                <div
                                    className="h-full rounded-full bg-brand-500 transition-all"
                                    style={{ width: `${percentage}%` }}
                                />
                            </div>
                        </div>
                        <span className="w-10 flex-shrink-0 text-right text-sm font-medium text-gray-900 dark:text-[#EDEEF0]">
                            {count}
                        </span>
                    </div>
                );
            })}
        </div>
    );
}

export default ChannelChart;
