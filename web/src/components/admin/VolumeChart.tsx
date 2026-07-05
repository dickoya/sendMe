import { useTranslation } from 'react-i18next';
import Card from '@/components/ui/Card';
import { formatCurrency } from '@/utils/formatCurrency';

import type { VolumeDataPoint } from '@/types';

interface VolumeChartProps {

  data: VolumeDataPoint[];
}

/**
 * Graphique en barres simple du volume de transactions par jour.
 * Utilise du CSS pur (pas de librairie externe) pour rester léger.
 */
export default function VolumeChart({ data }: VolumeChartProps) {
  const { t } = useTranslation('admin');

  if (!data.length) {
    return (
      <Card title={t('dashboard.volumeChart.title')}>
        <p className="py-8 text-center text-sm text-gray-400">{t('dashboard.volumeChart.noData')}</p>
      </Card>
    );
  }

  const maxVolume = Math.max(...data.map((d) => d.volume));

  return (
    <Card title={t('dashboard.volumeChart.title')}>
      <div className="flex items-end gap-1.5" style={{ height: 160 }}>
        {data.map((point) => {
          const height = maxVolume > 0 ? (point.volume / maxVolume) * 100 : 0;
          const date = new Date(point.date);
          const label = date.toLocaleDateString(undefined, { day: '2-digit', month: '2-digit' });

          return (
            <div
              key={point.date}
              className="group relative flex flex-1 flex-col items-center"
              style={{ height: '100%' }}
            >
              {/* Barre */}
              <div
                className="mt-auto w-full rounded-t bg-accent-400 transition-all hover:bg-accent-500"
                style={{ height: `${Math.max(height, 2)}%` }}
              >
                {/* Tooltip au survol */}
                <div className="pointer-events-none absolute -top-8 left-1/2 z-10 -translate-x-1/2 whitespace-nowrap rounded bg-gray-900 px-2 py-1 text-xs text-white opacity-0 shadow-lg transition-opacity group-hover:opacity-100">
                  <div>{formatCurrency(point.volume, 'EUR')}</div>
                  <div className="text-gray-300">{point.transactionCount} tx</div>

                </div>
              </div>
              {/* Label date */}
              <span className="mt-1 text-[10px] text-gray-400">{label}</span>
            </div>
          );
        })}
      </div>
    </Card>
  );
}
