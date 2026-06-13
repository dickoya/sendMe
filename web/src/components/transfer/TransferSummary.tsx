import { formatCurrency } from '@/utils/formatCurrency';
import { CHANNEL_LABELS, CHANNEL_ICONS } from '@/utils/constants';
import type { Beneficiary, SimulateResponse } from '@/types';

export interface TransferSummaryProps {
    beneficiary: Beneficiary;
    simulation: SimulateResponse;
    currencyFrom: string;
}

/**
 * Récapitulatif complet du transfert affiché avant le paiement
 * — bénéficiaire, montants, frais, taux et montant reçu.
 */
function TransferSummary({ beneficiary, simulation, currencyFrom }: TransferSummaryProps) {
    return (
        <div className="flex flex-col gap-4">
            {/* Bénéficiaire */}
            <div className="flex items-center gap-3 rounded-xl border border-gray-100 p-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700">
                    {beneficiary.firstName[0]}{beneficiary.lastName[0]}
                </div>
                <div>
                    <p className="text-sm font-medium text-gray-900">
                        {beneficiary.firstName} {beneficiary.lastName}
                    </p>
                    <p className="text-xs text-gray-500">
                        {CHANNEL_ICONS[beneficiary.channel]} {CHANNEL_LABELS[beneficiary.channel]}
                    </p>
                </div>
            </div>

            {/* Détails financiers */}
            <div className="flex flex-col gap-2 rounded-xl bg-gray-50 p-4 text-sm">
                <div className="flex justify-between">
                    <span className="text-gray-500">Montant envoyé</span>
                    <span className="font-medium text-gray-900">
            {formatCurrency(simulation.amountSent, currencyFrom)}
          </span>
                </div>
                <div className="flex justify-between">
                    <span className="text-gray-500">Frais ({(simulation.feeRate * 100).toFixed(1)}%)</span>
                    <span className="text-gray-500">
            {formatCurrency(simulation.fees, currencyFrom)}
          </span>
                </div>
                <div className="flex justify-between">
                    <span className="text-gray-500">Taux de change</span>
                    <span className="text-gray-500">
            1 {currencyFrom} = {simulation.exchangeRate} {simulation.currencyTo}
          </span>
                </div>
            </div>

            {/* Total + montant reçu */}
            <div className="flex flex-col gap-2">
                <div className="flex items-center justify-between rounded-lg bg-brand-600 px-4 py-3">
                    <span className="text-sm font-medium text-white">Total à payer</span>
                    <span className="text-lg font-bold text-white">
            {formatCurrency(simulation.totalDebited, currencyFrom)}
          </span>
                </div>
                <div className="flex items-center justify-between rounded-lg bg-accent-50 px-4 py-3">
          <span className="text-sm font-medium text-accent-600">
            {beneficiary.firstName} recevra
          </span>
                    <span className="text-lg font-bold text-accent-600">
            {formatCurrency(simulation.amountReceived, simulation.currencyTo)}
          </span>
                </div>
            </div>
        </div>
    );
}

export default TransferSummary;