import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { Clock, User, Shield, Settings, AlertTriangle, Info } from 'lucide-react';
import Card from '@/components/ui/Card';
import Spinner from '@/components/ui/Spinner';
import Pagination from '@/components/ui/Pagination';
import { adminService } from '@/services/adminService';
import { getErrorMessage } from '@/lib/utils';
import { formatDate } from '@/utils/formatDate';
import type { AdminLogEntry } from '@/types';

export interface AdminLogFilters {

  action: string;
  adminEmail: string;
  dateFrom: string;
  dateTo: string;
}

const ACTION_ICONS: Record<string, React.ReactNode> = {
  USER_TOGGLE: <User size={14} />,
  USER_DELETE: <User size={14} />,
  FEE_UPDATE: <Settings size={14} />,
  MAINTENANCE: <AlertTriangle size={14} />,
  LOGIN: <Shield size={14} />,
};

const ACTION_COLORS: Record<string, string> = {
  USER_TOGGLE: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
  USER_DELETE: 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400',
  FEE_UPDATE: 'bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400',
  MAINTENANCE: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
  LOGIN: 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400',
};

/**
 * Page des logs d'activité admin.
 * Affiche l'historique des actions des administrateurs avec filtres.
 */
function AdminLogs() {
  const { t } = useTranslation('admin');

  const [logs, setLogs] = useState<AdminLogEntry[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [filters, setFilters] = useState<AdminLogFilters>({
    action: '',
    adminEmail: '',
    dateFrom: '',
    dateTo: '',
  });

  const fetchLogs = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('size', '20');
      if (filters.action) params.set('action', filters.action);
      if (filters.adminEmail) params.set('adminEmail', filters.adminEmail);
      if (filters.dateFrom) params.set('dateFrom', filters.dateFrom);
      if (filters.dateTo) params.set('dateTo', filters.dateTo);

      const result = await adminService.getLogs(page, 20, filters);
      setLogs(result.content);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }, [page, filters]);

  useEffect(() => {
    fetchLogs();
  }, [fetchLogs]);

  const handleFilterChange = (partial: Partial<AdminLogFilters>) => {
    setFilters((prev) => ({ ...prev, ...partial }));
    setPage(0);
  };

  const hasActiveFilters = filters.action || filters.adminEmail || filters.dateFrom || filters.dateTo;

  return (
    <Card
      title={t('logs.pageTitle')}
      actions={
        <span className="text-sm text-gray-400">{t('logs.subtitle')}</span>
      }
    >
      <div className="flex flex-col gap-4">
        {/* Filtres */}
        <div className="flex flex-wrap items-center gap-3">
          <select
            value={filters.action}
            onChange={(e) => handleFilterChange({ action: e.target.value })}
            className="w-44 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
          >
            <option value="">{t('logs.filters.allActions')}</option>
            <option value="USER_TOGGLE">{t('logs.actions.USER_TOGGLE')}</option>
            <option value="USER_DELETE">{t('logs.actions.USER_DELETE')}</option>
            <option value="FEE_UPDATE">{t('logs.actions.FEE_UPDATE')}</option>
            <option value="MAINTENANCE">{t('logs.actions.MAINTENANCE')}</option>
            <option value="LOGIN">{t('logs.actions.LOGIN')}</option>
          </select>

          <input
            type="text"
            value={filters.adminEmail}
            onChange={(e) => handleFilterChange({ adminEmail: e.target.value })}
            placeholder={t('logs.filters.adminPlaceholder')}
            className="w-48 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm placeholder-gray-400 focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:placeholder-gray-500"
          />

          <input
            type="date"
            value={filters.dateFrom}
            onChange={(e) => handleFilterChange({ dateFrom: e.target.value })}
            className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
          />

          <input
            type="date"
            value={filters.dateTo}
            onChange={(e) => handleFilterChange({ dateTo: e.target.value })}
            className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
          />

          {hasActiveFilters && (
            <button
              onClick={() => {
                setFilters({ action: '', adminEmail: '', dateFrom: '', dateTo: '' });
                setPage(0);
              }}
              className="text-sm text-accent-600 hover:text-accent-700 dark:text-accent-400"
            >
              {t('logs.filters.clearAll')}
            </button>
          )}
        </div>

        {/* Tableau */}
        {isLoading ? (
          <Spinner label={t('loading', { ns: 'common' })} />
        ) : error ? (
          <p className="py-6 text-center text-sm text-danger-600">{error}</p>
        ) : logs.length === 0 ? (
          <div className="flex flex-col items-center gap-2 py-12 text-center">
            <Clock size={24} className="text-gray-300" />
            <p className="font-medium text-gray-500">{t('logs.empty.title')}</p>
            <p className="text-sm text-gray-400">{t('logs.empty.message')}</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-gray-100 text-xs uppercase text-gray-400">
                  <th className="py-3 font-medium">{t('logs.table.action')}</th>
                  <th className="py-3 font-medium">{t('logs.table.admin')}</th>
                  <th className="py-3 font-medium">{t('logs.table.details')}</th>
                  <th className="py-3 font-medium">{t('logs.table.date')}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50 dark:hover:bg-gray-800/50">
                    <td className="py-3">
                      <span
                        className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium ${
                          ACTION_COLORS[log.action] || 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300'
                        }`}
                      >
                        {ACTION_ICONS[log.action] || <Info size={14} />}
                        {t(`logs.actions.${log.action}`, log.action)}
                      </span>
                    </td>
                    <td className="py-3 text-gray-700 dark:text-gray-300">
                      <div className="font-medium text-gray-900 dark:text-gray-100">{log.adminEmail}</div>
                    </td>

                    <td className="max-w-xs truncate py-3 text-gray-600 dark:text-gray-400">
                      {log.details}
                    </td>
                    <td className="whitespace-nowrap py-3 text-gray-500">
                      {formatDate(log.createdAt)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </Card>
  );
}

export default AdminLogs;
