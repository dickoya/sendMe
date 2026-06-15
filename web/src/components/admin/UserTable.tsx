import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Loader2, UserCheck, UserX } from 'lucide-react';
import Badge from '@/components/ui/Badge';
import { formatCurrency } from '@/utils/formatCurrency';
import { formatDateShort } from '@/utils/formatDate';
import type { UserResponse } from '@/types';

export interface UserTableProps {
    users: UserResponse[];
    onToggleStatus: (id: number) => Promise<unknown>;
}

/**
 * Tableau des utilisateurs avec activation/désactivation en ligne.
 *
 * Migration i18n (Phase 11) : namespace "admin" (clés users.table.*,
 * users.roles.*, users.status.*, users.buttons.*).
 */
function UserTable({ users, onToggleStatus }: UserTableProps) {
    const { t } = useTranslation('admin');
    const [togglingId, setTogglingId] = useState<number | null>(null);

    const handleToggle = async (id: number) => {
        setTogglingId(id);
        try {
            await onToggleStatus(id);
        } finally {
            setTogglingId(null);
        }
    };

    return (
        <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
                <thead>
                <tr className="border-b border-gray-100 text-xs uppercase text-gray-400">
                    <th className="py-3 font-medium">{t('users.table.user')}</th>
                    <th className="py-3 font-medium">{t('users.table.role')}</th>
                    <th className="py-3 font-medium">{t('users.table.status')}</th>
                    <th className="py-3 font-medium text-right">{t('users.table.transactions')}</th>
                    <th className="py-3 font-medium text-right">{t('users.table.volumeSent')}</th>
                    <th className="py-3 font-medium">{t('users.table.registeredOn')}</th>
                    <th className="py-3 font-medium text-right">{t('users.table.action')}</th>
                </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                {users.map((user) => (
                    <tr key={user.id} className="hover:bg-gray-50">
                        <td className="py-3">
                            <div className="flex items-center gap-3">
                                <div className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-full bg-brand-50 text-xs font-semibold text-brand-700">
                                    {user.firstName[0]}{user.lastName[0]}
                                </div>
                                <div className="min-w-0">
                                    <p className="truncate font-medium text-gray-900">
                                        {user.firstName} {user.lastName}
                                    </p>
                                    <p className="truncate text-xs text-gray-400">{user.email}</p>
                                </div>
                            </div>
                        </td>
                        <td className="py-3">
                            <Badge variant={user.role === 'ROLE_ADMIN' ? 'purple' : 'gray'}>
                                {user.role === 'ROLE_ADMIN' ? t('users.roles.admin') : t('users.roles.user')}
                            </Badge>
                        </td>
                        <td className="py-3">
                            <Badge variant={user.isActive ? 'success' : 'danger'}>
                                {user.isActive ? t('users.status.active') : t('users.status.suspended')}
                            </Badge>
                        </td>
                        <td className="py-3 text-right font-medium text-gray-900">
                            {user.totalTransactions}
                        </td>
                        <td className="py-3 text-right text-gray-700">
                            {formatCurrency(user.totalAmountSent, 'EUR')}
                        </td>
                        <td className="py-3 text-gray-500">{formatDateShort(user.createdAt)}</td>
                        <td className="py-3 text-right">
                            {user.role !== 'ROLE_ADMIN' && (
                                <button
                                    onClick={() => handleToggle(user.id)}
                                    disabled={togglingId === user.id}
                                    className={`inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors disabled:opacity-50 ${
                                        user.isActive
                                            ? 'bg-danger-50 text-danger-600 hover:bg-danger-100'
                                            : 'bg-accent-50 text-accent-600 hover:bg-accent-100'
                                    }`}
                                >
                                    {togglingId === user.id ? (
                                        <Loader2 size={14} className="animate-spin" />
                                    ) : user.isActive ? (
                                        <UserX size={14} />
                                    ) : (
                                        <UserCheck size={14} />
                                    )}
                                    {user.isActive ? t('users.buttons.suspend') : t('users.buttons.reactivate')}
                                </button>
                            )}
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}

export default UserTable;