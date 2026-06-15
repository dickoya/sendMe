import { useTranslation } from 'react-i18next';
import { CHANNEL_ICONS, getChannelLabel } from '@/utils/constants';
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
 * Graphique en barres horizontales de la répartition des transactions par canal.
 * Implémenté en CSS pur — pas de dépendance de charting.
 */
function ChannelChart({ data }: ChannelChartProps) {
    const { t } = useTranslation('common'); // ◄── Initialisation de la traduction (Namespace common)
    const maxValue = Math.max(...Object.values(data), 1);
    const total = Object.values(data).reduce((sum, v) => sum + v, 0);

    if (total === 0) {
        return <p className="py-6 text-center text-sm text-gray-400">Aucune donnée disponible</p>;
    }

    return (
        <div className="flex flex-col gap-3">
            {ALL_CHANNELS.map((channel) => {
                const count = data[channel] ?? 0;
                const percentage = maxValue > 0 ? (count / maxValue) * 100 : 0;

                return (
                    <div key={channel} className="flex items-center gap-3">
                        <div className="flex w-36 flex-shrink-0 items-center gap-2 text-sm text-gray-700">
                            <span>{CHANNEL_ICONS[channel]}</span>
                            {/* ◄── Utilisation de la fonction dynamique getChannelLabel ici */}
                            <span className="truncate">{getChannelLabel(channel, t)}</span>
                        </div>
                        <div className="flex-1">
                            <div className="h-2.5 w-full overflow-hidden rounded-full bg-gray-100">
                                <div
                                    className="h-full rounded-full bg-brand-500 transition-all"
                                    style={{ width: `${percentage}%` }}
                                />
                            </div>
                        </div>
                        <span className="w-10 flex-shrink-0 text-right text-sm font-medium text-gray-900">
                            {count}
                        </span>
                    </div>
                );
            })}
        </div>
    );
}

export default ChannelChart;