import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Clock, Percent } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { adminService } from '@/services/adminService';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Formulaire de configuration des taux de change (admin).
 *
 * Permet à l'administrateur de modifier :
 * - La durée de vie du cache (en minutes)
 * - La marge interne (en pourcentage)
 *
 * Les modifications sont appliquées immédiatement sans redémarrage.
 */
function ExchangeRateConfigForm() {
    const { t } = useTranslation('admin');
    const { showToast } = useToast();

    const [cacheDurationMinutes, setCacheDurationMinutes] = useState('');
    const [marginPercent, setMarginPercent] = useState('');
    const [reason, setReason] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!reason.trim()) {
            setError(t('settings.exchangeRate.validation.reasonRequired'));
            return;
        }

        const cacheMinutes = cacheDurationMinutes ? parseInt(cacheDurationMinutes, 10) : undefined;
        const margin = marginPercent ? parseFloat(marginPercent) : undefined;

        if (!cacheMinutes && !margin) {
            setError(t('settings.exchangeRate.validation.atLeastOne'));
            return;
        }

        setIsSubmitting(true);
        try {
            await adminService.updateExchangeRateConfig({
                cacheDurationMinutes: cacheMinutes,
                marginPercent: margin,
                reason: reason.trim(),
            });
            showToast(t('settings.exchangeRate.success'), 'success');
            setReason('');
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input
                    label={t('settings.exchangeRate.cacheDuration')}
                    type="number"
                    min={1}
                    placeholder="360"
                    icon={<Clock size={16} />}
                    value={cacheDurationMinutes}
                    onChange={(e) => setCacheDurationMinutes(e.target.value)}
                    hint={t('settings.exchangeRate.cacheDurationHint')}
                />

                <Input
                    label={t('settings.exchangeRate.marginPercent')}
                    type="number"
                    min={0}
                    max={50}
                    step={0.1}
                    placeholder="2.0"
                    icon={<Percent size={16} />}
                    value={marginPercent}
                    onChange={(e) => setMarginPercent(e.target.value)}
                    hint={t('settings.exchangeRate.marginPercentHint')}
                />
            </div>

            <Input
                label={t('settings.exchangeRate.reason')}
                placeholder={t('settings.exchangeRate.reasonPlaceholder')}
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                required
            />

            {error && (
                <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">
                    {error}
                </div>
            )}

            <Button type="submit" loading={isSubmitting} className="self-start">
                {t('settings.exchangeRate.save')}
            </Button>
        </form>
    );
}

export default ExchangeRateConfigForm;
