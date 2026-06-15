import { useTranslation } from 'react-i18next';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import Pagination from '@/components/ui/Pagination';
import UserTable from '@/components/admin/UserTable';
import { useAdminUsers } from '@/hooks/useAdminUsers';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Page de gestion des utilisateurs (admin).
 *
 * Migration i18n (Phase 11) : namespace "admin" (clés users.*),
 * "common" pour l'état de chargement.
 */
function AdminUsers() {
    const { t } = useTranslation('admin');
    const { t: tCommon } = useTranslation('common');
    const { users, page, totalPages, setPage, isLoading, error, toggleStatus } = useAdminUsers();
    const { showToast } = useToast();

    const handleToggle = async (id: number) => {
        try {
            const updated = await toggleStatus(id);
            const name = `${updated.firstName} ${updated.lastName}`;
            showToast(
                updated.isActive
                    ? t('users.toasts.reactivated', { name })
                    : t('users.toasts.suspended', { name }),
                updated.isActive ? 'success' : 'warning'
            );
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        }
    };

    return (
        <Card title={t('users.pageTitle')}>
            {isLoading ? (
                <Spinner label={tCommon('states.loading')} />
            ) : error ? (
                <p className="py-6 text-center text-sm text-danger-600">{error}</p>
            ) : (
                <>
                    <UserTable users={users} onToggleStatus={handleToggle} />
                    <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
                </>
            )}
        </Card>
    );
}

export default AdminUsers;