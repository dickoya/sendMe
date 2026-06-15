import { useTranslation } from 'react-i18next';
import { Pencil, Trash2, Landmark, Smartphone } from 'lucide-react';
import { getChannelLabel, getChannelIcon } from '@/utils/constants';
import type { Beneficiary } from '@/types';

export interface BeneficiaryCardProps {
    beneficiary: Beneficiary;
    onEdit: () => void;
    onDelete: () => void;
}

/**
 * Carte affichant un bénéficiaire avec ses informations
 * de réception et les actions modifier/supprimer.
 *
 * Migration i18n (Phase 7) : libellé de canal via getChannelLabel,
 * aria-labels des boutons d'action via "common".
 */
function BeneficiaryCard({ beneficiary, onEdit, onDelete }: BeneficiaryCardProps) {
    const { t: tCommon } = useTranslation('common');
    const isBank = beneficiary.channel === 'BANK_TRANSFER';

    return (
        <div className="flex items-center justify-between gap-4 rounded-xl border border-gray-100 p-4 transition-shadow hover:shadow-sm">
            <div className="flex items-center gap-3 min-w-0">
                <div className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full bg-brand-50 text-lg font-semibold text-brand-700">
                    {beneficiary.firstName[0]}
                    {beneficiary.lastName[0]}
                </div>

                <div className="min-w-0">
                    <p className="truncate font-medium text-gray-900">
                        {beneficiary.firstName} {beneficiary.lastName}
                    </p>
                    <div className="flex items-center gap-1.5 text-sm text-gray-500">
                        <span>{getChannelIcon(beneficiary.channel)}</span>
                        <span>{getChannelLabel(beneficiary.channel, tCommon)}</span>
                        <span className="text-gray-300">•</span>
                        {isBank ? (
                            <span className="flex items-center gap-1 truncate">
                <Landmark size={12} />
                                {maskIban(beneficiary.bankDetails?.iban)}
              </span>
                        ) : (
                            <span className="flex items-center gap-1 truncate">
                <Smartphone size={12} />
                                {beneficiary.mobileDetails?.phoneNumber}
              </span>
                        )}
                    </div>
                </div>
            </div>

            {/* Actions */}
            <div className="flex flex-shrink-0 gap-1">
                <button
                    onClick={onEdit}
                    className="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-brand-600"
                    aria-label={tCommon('actions.edit')}
                >
                    <Pencil size={16} />
                </button>
                <button
                    onClick={onDelete}
                    className="rounded-lg p-2 text-gray-400 hover:bg-danger-50 hover:text-danger-600"
                    aria-label={tCommon('actions.delete')}
                >
                    <Trash2 size={16} />
                </button>
            </div>
        </div>
    );
}

/**
 * Masque un IBAN pour l'affichage : FR76 **** **** 0189
 */
function maskIban(iban?: string): string {
    if (!iban || iban.length < 8) return '••••';
    return `${iban.slice(0, 4)} •••• •••• ${iban.slice(-4)}`;
}

export default BeneficiaryCard;