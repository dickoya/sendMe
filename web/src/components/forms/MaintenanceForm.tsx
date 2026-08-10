import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { Wrench, Calendar, Clock } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { adminService } from '@/services/adminService';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Formulaire d'envoi d'une notification de maintenance.
 *
 * Namespace "admin" (clés settings.maintenance.*).
 *
 * Limitation documentée : les champs `date` et `duration` sont en
 * texte libre, envoyés tels quels au backend pour l'email (toujours
 * en français). Si l'admin saisit en anglais ici, l'email résultant
 * mélangera FR/EN.
 */
function MaintenanceForm() {
    const { t } = useTranslation('admin');
    const { showToast } = useToast();
    const [date, setDate] = useState('');
    const [duration, setDuration] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!date.trim() || !duration.trim()) {
            setError(t('settings.maintenance.errors.fieldsRequired'));
            return;
        }

        setIsSubmitting(true);
        try {
            await adminService.sendMaintenanceNotification(date, duration);
            showToast(t('settings.maintenance.toasts.success'), 'success');
            setDate('');
            setDuration('');
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <Input
                label={t('settings.maintenance.dateLabel')}
                placeholder={t('settings.maintenance.datePlaceholder')}
                icon={<Calendar size={16} />}
                value={date}
                onChange={(e) => setDate(e.target.value)}
                hint={t('settings.maintenance.dateHint')}
                required
            />

            <Input
                label={t('settings.maintenance.durationLabel')}
                placeholder={t('settings.maintenance.durationPlaceholder')}
                icon={<Clock size={16} />}
                value={duration}
                onChange={(e) => setDuration(e.target.value)}
                required
            />

            {error && (
                <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">{error}</div>
            )}

            <Button type="submit" loading={isSubmitting} icon={<Wrench size={16} />} className="self-start">
                {t('settings.maintenance.submitButton')}
            </Button>
        </form>
    );
}

export default MaintenanceForm;