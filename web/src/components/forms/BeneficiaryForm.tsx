import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, User } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import BeneficiaryChannelForm from '@/components/beneficiary/BeneficiaryChannelForm';
import { getErrorMessage } from '@/lib/utils';
import type { Beneficiary, BeneficiaryFormData, BeneficiaryChannelFormData } from '@/types';

export interface BeneficiaryFormProps {
    initialData?: Beneficiary;
    onSubmit: (payload: BeneficiaryFormData) => Promise<unknown>;
    onCancel: () => void;
}

const EMPTY_CHANNEL: BeneficiaryChannelFormData = {
    channel: 'ORANGE_MONEY',
    phoneNumber: '',
    countryCode: '',
    isPrimary: true,
};

/**
 * Formulaire de création/édition d'un bénéficiaire.
 *
 * Un bénéficiaire peut avoir 1 ou plusieurs canaux de réception.
 * Chaque canal peut être de type bancaire ou Mobile Money,
 * avec un code pays qui détermine la devise de réception.
 */
function BeneficiaryForm({ initialData, onSubmit, onCancel }: BeneficiaryFormProps) {
    const { t } = useTranslation('beneficiaries');
    const { t: tCommon } = useTranslation('common');

    const [firstName, setFirstName] = useState(initialData?.firstName ?? '');
    const [lastName, setLastName] = useState(initialData?.lastName ?? '');
    const [channels, setChannels] = useState<BeneficiaryChannelFormData[]>(
        initialData
            ? initialData.channels.map((ch) => ({
                  channel: ch.channel,
                  phoneNumber: ch.phoneNumber || '',
                  countryCode: ch.countryCode || '',
                  bankName: ch.bankName || '',
                  iban: ch.iban || '',
                  bic: ch.bic || '',
                  isPrimary: ch.isPrimary,
              }))
            : [{ ...EMPTY_CHANNEL }]
    );
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChannelChange = (index: number, data: BeneficiaryChannelFormData) => {
        const updated = channels.map((ch, i) => (i === index ? data : ch));
        setChannels(updated);
    };

    const handleRemoveChannel = (index: number) => {
        setChannels((prev) => prev.filter((_, i) => i !== index));
    };

    const handleAddChannel = () => {
        setChannels((prev) => [
            ...prev,
            { ...EMPTY_CHANNEL, isPrimary: false },
        ]);
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        // Utiliser le premier canal comme canal par défaut (compatibilité)
        const defaultChannel = channels[0]?.channel || 'ORANGE_MONEY';

        // Normaliser les valeurs avant envoi pour respecter les formats backend :
        // - IBAN / BIC : suppression des espaces/tirets + majuscules
        // - téléphone : suppression des espaces de groupement (garde le '+')
        // - noms : suppression des espaces inutiles en début/fin
        const normalizedChannels = channels.map((ch) => ({
            ...ch,
            phoneNumber: ch.phoneNumber ? ch.phoneNumber.replace(/[\s-]/g, '') : ch.phoneNumber,
            bankName: ch.bankName?.trim(),
            iban: ch.iban ? ch.iban.replace(/[\s-]/g, '').toUpperCase() : ch.iban,
            bic: ch.bic ? ch.bic.replace(/[\s-]/g, '').toUpperCase() : ch.bic,
        }));

        const payload: BeneficiaryFormData = {
            firstName: firstName.trim(),
            lastName: lastName.trim(),
            channel: defaultChannel,
            channels: normalizedChannels,
        };

        try {
            await onSubmit(payload);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            {/* Nom et prénom */}
            <div className="grid grid-cols-2 gap-4">
                <Input
                    label={t('form.firstName')}
                    placeholder={t('form.firstNamePlaceholder')}
                    icon={<User size={16} />}
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    required
                />
                <Input
                    label={t('form.lastName')}
                    placeholder={t('form.lastNamePlaceholder')}
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    required
                />
            </div>

            {/* Canaux de réception */}
            <div className="flex flex-col gap-3">
                <div className="flex items-center justify-between">
                    <label className="text-sm font-medium text-gray-700">
                        Canaux de réception
                    </label>
                    <button
                        type="button"
                        onClick={handleAddChannel}
                        className="flex items-center gap-1 text-sm font-medium text-brand-600 hover:text-brand-700"
                    >
                        <Plus size={14} />
                        Ajouter un canal
                    </button>
                </div>

                {channels.map((ch, index) => (
                    <BeneficiaryChannelForm
                        key={index}
                        index={index}
                        data={ch}
                        onChange={handleChannelChange}
                        onRemove={handleRemoveChannel}
                        canRemove={channels.length > 1}
                    />
                ))}
            </div>

            {error && (
                <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">
                    {error}
                </div>
            )}

            <div className="flex justify-end gap-2 pt-2">
                <Button type="button" variant="outline" onClick={onCancel}>
                    {tCommon('actions.cancel')}
                </Button>
                <Button type="submit" loading={isSubmitting}>
                    {initialData ? t('form.submitEdit') : t('form.submitCreate')}
                </Button>
            </div>
        </form>
    );
}

export default BeneficiaryForm;
