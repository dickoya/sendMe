import { useState } from 'react';
import { Plus } from 'lucide-react';
import Card from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import Spinner from '@/components/ui/Spinner';
import BeneficiaryList from '@/components/beneficiary/BeneficiaryList';
import BeneficiaryForm from '@/components/forms/BeneficiaryForm';
import { useBeneficiary } from '@/hooks/useBeneficiary';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';
import type { Beneficiary, BeneficiaryFormData } from '@/types';

/**
 * Page de gestion du carnet de bénéficiaires (CRUD complet).
 *
 * - Liste tous les bénéficiaires de l'utilisateur
 * - Modale de création/édition (formulaire adaptatif selon le canal)
 * - Confirmation avant suppression
 */
function Beneficiaries() {
    const {
        beneficiaries,
        isLoading,
        createBeneficiary,
        updateBeneficiary,
        removeBeneficiary,
    } = useBeneficiary();
    const { showToast } = useToast();

    const [formOpen, setFormOpen] = useState(false);
    const [editing, setEditing] = useState<Beneficiary | null>(null);
    const [deleting, setDeleting] = useState<Beneficiary | null>(null);
    const [isDeleting, setIsDeleting] = useState(false);

    const openCreateForm = () => {
        setEditing(null);
        setFormOpen(true);
    };

    const openEditForm = (beneficiary: Beneficiary) => {
        setEditing(beneficiary);
        setFormOpen(true);
    };

    const handleSubmit = async (payload: BeneficiaryFormData) => {
        if (editing) {
            await updateBeneficiary(editing.id, payload);
            showToast('Bénéficiaire mis à jour avec succès', 'success');
        } else {
            await createBeneficiary(payload);
            showToast('Bénéficiaire ajouté avec succès', 'success');
        }
        setFormOpen(false);
    };

    const confirmDelete = async () => {
        if (!deleting) return;
        setIsDeleting(true);
        try {
            await removeBeneficiary(deleting.id);
            showToast('Bénéficiaire supprimé', 'success');
            setDeleting(null);
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        } finally {
            setIsDeleting(false);
        }
    };

    return (
        <div className="flex flex-col gap-6">
            <Card
                title="Mes bénéficiaires"
                actions={
                    <Button size="sm" icon={<Plus size={16} />} onClick={openCreateForm}>
                        Ajouter
                    </Button>
                }
            >
                {isLoading ? (
                    <Spinner label="Chargement des bénéficiaires..." />
                ) : (
                    <BeneficiaryList
                        beneficiaries={beneficiaries}
                        onEdit={openEditForm}
                        onDelete={setDeleting}
                    />
                )}
            </Card>

            {/* Modale création/édition */}
            <Modal
                isOpen={formOpen}
                onClose={() => setFormOpen(false)}
                title={editing ? 'Modifier le bénéficiaire' : 'Ajouter un bénéficiaire'}
                size="lg"
            >
                <BeneficiaryForm
                    initialData={editing ?? undefined}
                    onSubmit={handleSubmit}
                    onCancel={() => setFormOpen(false)}
                />
            </Modal>

            {/* Modale confirmation suppression */}
            <Modal
                isOpen={!!deleting}
                onClose={() => setDeleting(null)}
                title="Supprimer ce bénéficiaire ?"
                footer={
                    <>
                        <Button variant="outline" onClick={() => setDeleting(null)}>
                            Annuler
                        </Button>
                        <Button variant="danger" loading={isDeleting} onClick={confirmDelete}>
                            Supprimer
                        </Button>
                    </>
                }
            >
                <p className="text-sm text-gray-600">
                    Êtes-vous sûr de vouloir supprimer{' '}
                    <strong>
                        {deleting?.firstName} {deleting?.lastName}
                    </strong>{' '}
                    de votre carnet de bénéficiaires ? Cette action est irréversible.
                </p>
            </Modal>
        </div>
    );
}

export default Beneficiaries;