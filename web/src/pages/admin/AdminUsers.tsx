import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Download, CheckSquare, Square } from 'lucide-react';

import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import Pagination from '@/components/ui/Pagination';

import UserFilters from '@/components/admin/UserFilters';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import { useAdminUsers } from '@/hooks/useAdminUsers';
import { useToast } from '@/store/notification/toastContext';
import { getErrorMessage } from '@/lib/utils';
import { exportCSV } from '@/utils/exportCSV';
import type { Role, UserResponse } from '@/types';

/**
 * Page de gestion des utilisateurs (admin).
 * - Filtres avancés (recherche, statut, dates, tri)
 * - Checkboxes pour sélection multiple
 * - Actions en masse (suspendre/réactiver)
 * - Export CSV
 */
function AdminUsers() {
    const { t } = useTranslation('admin');
    const { t: tCommon } = useTranslation('common');
    const { users, page, totalPages, setPage, filters, setFilters, isLoading, error, toggleStatus, batchToggle, updateRole } = useAdminUsers();
    const { showToast } = useToast();

    const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
    const [confirmBatch, setConfirmBatch] = useState<{ ids: number[]; active: boolean } | null>(null);
    const [confirmRole, setConfirmRole] = useState<{ id: number; name: string; newRole: Role } | null>(null);
    const [roleUpdating, setRoleUpdating] = useState(false);

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

    const toggleSelect = (id: number) => {
        setSelectedIds((prev) => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });
    };

    const toggleSelectAll = () => {
        if (selectedIds.size === users.length) {
            setSelectedIds(new Set());
        } else {
            setSelectedIds(new Set(users.map((u) => u.id)));
        }
    };

    const handleBatchToggle = async (active: boolean) => {
        const ids = Array.from(selectedIds);
        if (!ids.length) return;
        setConfirmBatch({ ids, active });
    };

    const confirmBatchAction = async () => {
        if (!confirmBatch) return;
        try {
            await batchToggle(confirmBatch.ids, confirmBatch.active);
            const action = confirmBatch.active ? 'users.toasts.reactivated' : 'users.toasts.suspended';
            showToast(t(action, { count: confirmBatch.ids.length }), confirmBatch.active ? 'success' : 'warning');
            setSelectedIds(new Set());
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        } finally {
            setConfirmBatch(null);
        }
    };

    // Gestion du rôle admin (promouvoir / rétrograder)
    const handleRoleClick = (user: UserResponse, newRole: Role) => {
        setConfirmRole({ id: user.id, name: `${user.firstName} ${user.lastName}`, newRole });
    };

    const confirmRoleAction = async () => {
        if (!confirmRole) return;
        setRoleUpdating(true);
        try {
            const updated = await updateRole(confirmRole.id, confirmRole.newRole);
            showToast(
                updated.role === 'ROLE_ADMIN'
                    ? t('users.toasts.promoted', { name: confirmRole.name })
                    : t('users.toasts.demoted', { name: confirmRole.name }),
                'success'
            );
            setConfirmRole(null);
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
            setConfirmRole(null);
        } finally {
            setRoleUpdating(false);
        }
    };

    const handleExportCSV = () => {
        exportCSV(
            users as unknown as Record<string, unknown>[],

            [
                { key: 'id', label: 'ID' },
                { key: 'firstName', label: t('users.table.user') },
                { key: 'lastName', label: t('users.table.user') },
                { key: 'email', label: 'Email' },
                { key: 'role', label: t('users.table.role') },
                { key: 'isActive', label: t('users.table.status'), format: (v) => (v ? t('users.status.active') : t('users.status.suspended')) },
                { key: 'totalTransactions', label: t('users.table.transactions') },
                { key: 'totalAmountSent', label: t('users.table.volumeSent'), format: (v) => String(v) },
                { key: 'createdAt', label: t('users.table.registeredOn'), format: (v) => new Date(v as string).toLocaleDateString() },
            ],
            'utilisateurs'
        );
        showToast(t('users.toasts.exported'), 'success');
    };

    return (
        <Card
            title={t('users.pageTitle')}
            actions={
                <button
                    onClick={handleExportCSV}
                    className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-400 dark:hover:bg-gray-800"
                >
                    <Download size={14} />
                    {t('users.buttons.exportCSV')}
                </button>
            }
        >
            {isLoading ? (
                <Spinner label={tCommon('states.loading')} />
            ) : error ? (
                <p className="py-6 text-center text-sm text-danger-600">{error}</p>
            ) : (
                <div className="flex flex-col gap-4">
                    {/* Filtres */}
                    <UserFilters filters={filters} onChange={setFilters} />

                    {/* Barre d'actions en masse */}
                    {selectedIds.size > 0 && (
                        <div className="flex items-center gap-3 rounded-lg bg-brand-50 px-4 py-2 dark:bg-brand-900/20">
                            <span className="text-sm font-medium text-brand-700 dark:text-brand-300">
                                {selectedIds.size} sélectionné(s)
                            </span>
                            <button
                                onClick={() => handleBatchToggle(true)}
                                className="rounded-lg bg-accent-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-accent-600"
                            >
                                {t('users.buttons.reactivate')}
                            </button>
                            <button
                                onClick={() => handleBatchToggle(false)}
                                className="rounded-lg bg-danger-500 px-3 py-1.5 text-xs font-medium text-white hover:bg-danger-600"
                            >
                                {t('users.buttons.suspend')}
                            </button>
                            <button
                                onClick={() => setSelectedIds(new Set())}
                                className="text-sm text-gray-500 hover:text-gray-700"
                            >
                                {t('users.buttons.clearSelection')}
                            </button>
                        </div>
                    )}

                    {/* Checkbox "Tout sélectionner" + Table */}
                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                            <thead>
                                <tr className="border-b border-gray-100 text-xs uppercase text-gray-400">
                                    <th className="w-10 py-3">
                                        <button onClick={toggleSelectAll} className="text-gray-400 hover:text-gray-600">
                                            {selectedIds.size === users.length && users.length > 0 ? (
                                                <CheckSquare size={16} />
                                            ) : (
                                                <Square size={16} />
                                            )}
                                        </button>
                                    </th>
                                    <th className="py-3 font-medium">{t('users.table.user')}</th>
                                    <th className="py-3 font-medium">{t('users.table.role')}</th>
                                    <th className="py-3 font-medium">{t('users.table.status')}</th>
                                    <th className="py-3 font-medium text-right">{t('users.table.transactions')}</th>
                                    <th className="py-3 font-medium text-right">{t('users.table.volumeSent')}</th>
                                    <th className="py-3 font-medium">{t('users.table.registeredOn')}</th>
                                    <th className="py-3 font-medium">{t('users.table.action')}</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50">
                                {users.map((user) => (
                                    <tr key={user.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50">
                                        <td className="py-3">
                                            <button onClick={() => toggleSelect(user.id)} className="text-gray-400 hover:text-gray-600">
                                                {selectedIds.has(user.id) ? (
                                                    <CheckSquare size={16} className="text-accent-500" />
                                                ) : (
                                                    <Square size={16} />
                                                )}
                                            </button>
                                        </td>
                                        <td className="py-3">
                                            <div className="font-medium text-gray-900 dark:text-gray-100">
                                                {user.firstName} {user.lastName}
                                            </div>
                                            <div className="text-xs text-gray-400">{user.email}</div>
                                        </td>
                                        <td className="py-3">
                                            <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-600 dark:bg-gray-800 dark:text-gray-300">
                                                {t(`users.roles.${user.role === 'ROLE_ADMIN' ? 'admin' : 'user'}`)}
                                            </span>
                                        </td>
                                        <td className="py-3">
                                            <span
                                                className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${
                                                    user.isActive
                                                        ? 'bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400'
                                                        : 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400'
                                                }`}
                                            >
                                                <span
                                                    className={`h-1.5 w-1.5 rounded-full ${
                                                        user.isActive ? 'bg-green-500' : 'bg-red-500'
                                                    }`}
                                                />
                                                {user.isActive ? t('users.status.active') : t('users.status.suspended')}
                                            </span>
                                        </td>
                                        <td className="py-3 text-right text-gray-700 dark:text-gray-300">
                                            {user.totalTransactions}
                                        </td>
                                        <td className="py-3 text-right text-gray-700 dark:text-gray-300">
                                            {user.totalAmountSent.toLocaleString()} €
                                        </td>
                                        <td className="py-3 text-gray-500">
                                            {new Date(user.createdAt).toLocaleDateString()}
                                        </td>
                                        <td className="py-3">
                                            <div className="flex items-center justify-end gap-2">
                                                <button
                                                    onClick={() =>
                                                        handleRoleClick(user, user.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN')
                                                    }
                                                    className="rounded-lg bg-purple-50 px-3 py-1 text-xs font-medium text-purple-600 hover:bg-purple-100 dark:bg-purple-900/20 dark:text-purple-400"
                                                >
                                                    {user.role === 'ROLE_ADMIN' ? t('users.buttons.demote') : t('users.buttons.promote')}
                                                </button>
                                                <button
                                                    onClick={() => handleToggle(user.id)}
                                                    className={`rounded-lg px-3 py-1 text-xs font-medium ${
                                                        user.isActive
                                                            ? 'bg-danger-50 text-danger-600 hover:bg-danger-100 dark:bg-danger-900/20 dark:text-danger-400'
                                                            : 'bg-accent-50 text-accent-600 hover:bg-accent-100 dark:bg-accent-900/20 dark:text-accent-400'
                                                    }`}
                                                >
                                                    {user.isActive ? t('users.buttons.suspend') : t('users.buttons.reactivate')}
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
                </div>
            )}

            {/* Confirmation pour action en masse */}
            <ConfirmDialog
                isOpen={!!confirmBatch}
                onClose={() => setConfirmBatch(null)}
                onConfirm={confirmBatchAction}
                title={confirmBatch?.active ? t('users.confirm.reactivateTitle') : t('users.confirm.suspendTitle')}
                message={
                    confirmBatch?.active
                        ? t('users.confirm.reactivateMessage', { count: confirmBatch?.ids.length })
                        : t('users.confirm.suspendMessage', { count: confirmBatch?.ids.length })
                }
                confirmLabel={confirmBatch?.active ? t('users.buttons.reactivate') : t('users.buttons.suspend')}
                variant={confirmBatch?.active ? 'primary' : 'danger'}
            />

            {/* Confirmation pour changement de rôle */}
            <ConfirmDialog
                isOpen={!!confirmRole}
                onClose={() => {
                    if (!roleUpdating) setConfirmRole(null);
                }}
                onConfirm={confirmRoleAction}
                title={
                    confirmRole?.newRole === 'ROLE_ADMIN'
                        ? t('users.confirm.promoteTitle')
                        : t('users.confirm.demoteTitle')
                }
                message={
                    confirmRole?.newRole === 'ROLE_ADMIN'
                        ? t('users.confirm.promoteMessage', { name: confirmRole?.name })
                        : t('users.confirm.demoteMessage', { name: confirmRole?.name })
                }
                confirmLabel={
                    confirmRole?.newRole === 'ROLE_ADMIN'
                        ? t('users.buttons.promote')
                        : t('users.buttons.demote')
                }
                variant="primary"
                isLoading={roleUpdating}
            />

        </Card>
    );
}

export default AdminUsers;
