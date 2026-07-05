import { useTranslation } from 'react-i18next';
import Card from '@/components/ui/Card';
import { getChannelLabel } from '@/utils/constants';

interface ChannelPieChartProps {
  data: Record<string, number>;
}

const CHANNEL_COLORS: Record<string, string> = {
  ORANGE_MONEY: '#FF6B00',
  MTN_MOBILE_MONEY: '#FFCC00',
  WAVE: '#00D4AA',
  MOOV_MONEY: '#003DA5',
  AIRTEL_MONEY: '#E30613',
  BANK_TRANSFER: '#6366F1',
};

/**
 * Graphique en camembert simple pour la répartition par canal.
 * Utilise un SVG inline (pas de librairie externe).
 */
export default function ChannelPieChart({ data }: ChannelPieChartProps) {
  const { t } = useTranslation('admin');
  const { t: tCommon } = useTranslation('common');

  const entries = Object.entries(data).filter(([, value]) => value > 0);
  const total = entries.reduce((sum, [, value]) => sum + value, 0);

  if (!entries.length) {
    return (
      <Card title={t('dashboard.channelChart.title')}>
        <p className="py-8 text-center text-sm text-gray-400">{t('dashboard.channelChart.noData')}</p>
      </Card>
    );
  }

  // Générer les arcs du camembert SVG
  let cumulativeAngle = 0;
  const slices = entries.map(([channel, count]) => {
    const angle = (count / total) * 360;
    const startAngle = cumulativeAngle;
    cumulativeAngle += angle;
    return { channel, count, percentage: (count / total) * 100, startAngle, angle };
  });

  return (
    <Card title={t('dashboard.channelChart.title')}>
      <div className="flex flex-col items-center gap-4 sm:flex-row">
        {/* Camembert SVG */}
        <svg width="140" height="140" viewBox="0 0 140 140" className="shrink-0">
          {slices.map((slice) => {
            const [startX, startY] = polarToCartesian(70, 70, 60, slice.startAngle);
            const [endX, endY] = polarToCartesian(70, 70, 60, slice.startAngle + slice.angle);
            const largeArc = slice.angle > 180 ? 1 : 0;
            const color = CHANNEL_COLORS[slice.channel] || '#94A3B8';

            return (
              <path
                key={slice.channel}
                d={`M 70 70 L ${startX} ${startY} A 60 60 0 ${largeArc} 1 ${endX} ${endY} Z`}
                fill={color}
                stroke="#fff"
                strokeWidth="1.5"
              >
                <title>
                  {getChannelLabel(slice.channel as import('@/types').ChannelType, tCommon)} — {slice.count} ({slice.percentage.toFixed(1)}%)
                </title>

              </path>
            );
          })}
          <circle cx="70" cy="70" r="30" fill="white" />
        </svg>

        {/* Légende */}
        <div className="flex flex-col gap-2">
          {slices.map((slice) => {
            const color = CHANNEL_COLORS[slice.channel] || '#94A3B8';
            return (
              <div key={slice.channel} className="flex items-center gap-2 text-sm">
                <span
                  className="inline-block h-3 w-3 rounded-full"
                  style={{ backgroundColor: color }}
                />
                <span className="text-gray-700 dark:text-gray-300">
                  {getChannelLabel(slice.channel as import('@/types').ChannelType, tCommon)}
                </span>
                <span className="font-medium text-gray-900 dark:text-gray-100">
                  {slice.percentage.toFixed(1)}%
                </span>
              </div>
            );
          })}
        </div>
      </div>
    </Card>
  );
}

function polarToCartesian(cx: number, cy: number, r: number, angleDeg: number): [number, number] {
  const angleRad = ((angleDeg - 90) * Math.PI) / 180;
  return [cx + r * Math.cos(angleRad), cy + r * Math.sin(angleRad)];
}
