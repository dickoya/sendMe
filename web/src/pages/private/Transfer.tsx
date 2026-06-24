import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Elements } from '@stripe/react-stripe-js';
import { ArrowLeft, Smartphone, Landmark } from 'lucide-react';
import { cn } from '@/lib/utils';
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
import { getStripe } from '@/services/stripeService';
import { transferService } from '@/services/transferService';
import { referralService } from '@/services/referralService';
import { getErrorMessage } from '@/lib/utils';
import { getChannelLabel, getChannelIcon, getCurrencyForChannel } from '@/utils/constants';
import { formatCurrency } from '@/utils/formatCurrency';
import type { SimulateResponse, Transaction, Beneficiary, BeneficiaryChannel } from '@/types';

type Step = 'form' | 'summary' | 'payment' | 'success';

/**
 * Tunnel de transfert en plusieurs étapes.
 *
 * Migration i18n (Phase 8) :
 * - namespace "transfer" pour tous les libellés du tunnel
 * - "common" pour les boutons génériques (back, continue)
 * - <Elements stripe={...}> utilise désormais getStripe(i18n.language)
 *   (Phase 3) au lieu du singleton stripePromise — Stripe affichera
 *   ses propres messages d'erreur dans la langue active
 */
function Transfer() {
    const { t, i18n } = useTranslation('transfer');
    const { t: tCommon } = useTranslation('common');
    const { beneficiaries, isLoading: beneficiariesLoading } = useBeneficiary();
    const { showToast } = useToast();

    const [step, setStep] = useState<Step>('form');
    const [amount, setAmount] = useState('');
    const [currency] = useState('EUR');
    const [beneficiaryId, setBeneficiaryId] = useState<number | null>(null);
    const [selectedChannelId, setSelectedChannelId] = useState<number | null>(null);

    const [simulation, setSimulation] = useState<SimulateResponse | null>(null);
    const [simulating, setSimulating] = useState(false);
    const [simulateError, setSimulateError] = useState<string | null>(null);

    const [isTransferring, setIsTransferring] = useState(false);
    const [transferError, setTransferError] = useState<string | null>(null);
    const [completedTransaction, setCompletedTransaction] = useState<Transaction | null>(null);

    // ── État du toggle de réduction de parrainage ───────────────────────────
    const [applyDiscount, setApplyDiscount] = useState(true);
    const [availableDiscount, setAvailableDiscount] = useState(0);

    // Récupérer le montant de réduction disponible au chargement
    useEffect(() => {
        referralService.getDiscount(currency).then(setAvailableDiscount).catch(() => setAvailableDiscount(0));
    }, [currency]);

    const selectedBeneficiary: Beneficiary | undefined = beneficiaries.find((b) => b.id === beneficiaryId);

    /**
     * Canal sélectionné pour ce transfert.
     * Si l'utilisateur a choisi un canal spécifique, on l'utilise.
     * Sinon, on prend le canal principal (isPrimary=true) ou le premier.
     */
    const selectedChannel: BeneficiaryChannel | undefined =
        selectedBeneficiary?.channels.find((ch) => ch.id === selectedChannelId) ||
        selectedBeneficiary?.channels.find((ch) => ch.isPrimary) ||
        selectedBeneficiary?.channels[0];

    // Réinitialiser le canal sélectionné quand le bénéficiaire change
    useEffect(() => {
        setSelectedChannelId(null);
    }, [beneficiaryId]);

    const debouncedAmount = useDebounce(amount, 400);
    const numericAmount = parseFloat(debouncedAmount);

    // ── Simulation automatique ──────────────────────────────────────────────
    useEffect(() => {
        if (!selectedBeneficiary || !selectedChannel || !numericAmount || numericAmount <= 0) {
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
                currencyTo: selectedChannel.currency,
                channel: selectedChannel.channel,
                applyDiscount,
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
    }, [debouncedAmount, numericAmount, selectedBeneficiary, selectedChannel, currency, applyDiscount]);

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
                channelId: selectedChannel?.id,
                paymentMethodId,
                idempotencyKey: crypto.randomUUID(),
            });

            setCompletedTransaction(transaction);
            setStep('success');
            showToast(t('toasts.success'), 'success');
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
        return <Spinner fullPage label={tCommon('states.loading')} />;
    }

    return (
        <div className="mx-auto max-w-lg">
            <Elements stripe={getStripe(i18n.language)}>
            <Card title={stepTitle(step, t)}>

                {/* ─────────────────────────── ÉTAPE 1 : FORMULAIRE ─────────────────────────── */}
                {step === 'form' && (
                    <div className="flex flex-col gap-5">
                        <BeneficiarySelector
                            beneficiaries={beneficiaries}
                            value={beneficiaryId}
                            onChange={setBeneficiaryId}
                        />

                        {/* ── Sélecteur de canal si le bénéficiaire a plusieurs canaux ── */}
                        {selectedBeneficiary && selectedBeneficiary.channels.length > 1 && (
                            <div className="flex flex-col gap-1.5">
                                <label className="text-sm font-medium text-gray-700">
                                    {t('form.channelLabel')}
                                </label>
                                <div className="flex flex-wrap gap-2">
                                    {selectedBeneficiary.channels.map((ch) => (
                                        <button
                                            key={ch.id}
                                            type="button"
                                            onClick={() => setSelectedChannelId(ch.id)}
                                            className={cn(
                                                'flex items-center gap-2 rounded-xl border px-3 py-2 text-left transition-all',
                                                selectedChannelId === ch.id || (!selectedChannelId && ch.isPrimary)
                                                    ? 'border-brand-500 bg-brand-50 ring-2 ring-brand-100'
                                                    : 'border-gray-200 bg-white hover:border-gray-300'
                                            )}
                                        >
                                            <span className="text-lg">{getChannelIcon(ch.channel)}</span>
                                            <div className="text-sm">
                                                <p className="font-medium text-gray-900">
                                                    {getChannelLabel(ch.channel, tCommon)}
                                                </p>
                                                <p className="text-xs text-gray-500">
                                                    {ch.channel === 'BANK_TRANSFER' ? ch.bankName : ch.phoneNumber}
                                                    {ch.isPrimary && (
                                                        <span className="ml-1.5 rounded-full bg-brand-100 px-1.5 py-0.5 text-[10px] text-brand-700">
                                                            {t('form.primaryBadge')}
                                                        </span>
                                                    )}
                                                </p>
                                            </div>
                                            <span className="ml-auto text-xs font-mono text-gray-400">{ch.currency}</span>
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}

                        <Input
                            label={t('form.amountLabel')}
                            type="number"
                            inputMode="decimal"
                            placeholder="0.00"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            hint={t('form.amountHint', { currency })}
                            min="1"
                            step="0.01"
                        />

                        {selectedBeneficiary && amount && (
                            <FeeSimulator
                                simulation={simulation}
                                isLoading={simulating}
                                currencyFrom={currency}
                                showDiscountToggle={availableDiscount > 0}
                                availableDiscount={availableDiscount}
                                applyDiscount={applyDiscount}
                                onToggleDiscount={setApplyDiscount}
                            />
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
                            {t('form.continueButton')}
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
                            selectedChannel={selectedChannel}
                        />

                        <div className="flex gap-2">
                            <Button variant="outline" icon={<ArrowLeft size={16} />} onClick={() => setStep('form')}>
                                {t('summary.backButton')}
                            </Button>
                            <Button fullWidth onClick={() => setStep('payment')}>
                                {t('summary.proceedButton')}
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
                            <ArrowLeft size={14} /> {t('payment.backToSummary')}
                        </button>

                        <StripePaymentForm
                            amountLabel={formatCurrency(simulation.totalDebited, currency)}
                            onPaymentMethodCreated={handlePayment}
                            disabled={isTransferring}
                        />

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
            </Elements>
        </div>

    );
}

/**
 * Titre de carte affiché selon l'étape du tunnel.
 */
function stepTitle(step: Step, t: (key: string) => string): string {
    switch (step) {
        case 'form': return t('stepTitles.form');
        case 'summary': return t('stepTitles.summary');
        case 'payment': return t('stepTitles.payment');
        case 'success': return t('stepTitles.success');
    }
}

export default Transfer;