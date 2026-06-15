import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { User } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import ChannelSelector from '@/components/transfer/ChannelSelector';
import ChannelDetailsForm from '@/components/beneficiary/BankDetailsForm';
import { getErrorMessage } from '@/lib/utils';
import type { Beneficiary, BeneficiaryFormData, BankDetails, MobileMoneyDetails, ChannelType } from '@/types';

export interface BeneficiaryFormProps {
    initialData?: Beneficiary;
    onSubmit: (payload: BeneficiaryFormData) => Promise<unknown>;
    onCancel: () => void;
}

const EMPTY_BANK: BankDetails = { bankName: '', iban: '', bic: '' };
const EMPTY_MOBILE: MobileMoneyDetails = { phoneNumber: '' };

/**
 * Formulaire de création/édition d'un bénéficiaire.
 *
 * Migration i18n (Phase 7) : namespace "beneficiaries" (clés form.*),
 * "common" pour les boutons Annuler/Continuer génériques.
 */
function BeneficiaryForm({ initialData, onSubmit, onCancel }: BeneficiaryFormProps) {
    const { t } = useTranslation('beneficiaries');
    const { t: tCommon } = useTranslation('common');

    const [firstName, setFirstName] = useState(initialData?.firstName ?? '');
    const [lastName, setLastName] = useState(initialData?.lastName ?? '');
    const [channel, setChannel] = useState<ChannelType>(initialData?.channel ?? 'ORANGE_MONEY');
    const [bankDetails, setBankDetails] = useState<BankDetails>(
        initialData?.bankDetails ?? EMPTY_BANK
    );
    const [mobileDetails, setMobileDetails] = useState<MobileMoneyDetails>(
        initialData?.mobileDetails ?? EMPTY_MOBILE
    );
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsSubmitting(true);

        const payload: BeneficiaryFormData = {
            firstName,
            lastName,
            channel,
            ...(channel === 'BANK_TRANSFER'
                ? { bankDetails }
                : { mobileDetails }),
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

            <ChannelSelector value={channel} onChange={setChannel} />

            <ChannelDetailsForm
                channel={channel}
                bankDetails={bankDetails}
                mobileDetails={mobileDetails}
                onBankChange={(field, value) => setBankDetails((prev) => ({ ...prev, [field]: value }))}
                onMobileChange={(field, value) => setMobileDetails((prev) => ({ ...prev, [field]: value }))}
            />

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