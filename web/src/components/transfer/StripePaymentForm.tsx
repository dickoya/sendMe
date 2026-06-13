import { useState, type FormEvent } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { CreditCard, Lock } from 'lucide-react';
import Button from '@/components/ui/Button';

export interface StripePaymentFormProps {
    /** Montant total à débiter, affiché sur le bouton de confirmation */
    amountLabel: string;
    /** Appelé avec le paymentMethodId une fois la carte validée par Stripe */
    onPaymentMethodCreated: (paymentMethodId: string) => Promise<void>;
    disabled?: boolean;
}

/**
 * Style du CardElement pour qu'il s'intègre visuellement
 * avec le design Tailwind (Inter, couleurs neutres).
 */
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
};

/**
 * Formulaire de paiement par carte via Stripe Elements.
 *
 * Flux :
 * 1. L'utilisateur saisit les infos de carte dans le <CardElement>
 * 2. Au submit, stripe.createPaymentMethod() valide la carte côté Stripe
 *    et retourne un paymentMethodId (ex: pm_xxxxx)
 * 3. Ce paymentMethodId est transmis au backend via POST /api/transfers
 *    qui l'utilise pour créer et confirmer le PaymentIntent
 *
 * Cartes de test Stripe :
 * - 4242 4242 4242 4242 → succès
 * - 4000 0000 0000 0002 → refusée
 * - Date future quelconque, CVC quelconque
 */
function StripePaymentForm({ amountLabel, onPaymentMethodCreated, disabled }: StripePaymentFormProps) {
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
            setCardError(error.message ?? 'Carte invalide');
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
                    Informations de carte
                </label>
                <div className="rounded-lg border border-gray-300 bg-white px-3 py-3 transition-colors focus-within:border-brand-500 focus-within:ring-2 focus-within:ring-brand-100">
                    <CardElement options={CARD_ELEMENT_OPTIONS} onChange={(e) => setCardError(e.error?.message ?? null)} />
                </div>
                {cardError && <span className="text-xs text-danger-500">{cardError}</span>}
            </div>

            <div className="flex items-center gap-1.5 text-xs text-gray-400">
                <Lock size={12} />
                Paiement sécurisé par Stripe — vos données bancaires ne sont jamais stockées par SendMe
            </div>

            <Button
                type="submit"
                size="lg"
                fullWidth
                icon={<CreditCard size={16} />}
                loading={isProcessing}
                disabled={!stripe || disabled}
            >
                Payer {amountLabel}
            </Button>
        </form>
    );
}

export default StripePaymentForm;