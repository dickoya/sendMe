import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Percent } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { adminService } from '@/services/adminService';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Formulaire de modification du taux de commission.
 *
 * Migration i18n (Phase 11) : namespace "admin" (clés settings.fees.*).
 */
function FeeConfigForm() {
    const { t } = useTranslation('admin');
    const { showToast } = useToast();
    const [feeRate, setFeeRate] = useState('1.5');
    const [reason, setReason] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);

        const rate = parseFloat(feeRate);
        if (isNaN(rate) || rate < 0 || rate > 10) {
            setError(t('settings.fees.errors.invalidRate'));
            return;
        }
        if (!reason.trim()) {
            setError(t('settings.fees.errors.reasonRequired'));
            return;
        }

        setIsSubmitting(true);
        try {
            await adminService.updateFeeRate({ feeRate: rate, reason });
            showToast(t('settings.fees.toasts.success', { rate }), 'success');
            setReason('');
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input
                label={t('settings.fees.rateLabel')}
                type="number"
                step="0.1"
                min="0"
                max="10"
                icon={<Percent size={16} />}
                value={feeRate}
                onChange={(e) => setFeeRate(e.target.value)}
                hint={t('settings.fees.rateHint')}
                required
            />

            <Input
                label={t('settings.fees.reasonLabel')}
                placeholder={t('settings.fees.reasonPlaceholder')}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                hint={t('settings.fees.reasonHint')}
                required
            />

            {error && (
                <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">{error}</div>
            )}

            <div className="rounded-lg bg-warning-50 px-3 py-2 text-xs text-warning-600">
                {t('settings.fees.warning')}
            </div>

            <Button type="submit" loading={isSubmitting} className="self-start">
                {t('settings.fees.submitButton')}
            </Button>
        </form>
    );
}

export default FeeConfigForm;