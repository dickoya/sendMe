import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import Pagination from '@/components/ui/Pagination';
import UserTable from '@/components/admin/UserTable'; // Cette ligne deviendra verte !
import { useAdminUsers } from '@/hooks/useAdminUsers';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';

/**
 * Page de gestion des utilisateurs (admin).
 */
function AdminUsers() {
    const { users, page, totalPages, setPage, isLoading, error, toggleStatus } = useAdminUsers();
    const { showToast } = useToast();

    const handleToggle = async (id: number) => {
        try {
            const updated = await toggleStatus(id);
            showToast(
                updated.isActive
                    ? `Compte de ${updated.firstName} ${updated.lastName} réactivé`
                    : `Compte de ${updated.firstName} ${updated.lastName} suspendu`,
                updated.isActive ? 'success' : 'warning'
            );
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        }
    };

    return (
        <Card title="Gestion des utilisateurs">
            {isLoading ? (
                <Spinner label="Chargement des utilisateurs..." />
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