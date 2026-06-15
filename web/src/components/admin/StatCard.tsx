import { cn } from '@/lib/utils';

export interface StatCardProps {
    label: string;
    value: string | number;
    icon: React.ReactNode;
    iconClassName?: string;
    subtext?: string;
}

/**
 * Carte de statistique pour le tableau de bord admin.
 *
 * Pas de migration i18n directe — `label` et `subtext` sont fournis
 * déjà traduits par le composant appelant (AdminDashboard, Phase 11).
 * Inchangé depuis l'Étape 9.
 */
function StatCard({ label, value, icon, iconClassName, subtext }: StatCardProps) {
    return (
        <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
            <div className="flex items-center justify-between">
                <span className="text-sm text-gray-500">{label}</span>
                <div className={cn('flex h-9 w-9 items-center justify-center rounded-full', iconClassName ?? 'bg-brand-50 text-brand-600')}>
                    {icon}
                </div>
            </div>
            <p className="mt-2 text-2xl font-bold text-gray-900">{value}</p>
            {subtext && <p className="mt-1 text-xs text-gray-400">{subtext}</p>}
        </div>
    );
}

export default StatCard;