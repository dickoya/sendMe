import { useTranslation } from 'react-i18next';
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';
import Card from '@/components/ui/Card';
import type { LiveExchangeRate, ExchangeRateHistoryPoint } from '@/services/dashboardService';

interface ExchangeRateWidgetProps {
    exchangeRates: LiveExchangeRate[];
    rateHistory: ExchangeRateHistoryPoint[];
    selectedPair: { from: string; to: string };
    onSelectPair: (pair: { from: string; to: string }) => void;
    isLoading: boolean;
}

const PAIRS = [
    // EUR → toutes
    { from: 'EUR', to: 'XOF', label: 'EUR → XOF' },
    { from: 'EUR', to: 'XAF', label: 'EUR → XAF' },
    { from: 'EUR', to: 'GNF', label: 'EUR → GNF' },
    { from: 'EUR', to: 'NGN', label: 'EUR → NGN' },
    { from: 'EUR', to: 'GHS', label: 'EUR → GHS' },
    { from: 'EUR', to: 'ZAR', label: 'EUR → ZAR' },
    { from: 'EUR', to: 'MAD', label: 'EUR → MAD' },
    { from: 'EUR', to: 'TND', label: 'EUR → TND' },
    { from: 'EUR', to: 'USD', label: 'EUR → USD' },
    { from: 'EUR', to: 'GBP', label: 'EUR → GBP' },
    // USD → africaines
    { from: 'USD', to: 'XOF', label: 'USD → XOF' },
    { from: 'USD', to: 'GNF', label: 'USD → GNF' },
    { from: 'USD', to: 'NGN', label: 'USD → NGN' },
    { from: 'USD', to: 'GHS', label: 'USD → GHS' },
    { from: 'USD', to: 'ZAR', label: 'USD → ZAR' },
    { from: 'USD', to: 'EUR', label: 'USD → EUR' },
    // GBP → africaines
    { from: 'GBP', to: 'XOF', label: 'GBP → XOF' },
    { from: 'GBP', to: 'GNF', label: 'GBP → GNF' },
    { from: 'GBP', to: 'NGN', label: 'GBP → NGN' },
    { from: 'GBP', to: 'EUR', label: 'GBP → EUR' },
    // CAD → africaines
    { from: 'CAD', to: 'XOF', label: 'CAD → XOF' },
    { from: 'CAD', to: 'GNF', label: 'CAD → GNF' },
    { from: 'CAD', to: 'EUR', label: 'CAD → EUR' },
    // CHF → africaines
    { from: 'CHF', to: 'XOF', label: 'CHF → XOF' },
    { from: 'CHF', to: 'GNF', label: 'CHF → GNF' },
    { from: 'CHF', to: 'EUR', label: 'CHF → EUR' },
];


/**
 * Widget affichant les taux de change en direct avec un graphique d'évolution.
 */
export default function ExchangeRateWidget({
    exchangeRates,
    rateHistory,
    selectedPair,
    onSelectPair,
    isLoading,
}: ExchangeRateWidgetProps) {
    const { t } = useTranslation('dashboard');

    const currentRate = exchangeRates.find(
        (r) => r.from === selectedPair.from && r.to === selectedPair.to
    );

    const chartData = rateHistory.map((point) => ({
        date: new Date(point.date).toLocaleDateString('fr-FR', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
        }),
        taux: point.rate,
    }));

    const CustomTooltip = ({ active, payload, label }: any) => {
        if (active && payload && payload.length) {
            return (
                <div className="rounded-lg border border-gray-200 bg-white p-3 shadow-lg dark:border-[#333] dark:bg-[#1a1a1a]">
                    <p className="text-xs text-gray-500 dark:text-gray-400">{label}</p>
                    <p className="text-sm font-semibold text-gray-900 dark:text-[#EDEEF0]">
                        {payload[0].value.toFixed(6)}
                    </p>
                </div>
            );
        }
        return null;
    };

    return (
        <Card title={t('charts.exchangeRates')} className="lg:col-span-2">
            {/* Sélecteur de paire */}
            <div className="mb-4 flex flex-wrap gap-2">
                {PAIRS.map((pair) => (
                    <button
                        key={`${pair.from}-${pair.to}`}
                        onClick={() => onSelectPair({ from: pair.from, to: pair.to })}
                        className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                            selectedPair.from === pair.from && selectedPair.to === pair.to
                                ? 'bg-brand-600 text-white'
                                : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-[#222] dark:text-gray-400 dark:hover:bg-[#333]'
                        }`}
                    >
                        {pair.label}
                    </button>
                ))}
            </div>

            {/* Taux actuel */}
            {currentRate && (
                <div className="mb-4 rounded-lg bg-gray-50 p-3 dark:bg-[#1a1a1a]">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-xs text-gray-500 dark:text-gray-400">
                                {t('charts.currentRate')}
                            </p>
                            <p className="text-2xl font-bold text-gray-900 dark:text-[#EDEEF0]">
                                1 {currentRate.from} = {currentRate.rate.toFixed(4)} {currentRate.to}
                            </p>
                        </div>
                        <div className="text-right">
                            <p className="text-xs text-gray-500 dark:text-gray-400">
                                {t('charts.rawRate')}
                            </p>
                            <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                                {currentRate.rawRate.toFixed(4)}
                            </p>
                        </div>
                    </div>
                </div>
            )}

            {/* Graphique d'évolution */}
            {isLoading ? (
                <div className="flex h-48 items-center justify-center">
                    <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-600 border-t-transparent" />
                </div>
            ) : chartData.length > 0 ? (
                <div className="h-48">
                    <ResponsiveContainer width="100%" height="100%">
                        <LineChart data={chartData} margin={{ top: 5, right: 20, left: 10, bottom: 5 }}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                            <XAxis
                                dataKey="date"
                                tick={{ fontSize: 10, fill: '#6b7280' }}
                                axisLine={{ stroke: '#e5e7eb' }}
                            />
                            <YAxis
                                domain={['auto', 'auto']}
                                tick={{ fontSize: 10, fill: '#6b7280' }}
                                axisLine={{ stroke: '#e5e7eb' }}
                                tickFormatter={(val) => val.toFixed(2)}
                            />
                            <Tooltip content={<CustomTooltip />} />
                            <Line
                                type="monotone"
                                dataKey="taux"
                                stroke="#2563eb"
                                strokeWidth={2}
                                dot={false}
                                activeDot={{ r: 4, fill: '#2563eb' }}
                            />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            ) : (
                <div className="flex h-48 items-center justify-center text-sm text-gray-500">
                    {t('empty.noRateData')}
                </div>
            )}
        </Card>
    );
}
