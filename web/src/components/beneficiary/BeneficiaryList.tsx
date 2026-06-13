import { Users } from 'lucide-react';
//import BeneficiaryCard from './BeneficiaryCard.';
import BeneficiaryCard  from "@/components/beneficiary/ BeneficiaryCard.tsx";
import type { Beneficiary } from '@/types';

export interface BeneficiaryListProps {
    beneficiaries: Beneficiary[];
    onEdit: (beneficiary: Beneficiary) => void;
    onDelete: (beneficiary: Beneficiary) => void;
}

/**
 * Liste des bénéficiaires avec état vide illustré.
 */
function BeneficiaryList({ beneficiaries, onEdit, onDelete }: BeneficiaryListProps) {
    if (beneficiaries.length === 0) {
        return (
            <div className="flex flex-col items-center gap-2 py-12 text-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400">
                    <Users size={22} />
                </div>
                <p className="font-medium text-gray-900">Aucun bénéficiaire</p>
                <p className="text-sm text-gray-500">
                    Ajoutez votre premier bénéficiaire pour commencer à envoyer de l'argent.
                </p>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-2">
            {beneficiaries.map((b) => (
                <BeneficiaryCard
                    key={b.id}
                    beneficiary={b}
                    onEdit={() => onEdit(b)}
                    onDelete={() => onDelete(b)}
                />
            ))}
        </div>
    );
}

export default BeneficiaryList;