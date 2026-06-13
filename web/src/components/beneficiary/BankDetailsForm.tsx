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
 * - BANK_TRANSFER → bankName, iban, bic (RG-02)
 * - Tous les autres canaux → phoneNumber (RG-02)
 */
function ChannelDetailsForm({
                                channel,
                                bankDetails,
                                mobileDetails,
                                onBankChange,
                                onMobileChange,
                            }: ChannelDetailsFormProps) {
    if (channel === 'BANK_TRANSFER') {
        return (
            <div className="flex flex-col gap-4">
                <Input
                    label="Nom de la banque"
                    placeholder="BNP Paribas"
                    icon={<Landmark size={16} />}
                    value={bankDetails.bankName}
                    onChange={(e) => onBankChange('bankName', e.target.value)}
                    required
                />
                <Input
                    label="IBAN"
                    placeholder="FR76 3000 6000 0112 3456 7890 189"
                    icon={<Hash size={16} />}
                    value={bankDetails.iban}
                    onChange={(e) => onBankChange('iban', e.target.value.toUpperCase())}
                    required
                />
                <Input
                    label="BIC / SWIFT"
                    placeholder="BNPAFRPPXXX"
                    value={bankDetails.bic}
                    onChange={(e) => onBankChange('bic', e.target.value.toUpperCase())}
                    required
                />
            </div>
        );
    }

    return (
        <Input
            label="Numéro de téléphone Mobile Money"
            type="tel"
            placeholder="+225 07 00 00 00 00"
            icon={<Hash size={16} />}
            value={mobileDetails.phoneNumber}
            onChange={(e) => onMobileChange('phoneNumber', e.target.value)}
            required
        />
    );
}

export default ChannelDetailsForm;