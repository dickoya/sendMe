import { useState, type FormEvent } from 'react';
import { Wrench, Calendar, Clock } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { adminService } from '@/services/adminService';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Formulaire d'envoi d'une notification de maintenance programmée
 * à tous les utilisateurs actifs (email — Phase F backend).
 */
function MaintenanceForm() {
    const { showToast } = useToast();
    const [date, setDate] = useState('');
    const [duration, setDuration] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);

        if (!date.trim() || !duration.trim()) {
            setError('Veuillez renseigner la date et la durée');
            return;
        }

        setIsSubmitting(true);
        try {
            await adminService.sendMaintenanceNotification(date, duration);
            showToast('Notification de maintenance envoyée à tous les utilisateurs actifs', 'success');
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
                label="Date et heure de la maintenance"
                placeholder="15 juin 2025 à 02:00"
                icon={<Calendar size={16} />}
                value={date}
                onChange={(e) => setDate(e.target.value)}
                hint="Format libre — repris tel quel dans l'email"
                required
            />

            <Input
                label="Durée estimée"
                placeholder="2 heures"
                icon={<Clock size={16} />}
                value={duration}
                onChange={(e) => setDuration(e.target.value)}
                required
            />

            {error && (
                <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">{error}</div>
            )}

            <Button type="submit" loading={isSubmitting} icon={<Wrench size={16} />} className="self-start">
                Envoyer la notification
            </Button>
        </form>
    );
}

export default MaintenanceForm;