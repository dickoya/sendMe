import { useTranslation } from 'react-i18next';
import { Pencil, Trash2, Landmark, Smartphone, Globe } from 'lucide-react';
import { getChannelLabel, getChannelIcon } from '@/utils/constants';
import type { Beneficiary, BeneficiaryChannel } from '@/types';

export interface BeneficiaryCardProps {
    beneficiary: Beneficiary;
    onEdit: () => void;
    onDelete: () => void;
}

/**
 * Carte affichant un bénéficiaire avec ses canaux de réception
 * et les actions modifier/supprimer.
 *
 * Un bénéficiaire peut avoir 1 ou plusieurs canaux (ex: Orange Money + Wave).
 * Chaque canal affiche ses détails spécifiques et sa devise.
 */
function BeneficiaryCard({ beneficiary, onEdit, onDelete }: BeneficiaryCardProps) {
    const { t: tCommon } = useTranslation('common');

    return (
        <div className="flex items-start justify-between gap-4 rounded-xl border border-gray-100 p-4 transition-shadow hover:shadow-sm">
            <div className="flex items-start gap-3 min-w-0 flex-1">
                <div className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full bg-brand-50 text-lg font-semibold text-brand-700">
                    {beneficiary.firstName[0]}
                    {beneficiary.lastName[0]}
                </div>

                <div className="min-w-0 flex-1">
                    <p className="truncate font-medium text-gray-900">
                        {beneficiary.firstName} {beneficiary.lastName}
                    </p>

                    {/* Liste des canaux */}
                    <div className="mt-2 flex flex-col gap-1.5">
                        {beneficiary.channels.map((ch) => (
                            <ChannelBadge key={ch.id} channel={ch} />
                        ))}
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
 * Badge affichant un canal de réception avec ses détails.
 */
function ChannelBadge({ channel }: { channel: BeneficiaryChannel }) {
    const { t: tCommon } = useTranslation('common');
    const isBank = channel.channel === 'BANK_TRANSFER';

    return (
        <div className="flex items-center gap-2 text-sm text-gray-500">
            <span>{getChannelIcon(channel.channel)}</span>
            <span className="font-medium text-gray-700">
                {getChannelLabel(channel.channel, tCommon)}
            </span>
            <span className="text-gray-300">•</span>
            {isBank ? (
                <span className="flex items-center gap-1 truncate">
                    <Landmark size={12} />
                    {channel.bankName ? `${channel.bankName} — ` : ''}
                    {maskIban(channel.iban)}
                </span>
            ) : (
                <span className="flex items-center gap-1 truncate">
                    <Smartphone size={12} />
                    {channel.phoneNumber}
                </span>
            )}
            {channel.countryCode && (
                <>
                    <span className="text-gray-300">•</span>
                    <span className="flex items-center gap-1">
                        <Globe size={12} />
                        {channel.countryCode}
                    </span>
                </>
            )}
            <span className="text-gray-300">•</span>
            <span className="font-mono text-xs text-brand-600">{channel.currency}</span>
            {channel.isPrimary && (
                <span className="ml-1 rounded-full bg-brand-50 px-2 py-0.5 text-[10px] font-medium text-brand-700">
                    Principal
                </span>
            )}
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
