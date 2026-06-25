import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
    BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
    Legend,
} from 'recharts';
import Card from '@/components/ui/Card';
import type { TransferEvolution } from '@/services/dashboardService';
import { formatCurrency } from '@/utils/formatCurrency';

interface TransferEvolutionChartProps {
    evolution: TransferEvolution | null;
    isLoading: boolean;
}

/**
 * Graphique d'évolution des transferts (mensuel/annuel).
 * Permet de basculer entre vue mensuelle et annuelle.
 */
export default function TransferEvolutionChart({ evolution, isLoading }: TransferEvolutionChartProps) {
    const { t } = useTranslation('dashboard');
    const [view, setView] = useState<'monthly' | 'yearly'>('monthly');

    if (isLoading) {
        return (
            <Card title={t('charts.transferEvolution')} className="lg:col-span-2">
                <div className="flex h-64 items-center justify-center">
                    <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-600 border-t-transparent" />
                </div>
            </Card>
        );
    }

    if (!evolution || (evolution.monthly.length === 0 && evolution.yearly.length === 0)) {
        return (
            <Card title={t('charts.transferEvolution')} className="lg:col-span-2">
                <div className="flex h-64 items-center justify-center text-sm text-gray-500">
                    {t('empty.noData')}
                </div>
            </Card>
        );
    }

    const monthlyData = evolution.monthly.map((m) => ({
        label: m.month,
        Montant: m.totalAmount,
        Transactions: m.count,
    }));

    const yearlyData = evolution.yearly.map((y) => ({
        label: String(y.year),
        Montant: y.totalAmount,
        Transactions: y.count,
    }));

    const data = view === 'monthly' ? monthlyData : yearlyData;

    const CustomTooltip = ({ active, payload, label }: any) => {
        if (active && payload && payload.length) {
            return (
                <div className="rounded-lg border border-gray-200 bg-white p-3 shadow-lg dark:border-[#333] dark:bg-[#1a1a1a]">
                    <p className="mb-1 text-sm font-medium text-gray-900 dark:text-[#EDEEF0]">{label}</p>
                    {payload.map((entry: any, idx: number) => (
                        <p key={idx} className="text-sm" style={{ color: entry.color }}>
                            {entry.name === 'Montant'
                                ? `${t('charts.amount')}: ${formatCurrency(entry.value, 'EUR')}`
                                : `${t('charts.count')}: ${entry.value}`}
                        </p>
                    ))}
                </div>
            );
        }
        return null;
    };

    return (
        <Card title={t('charts.transferEvolution')} className="lg:col-span-2">
            {/* Toggle mensuel/annuel */}
            <div className="mb-4 flex gap-2">
                <button
                    onClick={() => setView('monthly')}
                    className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                        view === 'monthly'
                            ? 'bg-brand-600 text-white'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-[#222] dark:text-gray-300 dark:hover:bg-[#333]'
                    }`}
                >
                    {t('charts.monthly')}
                </button>
                <button
                    onClick={() => setView('yearly')}
                    className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                        view === 'yearly'
                            ? 'bg-brand-600 text-white'
                            : 'bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-[#222] dark:text-gray-300 dark:hover:bg-[#333]'
                    }`}
                >
                    {t('charts.yearly')}
                </button>
            </div>

            <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={data} margin={{ top: 5, right: 20, left: 10, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                        <XAxis
                            dataKey="label"
                            tick={{ fontSize: 12, fill: '#6b7280' }}
                            axisLine={{ stroke: '#e5e7eb' }}
                        />
                        <YAxis
                            yAxisId="left"
                            tick={{ fontSize: 12, fill: '#6b7280' }}
                            axisLine={{ stroke: '#e5e7eb' }}
                        />
                        <YAxis
                            yAxisId="right"
                            orientation="right"
                            tick={{ fontSize: 12, fill: '#6b7280' }}
                            axisLine={{ stroke: '#e5e7eb' }}
                        />
                        <Tooltip content={<CustomTooltip />} />
                        <Legend />
                        <Bar
                            yAxisId="left"
                            dataKey="Montant"
                            fill="#2563eb"
                            radius={[4, 4, 0, 0]}
                            name={t('charts.amount')}
                        />
                        <Bar
                            yAxisId="right"
                            dataKey="Transactions"
                            fill="#10b981"
                            radius={[4, 4, 0, 0]}
                            name={t('charts.count')}
                        />
                    </BarChart>
                </ResponsiveContainer>
            </div>
        </Card>
    );
}
