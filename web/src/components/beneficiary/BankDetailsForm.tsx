import { useTranslation } from 'react-i18next';
import Input from '@/components/ui/Input';
import { Landmark, Hash } from 'lucide-react';
import type { BankDetails, MobileMoneyDetails, ChannelType } from '@/types';

export interface ChannelDetailsFormProps {
    channel: ChannelType;
    bankDetails: BankDetails;
    mobileDetails: MobileMoneyDetails;
    onBankChange: (field: keyof BankDetails, value: string) => void;
    onMobileChange: (field: keyof MobileMoneyDetails, value: string) => void;
}

/**
 * Sous-formulaire affichant les champs adaptés au canal sélectionné.
 *
 * Namespace "beneficiaries" (clés form.*).
 */
function ChannelDetailsForm({
                                channel,
                                bankDetails,
                                mobileDetails,
                                onBankChange,
                                onMobileChange,
                            }: ChannelDetailsFormProps) {
    const { t } = useTranslation('beneficiaries');

    if (channel === 'BANK_TRANSFER') {
        return (
            <div className="flex flex-col gap-4">
                <Input
                    label={t('form.bankName')}
                    placeholder={t('form.bankNamePlaceholder')}
                    icon={<Landmark size={16} />}
                    value={bankDetails.bankName}
                    onChange={(e) => onBankChange('bankName', e.target.value)}
                    required
                />
                <Input
                    label={t('form.iban')}
                    placeholder={t('form.ibanPlaceholder')}
                    icon={<Hash size={16} />}
                    value={bankDetails.iban}
                    onChange={(e) => onBankChange('iban', e.target.value.toUpperCase())}
                    required
                />
                <Input
                    label={t('form.bic')}
                    placeholder={t('form.bicPlaceholder')}
                    value={bankDetails.bic}
                    onChange={(e) => onBankChange('bic', e.target.value.toUpperCase())}
                    required
                />
            </div>
        );
    }

    return (
        <Input
            label={t('form.phoneNumber')}
            type="tel"
            placeholder={t('form.phoneNumberPlaceholder')}
            icon={<Hash size={16} />}
            value={mobileDetails.phoneNumber}
            onChange={(e) => onMobileChange('phoneNumber', e.target.value)}
            required
        />
    );
}

export default ChannelDetailsForm;