import React, { useState, useEffect, useCallback } from 'react';
import auditLogService from '../../services/auditLogService';
import type { AuditLogEntry, AuditLogFilters } from '../../services/auditLogService';
import type { Page } from '../../types';

// ── Constantes ──────────────────────────────────────────────────────────
const CATEGORIES = ['AUTH', 'TRANSFER', 'ADMIN', 'CONFIG', 'SYSTEM'] as const;
const EVENT_TYPES = [
    'REGISTER', 'LOGIN', 'LOGIN_FAILED', 'LOGOUT',
    'TRANSFER_CREATED', 'TRANSFER_COMPLETED', 'TRANSFER_FAILED', 'TRANSFER_REFUNDED',
    'USER_TOGGLE', 'BATCH_TOGGLE', 'FEE_UPDATE', 'MAINTENANCE',
    'CONFIG_CHANGE', 'ERROR', 'JOB_EXECUTED',
] as const;
const STATUSES = ['SUCCESS', 'FAILED', 'PENDING'] as const;

const CATEGORY_COLORS: Record<string, string> = {
    AUTH: 'bg-blue-100 text-blue-800',
    TRANSFER: 'bg-green-100 text-green-800',
    ADMIN: 'bg-purple-100 text-purple-800',
    CONFIG: 'bg-yellow-100 text-yellow-800',
    SYSTEM: 'bg-red-100 text-red-800',
};

const STATUS_COLORS: Record<string, string> = {
    SUCCESS: 'bg-green-100 text-green-800',
    FAILED: 'bg-red-100 text-red-800',
    PENDING: 'bg-yellow-100 text-yellow-800',
};

const EVENT_TYPE_LABELS: Record<string, string> = {
    REGISTER: 'Inscription',
    LOGIN: 'Connexion',
    LOGIN_FAILED: 'Échec connexion',
    LOGOUT: 'Déconnexion',
    TRANSFER_CREATED: 'Transfert créé',
    TRANSFER_COMPLETED: 'Transfert réussi',
    TRANSFER_FAILED: 'Transfert échoué',
    TRANSFER_REFUNDED: 'Transfert remboursé',
    USER_TOGGLE: 'Activation/Désactivation utilisateur',
    BATCH_TOGGLE: 'Action groupée',
    FEE_UPDATE: 'Mise à jour frais',
    MAINTENANCE: 'Maintenance',
    CONFIG_CHANGE: 'Configuration',
    ERROR: 'Erreur système',
    JOB_EXECUTED: 'Job exécuté',
};

