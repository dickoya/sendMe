import { useState, useEffect } from 'react';
import { Elements } from '@stripe/react-stripe-js';
import { ArrowLeft } from 'lucide-react';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Spinner from '@/components/ui/Spinner';
import BeneficiarySelector from '@/components/transfer/BeneficiarySelector';
import FeeSimulator from '@/components/transfer/FeeSimulator';
import TransferSummary from '@/components/transfer/TransferSummary';
import StripePaymentForm from '@/components/transfer/StripePaymentForm';
import TransferSuccess from '@/components/transfer/TransferSuccess';
import { useBeneficiary } from '@/hooks/useBeneficiary';
import { useDebounce } from '@/hooks/useDebounce';
import { useToast } from '@/store/notification/toastContext';
import { transferService } from '@/services/transferService';
import { getErrorMessage } from '@/lib/utils';
import { getCurrencyForChannel } from '@/utils/constants';
import { formatCurrency } from '@/utils/formatCurrency';
import type { SimulateResponse, Transaction, Beneficiary } from '@/types';
import {getStripe} from "@/services/stripeService.ts";
export const stripePromise = getStripe('fr');

type Step = 'form' | 'summary' | 'payment' | 'success';

/**
 * Tunnel de transfert en plusieurs étapes :
 *
 * 1. "form"    — choix du bénéficiaire + montant, simulation en temps réel
 * 2. "summary" — récapitulatif complet avant paiement
 * 3. "payment" — saisie de la carte via Stripe Elements
 * 4. "success" — confirmation avec référence et montant reçu
 *
 * La simulation (/transfers/simulate) est appelée automatiquement
 * (debounced) à chaque changement de montant ou de bénéficiaire,
 * sans créer de transaction en base.
 */
