import Spinner from '@/components/ui/Spinner';
import { formatCurrency } from '@/utils/formatCurrency';
import type { SimulateResponse } from '@/types';

export interface FeeSimulatorProps {
    simulation: SimulateResponse | null;
    isLoading: boolean;
    currencyFrom: string;
}

/**
 * Affiche le récapitulatif en temps réel du calcul des frais
 * (montant envoyé, frais, taux de change, montant reçu, total débité).
 *
 * Affiche un état de chargement pendant l'appel debounced à /simulate.
 */
function FeeSimulator({ simulation, isLoading, currencyFrom }: FeeSimulatorProps) {
    if (isLoading) {
        return (
            <div className="flex items-center justify-center rounded-xl bg-gray-50 p-6">
                <Spinner size="sm" label="Calcul en cours..." />
            </div>
        );
    }

    if (!simulation) {
        return (
            <div className="rounded-xl bg-gray-50 p-6 text-center text-sm text-gray-400">
                Entrez un montant pour voir le récapitulatif
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-3 rounded-xl bg-gray-50 p-4">
            <Row label="Montant envoyé" value={formatCurrency(simulation.amountSent, currencyFrom)} />
            <Row
                label={`Frais plateforme (${(simulation.feeRate * 100).toFixed(1)}%)`}
                value={`- ${formatCurrency(simulation.fees, currencyFrom)}`}
                muted
            />
            <Row
                label="Taux de change"
                value={`1 ${currencyFrom} = ${simulation.exchangeRate} ${simulation.currencyTo}`}
                muted
            />

            <div className="h-px bg-gray-200" />

            {/* Total débité — mis en évidence */}
            <div className="flex items-center justify-between rounded-lg bg-brand-600 px-4 py-3">
                <span className="text-sm font-medium text-white">Total débité</span>
                <span className="text-base font-bold text-white">
          {formatCurrency(simulation.totalDebited, currencyFrom)}
        </span>
            </div>

            {/* Montant reçu — mis en évidence en vert */}
            <div className="flex items-center justify-between rounded-lg bg-accent-50 px-4 py-3">
                <span className="text-sm font-medium text-accent-600">Montant reçu</span>
                <span className="text-base font-bold text-accent-600">
          {formatCurrency(simulation.amountReceived, simulation.currencyTo)}
        </span>
            </div>
        </div>
    );
}

function Row({ label, value, muted }: { label: string; value: string; muted?: boolean }) {
    return (
        <div className="flex items-center justify-between text-sm">
            <span className={muted ? 'text-gray-500' : 'text-gray-700'}>{label}</span>
            <span className={muted ? 'text-gray-500' : 'font-medium text-gray-900'}>{value}</span>
        </div>
    );
}

export default FeeSimulator;