import { type ReactNode } from 'react';
import Card from '@/components/ui/Card';

interface StatsCardProps {
    title: string;
    value: string;
    subtitle?: string;
    icon: ReactNode;
    trend?: {
        direction: 'up' | 'down' | 'neutral';
        label: string;
    };
    isLoading?: boolean;
}

/**
 * Carte de statistique pour le tableau de bord.
 * Affiche une valeur, un libellé et une tendance optionnelle.
 */
export default function StatsCard({ title, value, subtitle, icon, trend, isLoading }: StatsCardProps) {
    if (isLoading) {
        return (
            <Card>
                <div className="flex animate-pulse items-start gap-4">
                    <div className="h-10 w-10 rounded-lg bg-gray-200 dark:bg-[#333]" />
                    <div className="flex-1 space-y-2">
                        <div className="h-3 w-24 rounded bg-gray-200 dark:bg-[#333]" />
                        <div className="h-5 w-32 rounded bg-gray-200 dark:bg-[#333]" />
                        <div className="h-3 w-20 rounded bg-gray-200 dark:bg-[#333]" />
                    </div>
                </div>
            </Card>
        );
    }

    return (
        <Card>
            <div className="flex items-start gap-4">
                <div className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-lg bg-brand-50 text-brand-600 dark:bg-brand-900/30 dark:text-brand-400">
                    {icon}
                </div>
                <div className="min-w-0 flex-1">
                    <p className="text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-300">
                        {title}
                    </p>
                    <p className="mt-1 text-xl font-bold text-gray-900 dark:text-[#EDEEF0] truncate">
                        {value}
                    </p>
                    {subtitle && (
                        <p className="mt-0.5 text-xs text-gray-500 dark:text-gray-300">
                            {subtitle}
                        </p>
                    )}
                    {trend && (
                        <p className={`mt-1 text-xs font-medium ${
                            trend.direction === 'up'
                                ? 'text-green-600 dark:text-green-400'
                                : trend.direction === 'down'
                                ? 'text-red-600 dark:text-red-400'
                                : 'text-gray-500 dark:text-gray-300'
                        }`}>
                            {trend.label}
                        </p>
                    )}
                </div>
            </div>
        </Card>
    );
}
