import { useState } from 'react';
import { useTranslation } from 'react-i18next';
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
 * Migration i18n (Phase 7) : namespace "beneficiaries" pour le contenu
 * propre à la page, "common" pour les actions/boutons génériques.
 */
function Beneficiaries() {
    const { t } = useTranslation('beneficiaries');
    const { t: tCommon } = useTranslation('common');

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
            showToast(t('toasts.updated'), 'success');
        } else {
            await createBeneficiary(payload);
            showToast(t('toasts.created'), 'success');
        }
        setFormOpen(false);
    };

    const confirmDelete = async () => {
        if (!deleting) return;
        setIsDeleting(true);
        try {
            await removeBeneficiary(deleting.id);
            showToast(t('toasts.deleted'), 'success');
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
                title={t('pageTitle')}
                actions={
                    <Button size="sm" icon={<Plus size={16} />} onClick={openCreateForm}>
                        {t('addButton')}
                    </Button>
                }
            >
                {isLoading ? (
                    <Spinner label={tCommon('states.loading')} />
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
                title={editing ? t('modals.editTitle') : t('modals.createTitle')}
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
                title={t('modals.deleteTitle')}
                footer={
                    <>
                        <Button variant="outline" onClick={() => setDeleting(null)}>
                            {tCommon('actions.cancel')}
                        </Button>
                        <Button variant="danger" loading={isDeleting} onClick={confirmDelete}>
                            {tCommon('actions.delete')}
                        </Button>
                    </>
                }
            >
                <p className="text-sm text-gray-600">
                    {t('modals.deleteMessage', {
                        name: deleting ? `${deleting.firstName} ${deleting.lastName}` : '',
                    })}
                </p>
            </Modal>
        </div>
    );
}

export default Beneficiaries;