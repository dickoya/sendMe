import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { CreditCard, Lock } from 'lucide-react';
import Button from '@/components/ui/Button';

export interface StripePaymentFormProps {
    amountLabel: string;
    onPaymentMethodCreated: (paymentMethodId: string) => Promise<void>;
    disabled?: boolean;
}

const CARD_ELEMENT_OPTIONS = {
    style: {
        base: {
            fontSize: '14px',
            fontFamily: 'Inter, sans-serif',
            color: '#1C2833',
            '::placeholder': { color: '#9CA3AF' },
        },
        invalid: {
            color: '#C0392B',
        },
    },
    hidePostalCode: true,
};

/**
 * Formulaire de paiement par carte via Stripe Elements.
 *
 * Namespace "transfer" (clés payment.*).
 *
 * Note importante : les messages d'erreur Stripe.js eux-mêmes
 * (`error.message` retourné par stripe.createPaymentMethod()) sont
 * déjà dans la bonne langue grâce à `getStripe(i18n.language)`
 * (locale Stripe activée au niveau de <Elements> dans Transfer.tsx).
 * On ne traduit donc PAS ces messages ici : ils proviennent
 * directement de Stripe, déjà localisés. `genericCardError` ne sert
 * que de repli si `error.message` est absent.
 */
function StripePaymentForm({ amountLabel, onPaymentMethodCreated, disabled }: StripePaymentFormProps) {
    const { t } = useTranslation('transfer');
    const stripe = useStripe();
    const elements = useElements();
    const [cardError, setCardError] = useState<string | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!stripe || !elements) return;

        const cardElement = elements.getElement(CardElement);
        if (!cardElement) return;

        setIsProcessing(true);
        setCardError(null);

        const { error, paymentMethod } = await stripe.createPaymentMethod({
            type: 'card',
            card: cardElement,
        });

        if (error) {
            setCardError(error.message ?? t('payment.genericCardError'));
            setIsProcessing(false);
            return;
        }

        try {
            await onPaymentMethodCreated(paymentMethod.id);
        } finally {
            setIsProcessing(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
                <label className="text-sm font-medium text-gray-700">
                    {t('payment.cardInfoLabel')}
                </label>
                <div className="rounded-lg border border-gray-300 bg-white px-3 py-3 transition-colors focus-within:border-brand-500 focus-within:ring-2 focus-within:ring-brand-100">
                    <CardElement options={CARD_ELEMENT_OPTIONS} onChange={(e) => setCardError(e.error?.message ?? null)} />
                </div>
                {cardError && <span className="text-xs text-danger-500">{cardError}</span>}
            </div>

            <div className="flex items-center gap-1.5 text-xs text-gray-400">
                <Lock size={12} />
                {t('payment.securityNote')}
            </div>

            <Button
                type="submit"
                size="lg"
                fullWidth
                icon={<CreditCard size={16} />}
                loading={isProcessing}
                disabled={!stripe || disabled}
            >
                {t('payment.payButton', { amount: amountLabel })}
            </Button>
        </form>
    );
}

export default StripePaymentForm;