// ── Composant ───────────────────────────────────────────────────────────
const AdminLogs: React.FC = () => {
    const [logs, setLogs] = useState<Page<AuditLogEntry> | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(0);
    const [filters, setFilters] = useState<AuditLogFilters>({});
    const [showFilters, setShowFilters] = useState(false);
    const [selectedLog, setSelectedLog] = useState<AuditLogEntry | null>(null);

    const fetchLogs = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const hasFilters = filters.category || filters.eventType || filters.status || filters.userEmail;
            const result = hasFilters
                ? await auditLogService.search(filters, page)
                : await auditLogService.getAll(page);
            setLogs(result);
        } catch (err) {
            setError('Erreur lors du chargement des logs.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, [filters, page]);

    useEffect(() => {
        fetchLogs();
    }, [fetchLogs]);

    const handleFilterChange = (key: keyof AuditLogFilters, value: string | undefined) => {
        setFilters(prev => ({ ...prev, [key]: value || undefined }));
        setPage(0);
    };

    const resetFilters = () => {
        setFilters({});
        setPage(0);
    };

    const formatDate = (dateStr: string) => {
        const date = new Date(dateStr);
        return date.toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
        });
    };

    const getEventTypeLabel = (type: string) => EVENT_TYPE_LABELS[type] || type;

    return (
        <div className="space-y-6">
            {/* En-tête */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Logs d'Audit</h1>
                    <p className="mt-1 text-sm text-gray-500">
                        Tous les événements de la plateforme centralisés (authentification, transferts, actions admin, configuration, système)
                    </p>
                </div>
                <button
                    onClick={() => setShowFilters(!showFilters)}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                        showFilters
                            ? 'bg-indigo-100 text-indigo-700'
                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                >
                    {showFilters ? 'Masquer filtres' : 'Filtres'}
                </button>
            </div>

            {/* Filtres */}
            {showFilters && (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                    <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Catégorie</label>
                            <select
                                value={filters.category || ''}
                                onChange={e => handleFilterChange('category', e.target.value || undefined)}
                                className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
                            >
                                <option value="">Toutes</option>
                                {CATEGORIES.map(cat => (
                                    <option key={cat} value={cat}>{cat}</option>
                                ))}
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Type d'événement</label>
                            <select
                                value={filters.eventType || ''}
                                onChange={e => handleFilterChange('eventType', e.target.value || undefined)}
                                className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
                            >
                                <option value="">Tous</option>
                                {EVENT_TYPES.map(type => (
                                    <option key={type} value={type}>{getEventTypeLabel(type)}</option>
                                ))}
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Statut</label>
                            <select
                                value={filters.status || ''}
                                onChange={e => handleFilterChange('status', e.target.value || undefined)}
                                className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
                            >
                                <option value="">Tous</option>
                                {STATUSES.map(status => (
                                    <option key={status} value={status}>{status}</option>
                                ))}
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                            <input
                                type="text"
                                value={filters.userEmail || ''}
                                onChange={e => handleFilterChange('userEmail', e.target.value || undefined)}
                                placeholder="user@example.com"
                                className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Date début</label>
                            <input
                                type="date"
                                value={filters.dateFrom || ''}
                                onChange={e => handleFilterChange('dateFrom', e.target.value || undefined)}
                                className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Date fin</label>
                            <input
                                type="date"
                                value={filters.dateTo || ''}
                                onChange={e => handleFilterChange('dateTo', e.target.value || undefined)}
                                className="w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 text-sm"
                            />
                        </div>
                    </div>
                    <div className="mt-3 flex justify-end">
                        <button onClick={resetFilters} className="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900">
                            Réinitialiser
                        </button>
                    </div>
                </div>
            )}

            {/* Statistiques rapides */}
            {logs && !loading && (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                        <p className="text-sm text-gray-500">Total</p>
                        <p className="text-2xl font-bold text-gray-900">{logs.totalElements}</p>
                    </div>
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                        <p className="text-sm text-gray-500">Pages</p>
                        <p className="text-2xl font-bold text-gray-900">{logs.totalPages}</p>
                    </div>
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                        <p className="text-sm text-gray-500">Page actuelle</p>
                        <p className="text-2xl font-bold text-gray-900">{logs.number + 1}</p>
                    </div>
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                        <p className="text-sm text-gray-500">Par page</p>
                        <p className="text-2xl font-bold text-gray-900">{logs.size}</p>
                    </div>
                </div>
            )}

            {/* Tableau */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                {loading ? (
                    <div className="flex items-center justify-center h-64">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
                        <span className="ml-3 text-gray-500">Chargement des logs...</span>
                    </div>
                ) : error ? (
                    <div className="flex items-center justify-center h-64">
                        <div className="text-center">
                            <svg className="mx-auto h-12 w-12 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
                            </svg>
                            <p className="mt-2 text-sm text-red-600">{error}</p>
                            <button onClick={fetchLogs} className="mt-2 text-sm text-indigo-600 hover:text-indigo-500">Réessayer</button>
                        </div>
                    </div>
                ) : !logs || logs.content.length === 0 ? (
                    <div className="flex items-center justify-center h-64">
                        <div className="text-center">
                            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            <p className="mt-2 text-sm text-gray-500">Aucun log trouvé</p>
                            <p className="text-xs text-gray-400">Les logs apparaîtront ici une fois que des événements se produiront.</p>
                        </div>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Catégorie</th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Événement</th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Statut</th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Utilisateur</th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Message</th>
                                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">IP</th>
                                    <th className="px-4 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {logs!.content.map(log => (
                                    <tr key={log.id} className="hover:bg-gray-50 transition-colors">
                                        <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">{formatDate(log.createdAt)}</td>
                                        <td className="px-4 py-3 whitespace-nowrap">
                                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${CATEGORY_COLORS[log.category] || 'bg-gray-100 text-gray-800'}`}>
                                                {log.category}
                                            </span>
                                        </td>
                                        <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-900">{getEventTypeLabel(log.eventType)}</td>
                                        <td className="px-4 py-3 whitespace-nowrap">
                                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[log.status] || 'bg-gray-100 text-gray-800'}`}>
                                                {log.status}
                                            </span>
                                        </td>
                                        <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500">{log.userEmail || '-'}</td>
                                        <td className="px-4 py-3 text-sm text-gray-500 max-w-xs truncate">{log.message || '-'}</td>
                                        <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 font-mono">{log.ipAddress || '-'}</td>
                                        <td className="px-4 py-3 whitespace-nowrap text-right text-sm">
                                            <button onClick={() => setSelectedLog(log)} className="text-indigo-600 hover:text-indigo-900">Détails</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {/* Pagination */}
                {logs && logs.totalPages > 1 && (
                    <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
                        <div className="flex-1 flex justify-between sm:hidden">
                            <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={logs.first}
                                className="relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50">Précédent</button>
                            <button onClick={() => setPage(p => p + 1)} disabled={logs.last}
                                className="ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-50">Suivant</button>
                        </div>
                        <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                            <p className="text-sm text-gray-700">
                                Page <span className="font-medium">{logs.number + 1}</span> sur{' '}
                                <span className="font-medium">{logs.totalPages}</span>
                            </p>
                            <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
                                <button onClick={() => setPage(0)} disabled={logs.first}
                                    className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50">
                                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M15.707 15.707a1 1 0 01-1.414 0l-5-5a1 1 0 010-1.414l5-5a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 010 1.414zm-6 0a1 1 0 01-1.414 0l-5-5a1 1 0 010-1.414l5-5a1 1 0 011.414 1.414L5.414 10l4.293 4.293a1 1 0 010 1.414z" clipRule="evenodd" /></svg>
                                </button>
                                <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={logs.first}
                                    className="relative inline-flex items-center px-2 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50">
                                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" /></svg>
                                </button>
                                {Array.from({ length: Math.min(5, logs.totalPages) }, (_, i) => {
                                    const start = Math.max(0, Math.min(logs.number - 2, logs.totalPages - 5));
                                    const pageNum = start + i;
                                    return (
                                        <button key={pageNum} onClick={() => setPage(pageNum)}
                                            className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                                                pageNum === logs.number
                                                    ? 'z-10 bg-indigo-50 border-indigo-500 text-indigo-600'
                                                    : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                                            }`}>{pageNum + 1}</button>
                                    );
                                })}
                                <button onClick={() => setPage(p => p + 1)} disabled={logs.last}
                                    className="relative inline-flex items-center px-2 py-2 border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50">
                                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" /></svg>
                                </button>
                                <button onClick={() => setPage(logs.totalPages - 1)} disabled={logs.last}
                                    className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50">
                                    <svg className="h-5 w-5" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M10.293 15.707a1 1 0 010-1.414L14.586 10l-4.293-4.293a1 1 0 111.414-1.414l5 5a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0z" clipRule="evenodd" /></svg>
                                </button>
                            </nav>
                        </div>
                    </div>
                )}
            </div>

            {/* Modal Détails */}
            {selectedLog && (
                <div className="fixed inset-0 z-50 overflow-y-auto">
                    <div className="flex items-center justify-center min-h-screen px-4">
                        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={() => setSelectedLog(null)}></div>
                        <div className="relative bg-white rounded-lg shadow-xl max-w-2xl w-full p-6">
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-lg font-medium text-gray-900">Détails du log</h3>
                                <button onClick={() => setSelectedLog(null)} className="text-gray-400 hover:text-gray-500">
                                    <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </div>
                            <div className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">ID</label>
                                        <p className="mt-1 text-sm text-gray-900">#{selectedLog.id}</p>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Date</label>
                                        <p className="mt-1 text-sm text-gray-900">{formatDate(selectedLog.createdAt)}</p>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Catégorie</label>
                                        <span className={`mt-1 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${CATEGORY_COLORS[selectedLog.category] || 'bg-gray-100 text-gray-800'}`}>
                                            {selectedLog.category}
                                        </span>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Type d'événement</label>
                                        <p className="mt-1 text-sm text-gray-900">{getEventTypeLabel(selectedLog.eventType)}</p>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Statut</label>
                                        <span className={`mt-1 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[selectedLog.status] || 'bg-gray-100 text-gray-800'}`}>
                                            {selectedLog.status}
                                        </span>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Utilisateur</label>
                                        <p className="mt-1 text-sm text-gray-900">{selectedLog.userEmail || '-'}</p>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">ID Utilisateur</label>
                                        <p className="mt-1 text-sm text-gray-900">{selectedLog.userId || '-'}</p>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Ressource</label>
                                        <p className="mt-1 text-sm text-gray-900">
                                            {selectedLog.resourceType ? `${selectedLog.resourceType} #${selectedLog.resourceId}` : '-'}
                                        </p>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Adresse IP</label>
                                        <p className="mt-1 text-sm text-gray-900 font-mono">{selectedLog.ipAddress || '-'}</p>
                                    </div>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-500">User-Agent</label>
                                        <p className="mt-1 text-sm text-gray-900 break-all">{selectedLog.userAgent || '-'}</p>
                                    </div>
                                </div>
                                {selectedLog.message && (
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Message</label>
                                        <p className="mt-1 text-sm text-gray-900 bg-gray-50 rounded-md p-3">{selectedLog.message}</p>
                                    </div>
                                )}
                                {selectedLog.details && (
                                    <div>
                                        <label className="block text-sm font-medium text-gray-500">Détails (JSON)</label>
                                        <pre className="mt-1 text-sm text-gray-900 bg-gray-50 rounded-md p-3 overflow-x-auto">
                                            {(() => {
                                                try { return JSON.stringify(JSON.parse(selectedLog.details), null, 2); }
                                                catch { return selectedLog.details; }
                                            })()}
                                        </pre>
                                    </div>
                                )}
                            </div>
                            <div className="mt-6 flex justify-end">
                                <button onClick={() => setSelectedLog(null)}
                                    className="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 text-sm font-medium">Fermer</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminLogs;
