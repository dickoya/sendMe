import { useTranslation } from 'react-i18next';
import { formatCurrency } from '@/utils/formatCurrency';
import { getChannelLabel, getChannelIcon } from '@/utils/constants';
import type { Beneficiary, SimulateResponse, BeneficiaryChannel } from '@/types';

export interface TransferSummaryProps {
    beneficiary: Beneficiary;
    simulation: SimulateResponse;
    currencyFrom: string;
    /** Canal de réception sélectionné pour ce transfert (optionnel) */
    selectedChannel?: BeneficiaryChannel;
}

/**
 * Récapitulatif complet du transfert affiché avant le paiement.
 *
 * Migration i18n (Phase 8) : réutilise les clés "transfer.simulator.*"
 * pour les libellés financiers (déjà traduits en Phase 2 — éviter la
 * duplication entre simulateur et récapitulatif), et "transfer.summary.*"
 * pour les éléments propres à cette étape.
 */
function TransferSummary({ beneficiary, simulation, currencyFrom, selectedChannel }: TransferSummaryProps) {
    const { t } = useTranslation('transfer');
    const { t: tCommon } = useTranslation('common');

    // Utiliser le canal sélectionné s'il est fourni, sinon le canal principal ou le premier
    const displayChannel = selectedChannel || beneficiary.channels.find((ch) => ch.isPrimary) || beneficiary.channels[0];

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
                        {displayChannel ? (
                            <>{getChannelIcon(displayChannel.channel)} {getChannelLabel(displayChannel.channel, tCommon)} • {displayChannel.currency}</>
                        ) : (
                            <>{getChannelIcon(beneficiary.channel)} {getChannelLabel(beneficiary.channel, tCommon)}</>
                        )}
                    </p>
                </div>
            </div>

            {/* Détails financiers */}
            <div className="flex flex-col gap-2 rounded-xl bg-gray-50 p-4 text-sm">
                <div className="flex justify-between">
                    <span className="text-gray-500">{t('simulator.amountSent')}</span>
                    <span className="font-medium text-gray-900">
            {formatCurrency(simulation.amountSent, currencyFrom)}
          </span>
                </div>
                <div className="flex justify-between">
          <span className="text-gray-500">
            {t('simulator.fees', { rate: (simulation.feeRate * 100).toFixed(1) })}
          </span>
                    <span className="text-gray-500">
            {formatCurrency(simulation.fees, currencyFrom)}
          </span>
                </div>
                {simulation.discount && simulation.discount > 0 && (
                    <div className="flex justify-between">
                        <span className="text-success-600 font-medium">{t('simulator.discount')}</span>
                        <span className="text-success-600 font-medium">- {formatCurrency(simulation.discount, currencyFrom)}</span>
                    </div>
                )}
                <div className="flex justify-between">
                    <span className="text-gray-500">{t('simulator.exchangeRate')}</span>
                    <span className="text-gray-500">
            {t('simulator.exchangeRateValue', {
                from: currencyFrom,
                rate: simulation.exchangeRate,
                to: simulation.currencyTo,
            })}
          </span>
                </div>
            </div>

            {/* Total + montant reçu */}
            <div className="flex flex-col gap-2">
                <div className="flex items-center justify-between rounded-lg bg-brand-600 px-4 py-3">
                    <span className="text-sm font-medium text-white">{t('summary.totalToPay')}</span>
                    <span className="text-lg font-bold text-white">
            {formatCurrency(simulation.totalDebited, currencyFrom)}
          </span>
                </div>
                <div className="flex items-center justify-between rounded-lg bg-accent-50 px-4 py-3">
          <span className="text-sm font-medium text-accent-600">
            {t('summary.willReceive', { name: beneficiary.firstName })}
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