function Transfer() {
    const { beneficiaries, isLoading: beneficiariesLoading } = useBeneficiary();
    const { showToast } = useToast();

    const [step, setStep] = useState<Step>('form');
    const [amount, setAmount] = useState('');
    const [currency] = useState('EUR'); // EUR fixe — devise de la carte (RG-01)
    const [beneficiaryId, setBeneficiaryId] = useState<number | null>(null);

    const [simulation, setSimulation] = useState<SimulateResponse | null>(null);
    const [simulating, setSimulating] = useState(false);
    const [simulateError, setSimulateError] = useState<string | null>(null);

    const [isTransferring, setIsTransferring] = useState(false);
    const [transferError, setTransferError] = useState<string | null>(null);
    const [completedTransaction, setCompletedTransaction] = useState<Transaction | null>(null);

    const selectedBeneficiary: Beneficiary | undefined = beneficiaries.find((b) => b.id === beneficiaryId);

    const debouncedAmount = useDebounce(amount, 400);
    const numericAmount = parseFloat(debouncedAmount);

    // ── Simulation automatique ──────────────────────────────────────────────
    useEffect(() => {
        if (!selectedBeneficiary || !numericAmount || numericAmount <= 0) {
            setSimulation(null);
            return;
        }

        let cancelled = false;
        setSimulating(true);
        setSimulateError(null);

        transferService
            .simulate({
                amount: numericAmount,
                currencyFrom: currency,
                currencyTo: getCurrencyForChannel(selectedBeneficiary.channel),
                channel: selectedBeneficiary.channel,
            })
            .then((result) => {
                if (!cancelled) setSimulation(result);
            })
            .catch((err) => {
                if (!cancelled) setSimulateError(getErrorMessage(err));
            })
            .finally(() => {
                if (!cancelled) setSimulating(false);
            });

        return () => {
            cancelled = true;
        };
    }, [debouncedAmount, numericAmount, selectedBeneficiary, currency]);

    // ── Soumission du paiement ──────────────────────────────────────────────
    const handlePayment = async (paymentMethodId: string) => {
        if (!selectedBeneficiary || !simulation) return;

        setIsTransferring(true);
        setTransferError(null);

        try {
            const transaction = await transferService.transfer({
                amount: simulation.amountSent,
                currency,
                beneficiaryId: selectedBeneficiary.id,
                paymentMethodId,
                idempotencyKey: crypto.randomUUID(),
            });

            setCompletedTransaction(transaction);
            setStep('success');
            showToast('Transfert effectué avec succès !', 'success');
        } catch (err) {
            setTransferError(getErrorMessage(err));
            showToast(getErrorMessage(err), 'error');
        } finally {
            setIsTransferring(false);
        }
    };

    const resetTunnel = () => {
        setStep('form');
        setAmount('');
        setBeneficiaryId(null);
        setSimulation(null);
        setCompletedTransaction(null);
        setTransferError(null);
    };

    // ── Rendu ─────────────────────────────────────────────────────────────────
    if (beneficiariesLoading) {
        return <Spinner fullPage label="Chargement..." />;
    }

    return (
        <div className="mx-auto max-w-lg">
            <Card title={stepTitle(step)}>
                {/* ─────────────────────────── ÉTAPE 1 : FORMULAIRE ─────────────────────────── */}
                {step === 'form' && (
                    <div className="flex flex-col gap-5">
                        <BeneficiarySelector
                            beneficiaries={beneficiaries}
                            value={beneficiaryId}
                            onChange={setBeneficiaryId}
                        />

                        <Input
                            label="Montant à envoyer"
                            type="number"
                            inputMode="decimal"
                            placeholder="0.00"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            hint={`Devise de paiement : ${currency}`}
                            min="1"
                            step="0.01"
                        />

                        {selectedBeneficiary && amount && (
                            <FeeSimulator simulation={simulation} isLoading={simulating} currencyFrom={currency} />
                        )}

                        {simulateError && (
                            <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">
                                {simulateError}
                            </div>
                        )}

                        <Button
                            size="lg"
                            fullWidth
                            disabled={!selectedBeneficiary || !simulation}
                            onClick={() => setStep('summary')}
                        >
                            Continuer
                        </Button>
                    </div>
                )}

                {/* ─────────────────────────── ÉTAPE 2 : RÉCAPITULATIF ─────────────────────────── */}
                {step === 'summary' && selectedBeneficiary && simulation && (
                    <div className="flex flex-col gap-5">
                        <TransferSummary
                            beneficiary={selectedBeneficiary}
                            simulation={simulation}
                            currencyFrom={currency}
                        />

                        <div className="flex gap-2">
                            <Button variant="outline" icon={<ArrowLeft size={16} />} onClick={() => setStep('form')}>
                                Retour
                            </Button>
                            <Button fullWidth onClick={() => setStep('payment')}>
                                Procéder au paiement
                            </Button>
                        </div>
                    </div>
                )}

                {/* ─────────────────────────── ÉTAPE 3 : PAIEMENT STRIPE ─────────────────────────── */}
                {step === 'payment' && simulation && (
                    <div className="flex flex-col gap-4">
                        <button
                            onClick={() => setStep('summary')}
                            className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
                        >
                            <ArrowLeft size={14} /> Retour au récapitulatif
                        </button>

                        <Elements stripe={stripePromise}>
                            <StripePaymentForm
                                amountLabel={formatCurrency(simulation.totalDebited, currency)}
                                onPaymentMethodCreated={handlePayment}
                                disabled={isTransferring}
                            />
                        </Elements>

                        {transferError && (
                            <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">
                                {transferError}
                            </div>
                        )}
                    </div>
                )}

                {/* ─────────────────────────── ÉTAPE 4 : SUCCÈS ─────────────────────────── */}
                {step === 'success' && completedTransaction && (
                    <TransferSuccess transaction={completedTransaction} onNewTransfer={resetTunnel} />
                )}
            </Card>
        </div>
    );
}

/**
 * Titre de carte affiché selon l'étape du tunnel.
 */
function stepTitle(step: Step): string {
    switch (step) {
        case 'form': return 'Nouveau transfert';
        case 'summary': return 'Récapitulatif';
        case 'payment': return 'Paiement par carte';
        case 'success': return '';
    }
}

export default Transfer;