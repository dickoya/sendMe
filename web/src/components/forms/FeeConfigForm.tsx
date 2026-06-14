import { useState, type FormEvent } from 'react';
import { Percent } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import { adminService } from '@/services/adminService';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Formulaire de modification du taux de commission de la plateforme.
 *
 * Soumission → PUT /api/admin/config/fees
 * Déclenche côté backend l'envoi d'un email "Mise à jour des tarifs"
 * à tous les utilisateurs actifs (Phase F).
 */
function FeeConfigForm() {
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
            setError('Le taux doit être compris entre 0% et 10%');
            return;
        }
        if (!reason.trim()) {
            setError('Veuillez indiquer une raison pour ce changement');
            return;
        }

        setIsSubmitting(true);
        try {
            await adminService.updateFeeRate({ feeRate: rate, reason });
            showToast(
                `Taux mis à jour à ${rate}% — un email a été envoyé à tous les utilisateurs actifs`,
                'success'
            );
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
                label="Nouveau taux de commission (%)"
                type="number"
                step="0.1"
                min="0"
                max="10"
                icon={<Percent size={16} />}
                value={feeRate}
                onChange={(e) => setFeeRate(e.target.value)}
                hint="Entre 0% et 10%. Exemple : 1.5 pour 1,5%"
                required
            />

            <Input
                label="Raison du changement"
                placeholder="Ex: Ajustement tarifaire trimestriel"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                hint="Conservée dans les logs pour audit"
                required
            />

            {error && (
                <div className="rounded-lg bg-danger-50 px-3 py-2 text-sm text-danger-600">{error}</div>
            )}

            <div className="rounded-lg bg-warning-50 px-3 py-2 text-xs text-warning-600">
                ⚠️ Le nouveau taux sera appliqué immédiatement aux nouveaux transferts et un email
                sera envoyé à tous les utilisateurs actifs.
            </div>

            <Button type="submit" loading={isSubmitting} className="self-start">
                Mettre à jour le taux
            </Button>
        </form>
    );
}

export default FeeConfigForm;