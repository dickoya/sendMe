import { useTranslation } from 'react-i18next';
import Spinner from '@/components/ui/Spinner';
import { formatCurrency } from '@/utils/formatCurrency';
import type { SimulateResponse } from '@/types';

export interface FeeSimulatorProps {
    simulation: SimulateResponse | null;
    isLoading: boolean;
    currencyFrom: string;
}

/**
 * Affiche le récapitulatif en temps réel du calcul des frais.
 *
 * Migration i18n (Phase 8) : namespace "transfer" (clés simulator.*).
 * Le taux de frais est interpolé en pourcentage déjà formaté
 * (ex: "1.5"), la clé contient le symbole "%".
 */
function FeeSimulator({ simulation, isLoading, currencyFrom }: FeeSimulatorProps) {
    const { t } = useTranslation('transfer');

    if (isLoading) {
        return (
            <div className="flex items-center justify-center rounded-xl bg-gray-50 p-6">
                <Spinner size="sm" label={t('simulator.calculating')} />
            </div>
        );
    }

    if (!simulation) {
        return (
            <div className="rounded-xl bg-gray-50 p-6 text-center text-sm text-gray-400">
                {t('simulator.emptyHint')}
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-3 rounded-xl bg-gray-50 p-4">
            <Row label={t('simulator.amountSent')} value={formatCurrency(simulation.amountSent, currencyFrom)} />
            <Row
                label={t('simulator.fees', { rate: (simulation.feeRate * 100).toFixed(1) })}
                value={`- ${formatCurrency(simulation.fees, currencyFrom)}`}
                muted
            />
            {simulation.discount && simulation.discount > 0 && (
                <Row
                    label={t('simulator.discount')}
                    value={`- ${formatCurrency(simulation.discount, currencyFrom)}`}
                    discount
                />
            )}
            <Row
                label={t('simulator.exchangeRate')}
                value={t('simulator.exchangeRateValue', {
                    from: currencyFrom,
                    rate: simulation.exchangeRate,
                    to: simulation.currencyTo,
                })}
                muted
            />

            <div className="h-px bg-gray-200" />

            {/* Total débité — mis en évidence */}
            <div className="flex items-center justify-between rounded-lg bg-brand-600 px-4 py-3">
                <span className="text-sm font-medium text-white">{t('simulator.totalDebited')}</span>
                <span className="text-base font-bold text-white">
          {formatCurrency(simulation.totalDebited, currencyFrom)}
        </span>
            </div>

            {/* Montant reçu — mis en évidence en vert */}
            <div className="flex items-center justify-between rounded-lg bg-accent-50 px-4 py-3">
                <span className="text-sm font-medium text-accent-600">{t('simulator.amountReceived')}</span>
                <span className="text-base font-bold text-accent-600">
          {formatCurrency(simulation.amountReceived, simulation.currencyTo)}
        </span>
            </div>
        </div>
    );
}

function Row({ label, value, muted, discount }: { label: string; value: string; muted?: boolean; discount?: boolean }) {
    return (
        <div className="flex items-center justify-between text-sm">
            <span className={discount ? 'text-success-600 font-medium' : muted ? 'text-gray-500' : 'text-gray-700'}>{label}</span>
            <span className={discount ? 'text-success-600 font-medium' : muted ? 'text-gray-500' : 'font-medium text-gray-900'}>{value}</span>
        </div>
    );
}

export default FeeSimulator;