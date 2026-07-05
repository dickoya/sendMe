import { useTranslation } from 'react-i18next';
import { Search, X } from 'lucide-react';


export interface UserFiltersState {
  search: string;
  status: 'ALL' | 'ACTIVE' | 'INACTIVE';
  dateFrom: string;
  dateTo: string;
  sortBy: 'createdAt' | 'firstName' | 'email' | 'totalTransactions' | 'totalAmountSent';
  sortDir: 'asc' | 'desc';
}

interface UserFiltersProps {
  filters: UserFiltersState;
  onChange: (filters: UserFiltersState) => void;
}

/**
 * Barre de filtres pour la page AdminUsers.
 * - Recherche par nom/email
 * - Filtre par statut (actif/inactif)
 * - Filtre par date d'inscription
 * - Tri par différents champs
 * - Persistance via l'URL (gérée par le parent)
 */
export default function UserFilters({ filters, onChange }: UserFiltersProps) {
  const { t } = useTranslation('admin');

  const update = (partial: Partial<UserFiltersState>) => {
    onChange({ ...filters, ...partial });
  };

  const hasActiveFilters =
    filters.search !== '' ||
    filters.status !== 'ALL' ||
    filters.dateFrom !== '' ||
    filters.dateTo !== '';

  return (
    <div className="flex flex-col gap-3">
      {/* Ligne 1 : Recherche + Filtre statut */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[200px]">
          <Search
            size={16}
            className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
          />
          <input
            type="text"
            value={filters.search}
            onChange={(e) => update({ search: e.target.value })}
            placeholder={t('users.filters.searchPlaceholder')}
            className="w-full rounded-lg border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm placeholder-gray-400 focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100 dark:placeholder-gray-500"
          />
          {filters.search && (
            <button
              onClick={() => update({ search: '' })}
              className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              <X size={14} />
            </button>
          )}
        </div>

        <select
          value={filters.status}
          onChange={(e) => update({ status: e.target.value as UserFiltersState['status'] })}
          className="w-40 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
        >
          <option value="ALL">{t('users.filters.allStatuses')}</option>
          <option value="ACTIVE">{t('users.status.active')}</option>
          <option value="INACTIVE">{t('users.status.suspended')}</option>
        </select>

      </div>

      {/* Ligne 2 : Dates + Tri */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-2">
          <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
            {t('users.filters.from')}
          </label>
          <input
            type="date"
            value={filters.dateFrom}
            onChange={(e) => update({ dateFrom: e.target.value })}
            className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
          />
        </div>

        <div className="flex items-center gap-2">
          <label className="text-xs font-medium text-gray-500 dark:text-gray-400">
            {t('users.filters.to')}
          </label>
          <input
            type="date"
            value={filters.dateTo}
            onChange={(e) => update({ dateTo: e.target.value })}
            className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
          />
        </div>

        <select
          value={`${filters.sortBy}-${filters.sortDir}`}
          onChange={(e) => {
            const [sortBy, sortDir] = e.target.value.split('-') as [UserFiltersState['sortBy'], UserFiltersState['sortDir']];
            update({ sortBy, sortDir });
          }}
          className="w-48 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-accent-500 focus:outline-none dark:border-gray-700 dark:bg-gray-800 dark:text-gray-100"
        >
          <option value="createdAt-desc">{t('users.filters.sortNewest')}</option>
          <option value="createdAt-asc">{t('users.filters.sortOldest')}</option>
          <option value="firstName-asc">{t('users.filters.sortNameAsc')}</option>
          <option value="firstName-desc">{t('users.filters.sortNameDesc')}</option>
          <option value="email-asc">{t('users.filters.sortEmailAsc')}</option>
          <option value="email-desc">{t('users.filters.sortEmailDesc')}</option>
          <option value="totalTransactions-desc">{t('users.filters.sortMostTx')}</option>
          <option value="totalAmountSent-desc">{t('users.filters.sortMostVolume')}</option>
        </select>


        {hasActiveFilters && (
          <button
            onClick={() =>
              onChange({
                search: '',
                status: 'ALL',
                dateFrom: '',
                dateTo: '',
                sortBy: 'createdAt',
                sortDir: 'desc',
              })
            }
            className="text-sm text-accent-600 hover:text-accent-700 dark:text-accent-400"
          >
            {t('users.filters.clearAll')}
          </button>
        )}
      </div>
    </div>
  );
}
