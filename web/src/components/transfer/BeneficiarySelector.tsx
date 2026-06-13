import { Link } from 'react-router-dom';
import { Plus } from 'lucide-react';
import { cn } from '@/lib/utils';
import { CHANNEL_LABELS, CHANNEL_ICONS } from '@/utils/constants';
import type { Beneficiary } from '@/types';

export interface BeneficiarySelectorProps {
    beneficiaries: Beneficiary[];
    value: number | null;
    onChange: (id: number) => void;
}

/**
 * Sélecteur de bénéficiaire sous forme de liste de cartes cliquables.
 * Affiche un lien vers /beneficiaries si le carnet est vide.
 */
function BeneficiarySelector({ beneficiaries, value, onChange }: BeneficiarySelectorProps) {
    if (beneficiaries.length === 0) {
        return (
            <div className="flex flex-col items-center gap-2 rounded-xl border border-dashed border-gray-300 p-6 text-center">
                <p className="text-sm text-gray-500">Vous n'avez aucun bénéficiaire enregistré.</p>
                <Link
                    to="/beneficiaries"
                    className="flex items-center gap-1 text-sm font-medium text-brand-600 hover:underline"
                >
                    <Plus size={14} /> Ajouter un bénéficiaire
                </Link>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-1.5">
            <label className="text-sm font-medium text-gray-700">Bénéficiaire</label>
            <div className="flex flex-col gap-2">
                {beneficiaries.map((b) => (
                    <button
                        key={b.id}
                        type="button"
                        onClick={() => onChange(b.id)}
                        className={cn(
                            'flex items-center gap-3 rounded-xl border p-3 text-left transition-all',
                            value === b.id
                                ? 'border-brand-500 bg-brand-50 ring-2 ring-brand-100'
                                : 'border-gray-200 bg-white hover:border-gray-300'
                        )}
                    >
                        <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700">
                            {b.firstName[0]}{b.lastName[0]}
                        </div>
                        <div className="min-w-0">
                            <p className="truncate text-sm font-medium text-gray-900">
                                {b.firstName} {b.lastName}
                            </p>
                            <p className="text-xs text-gray-500">
                                {CHANNEL_ICONS[b.channel]} {CHANNEL_LABELS[b.channel]}
                            </p>
                        </div>
                    </button>
                ))}
            </div>
        </div>
    );
}

export default BeneficiarySelector;