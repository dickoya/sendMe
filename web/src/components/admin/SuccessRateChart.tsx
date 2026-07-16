import { useTranslation } from 'react-i18next';
import Card from '@/components/ui/Card';

import type { SuccessRateDataPoint } from '@/types';

interface SuccessRateChartProps {

  data: SuccessRateDataPoint[];
}

/**
 * Graphique linéaire simple de l'évolution du taux de succès.
 * Utilise du SVG inline (pas de librairie externe).
 */
export default function SuccessRateChart({ data }: SuccessRateChartProps) {
  const { t } = useTranslation('admin');

  if (!data.length) {
    return (
      <Card title={t('dashboard.successRateChart.title')}>
        <p className="py-8 text-center text-sm text-gray-400 dark:text-[#5A5E72]">{t('dashboard.successRateChart.noData')}</p>
      </Card>
    );
  }

  const width = 600;
  const height = 180;
  const padding = { top: 20, right: 20, bottom: 30, left: 40 };
  const chartW = width - padding.left - padding.right;
  const chartH = height - padding.top - padding.bottom;

  const maxRate = 100;
  const minRate = 0;

  const points = data.map((d, i) => ({
    x: padding.left + (i / Math.max(data.length - 1, 1)) * chartW,
    y: padding.top + chartH - ((d.successRate - minRate) / (maxRate - minRate)) * chartH,
    ...d,
  }));

  const linePath = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');

  // Graduations Y
  const yTicks = [0, 25, 50, 75, 100];

  return (
    <Card title={t('dashboard.successRateChart.title')}>
      <svg width="100%" height={height} viewBox={`0 0 ${width} ${height}`} className="overflow-visible">
        {/* Grille Y */}
        {yTicks.map((tick) => {
          const y = padding.top + chartH - ((tick - minRate) / (maxRate - minRate)) * chartH;
          return (
            <g key={tick}>
              <line x1={padding.left} y1={y} x2={width - padding.right} y2={y} stroke="var(--color-border)" strokeWidth="1" />
              <text x={padding.left - 8} y={y + 4} textAnchor="end" className="text-[10px]" fill="var(--color-text-tertiary)">
                {tick}%
              </text>
            </g>
          );
        })}

        {/* Ligne */}
        <path d={linePath} fill="none" stroke="#22C55E" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />

        {/* Points */}
        {points.map((p) => (
          <g key={p.date}>
            <circle cx={p.x} cy={p.y} r="3" fill="#22C55E" stroke="#fff" strokeWidth="1.5">
              <title>
                {new Date(p.date).toLocaleDateString()} — {p.successRate.toFixed(1)}% ({p.completed}/{p.total})
              </title>

            </circle>
          </g>
        ))}

        {/* Labels X (tous les N points) */}
        {points
          .filter((_, i) => i % Math.max(1, Math.floor(data.length / 7)) === 0)
          .map((p) => (
            <text
              key={p.date}
              x={p.x}
              y={height - 5}
              textAnchor="middle"
              className="text-[10px]"
              fill="var(--color-text-tertiary)"
            >
              {new Date(p.date).toLocaleDateString(undefined, { day: '2-digit', month: '2-digit' })}
            </text>
          ))}
      </svg>
    </Card>
  );
}
