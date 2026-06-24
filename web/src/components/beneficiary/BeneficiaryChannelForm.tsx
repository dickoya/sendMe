import { useTranslation } from 'react-i18next';
import { X, Landmark, Hash, Globe, Smartphone } from 'lucide-react';
import Input from '@/components/ui/Input';
import ChannelSelector from '@/components/transfer/ChannelSelector';
import { COUNTRIES } from '@/utils/constants';
import type { BeneficiaryChannelFormData, ChannelType } from '@/types';

export interface BeneficiaryChannelFormProps {
    index: number;
    data: BeneficiaryChannelFormData;
    onChange: (index: number, data: BeneficiaryChannelFormData) => void;
    onRemove: (index: number) => void;
    canRemove: boolean;
}

/**
 * Formulaire pour un canal de réception d'un bénéficiaire.
 * 
 * - BANK_TRANSFER : affiche les champs banque (nom, IBAN, BIC)
 * - Mobile Money : affiche le numéro de téléphone et le pays
 * 
 * Le pays détermine la devise de réception (ex: GIN → GNF, SEN → XOF).
 */
function BeneficiaryChannelForm({
    index,
    data,
    onChange,
    onRemove,
    canRemove,
}: BeneficiaryChannelFormProps) {
    const { t } = useTranslation('beneficiaries');
    const isBank = data.channel === 'BANK_TRANSFER';

    const update = (partial: Partial<BeneficiaryChannelFormData>) => {
        onChange(index, { ...data, ...partial });
    };

    return (
        <div className="rounded-lg border border-gray-200 bg-gray-50 p-4">
            <div className="mb-3 flex items-center justify-between">
                <span className="text-sm font-medium text-gray-700">
                    Canal {index + 1}
                </span>
                {canRemove && (
                    <button
                        type="button"
                        onClick={() => onRemove(index)}
                        className="rounded-lg p-1 text-gray-400 hover:bg-danger-50 hover:text-danger-600"
                        aria-label="Supprimer ce canal"
                    >
                        <X size={16} />
                    </button>
                )}
            </div>

            <div className="flex flex-col gap-3">
                {/* Sélecteur de canal */}
                <ChannelSelector
                    value={data.channel}
                    onChange={(ch) =>
                        update({
                            channel: ch,
                            // Réinitialiser les champs spécifiques au changement de type
                            phoneNumber: undefined,
                            countryCode: undefined,
                            bankName: undefined,
                            iban: undefined,
                            bic: undefined,
                        })
                    }
                />

                {isBank ? (
                    <>
                        <Input
                            label={t('form.bankName')}
                            placeholder={t('form.bankNamePlaceholder')}
                            icon={<Landmark size={16} />}
                            value={data.bankName || ''}
                            onChange={(e) => update({ bankName: e.target.value })}
                            required
                        />
                        <Input
                            label={t('form.iban')}
                            placeholder={t('form.ibanPlaceholder')}
                            icon={<Hash size={16} />}
                            value={data.iban || ''}
                            onChange={(e) => update({ iban: e.target.value.toUpperCase() })}
                            required
                        />
                        <Input
                            label={t('form.bic')}
                            placeholder={t('form.bicPlaceholder')}
                            value={data.bic || ''}
                            onChange={(e) => update({ bic: e.target.value.toUpperCase() })}
                            required
                        />
                    </>
                ) : (
                    <>
                        <Input
                            label={t('form.phoneNumber')}
                            type="tel"
                            placeholder={t('form.phoneNumberPlaceholder')}
                            icon={<Smartphone size={16} />}
                            value={data.phoneNumber || ''}
                            onChange={(e) => update({ phoneNumber: e.target.value })}
                            required
                        />

                        {/* Sélecteur de pays pour déterminer la devise */}
                        <div>
                            <label className="mb-1 block text-sm font-medium text-gray-700">
                                Pays du numéro
                            </label>
                            <div className="relative">
                                <Globe
                                    size={16}
                                    className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
                                />
                                <select
                                    value={data.countryCode || ''}
                                    onChange={(e) => update({ countryCode: e.target.value })}
                                    className="w-full rounded-lg border border-gray-300 py-2.5 pl-10 pr-3 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-200"
                                >
                                    <option value="">Sélectionner un pays</option>
                                    {COUNTRIES.map((c) => (
                                        <option key={c.code} value={c.code}>
                                            {c.name} ({c.dialCode}) — {c.currency}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            {data.countryCode && (
                                <p className="mt-1 text-xs text-gray-500">
                                    Devise : <span className="font-mono font-medium text-brand-600">
                                        {COUNTRIES.find((c) => c.code === data.countryCode)?.currency || '—'}
                                    </span>
                                </p>
                            )}
                        </div>
                    </>
                )}

                {/* Canal principal */}
                <label className="flex items-center gap-2 text-sm text-gray-600">
                    <input
                        type="checkbox"
                        checked={data.isPrimary}
                        onChange={(e) => update({ isPrimary: e.target.checked })}
                        className="rounded border-gray-300 text-brand-600 focus:ring-brand-500"
                    />
                    Canal de réception principal
                </label>
            </div>
        </div>
    );
}

export default BeneficiaryChannelForm;
