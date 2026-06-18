import { useTranslation } from 'react-i18next';
import { CheckCircle2, Circle, XCircle, Loader2 } from 'lucide-react';
import type { Transaction } from '@/types';
import { formatDate } from '@/utils/formatDate';

export interface TransferTimelineProps {
    transaction: Transaction;
}

/**
 * Timeline visuelle détaillant les étapes du cycle de vie d'un transfert.
 *
 * Affiche une chronologie avec :
 * - Étapes complétées (cercle vert + checkmark)
 * - Étape en cours (cercle bleu animé)
 * - Étapes à venir (cercle gris)
 * - Étape d'échec (cercle rouge)
 *
 * Les étapes sont déduites du statut actuel et des dates createdAt/updatedAt.
 */
function TransferTimeline({ transaction }: TransferTimelineProps) {
    const { t } = useTranslation('common');

    // Définition des étapes de la timeline
    interface TimelineStep {
        key: string;
        label: string;
        status: 'completed' | 'current' | 'pending' | 'failed';
        timestamp?: string;
    }

    const buildSteps = (): TimelineStep[] => {
        const { status, createdAt, updatedAt } = transaction;

        switch (status) {
            case 'PENDING':
                return [
                    { key: 'initiated', label: t('timeline.initiated'), status: 'completed', timestamp: createdAt },
                    { key: 'pendingOperator', label: t('timeline.pendingOperator'), status: 'current' },
                    { key: 'processing', label: t('timeline.processing'), status: 'pending' },
                    { key: 'completed', label: t('timeline.completed'), status: 'pending' },
                ];

            case 'PROCESSING':
                return [
                    { key: 'initiated', label: t('timeline.initiated'), status: 'completed', timestamp: createdAt },
                    { key: 'pendingOperator', label: t('timeline.pendingOperator'), status: 'completed', timestamp: createdAt },
                    { key: 'processing', label: t('timeline.processing'), status: 'current' },
                    { key: 'completed', label: t('timeline.completed'), status: 'pending' },
                ];

            case 'COMPLETED':
                return [
                    { key: 'initiated', label: t('timeline.initiated'), status: 'completed', timestamp: createdAt },
                    { key: 'pendingOperator', label: t('timeline.pendingOperator'), status: 'completed', timestamp: createdAt },
                    { key: 'processing', label: t('timeline.processing'), status: 'completed', timestamp: updatedAt },
                    { key: 'completed', label: t('timeline.completed'), status: 'completed', timestamp: updatedAt },
                ];

            case 'FAILED':
                return [
                    { key: 'initiated', label: t('timeline.initiated'), status: 'completed', timestamp: createdAt },
                    { key: 'failed', label: t('timeline.failed'), status: 'failed', timestamp: updatedAt },
                ];

            case 'REFUNDED':
                return [
                    { key: 'initiated', label: t('timeline.initiated'), status: 'completed', timestamp: createdAt },
                    { key: 'failed', label: t('timeline.failed'), status: 'failed', timestamp: updatedAt },
                    { key: 'refunded', label: t('timeline.refunded'), status: 'completed', timestamp: updatedAt },
                ];

            default:
                return [];
        }
    };

    const steps = buildSteps();

    if (steps.length === 0) return null;

    return (
        <div className="rounded-xl bg-gray-50 p-4">
            <h4 className="mb-4 text-sm font-semibold text-gray-700">{t('timeline.title')}</h4>
            <div className="flex flex-col gap-0">
                {steps.map((step, index) => {
                    const isLast = index === steps.length - 1;

                    return (
                        <div key={step.key} className="flex gap-3">
                            {/* Colonne des icônes + ligne de connexion */}
                            <div className="flex flex-col items-center">
                                <div className="flex h-6 w-6 items-center justify-center">
                                    {step.status === 'completed' && (
                                        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-accent-100">
                                            <CheckCircle2 size={14} className="text-accent-600" />
                                        </div>
                                    )}
                                    {step.status === 'current' && (
                                        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-brand-100">
                                            <Loader2 size={14} className="animate-spin text-brand-600" />
                                        </div>
                                    )}
                                    {step.status === 'pending' && (
                                        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-gray-200">
                                            <Circle size={14} className="text-gray-400" />
                                        </div>
                                    )}
                                    {step.status === 'failed' && (
                                        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-danger-100">
                                            <XCircle size={14} className="text-danger-600" />
                                        </div>
                                    )}
                                </div>
                                {/* Ligne de connexion verticale */}
                                {!isLast && (
                                    <div
                                        className={`mt-1 h-full w-0.5 ${
                                            step.status === 'completed' || step.status === 'current'
                                                ? 'bg-accent-300'
                                                : 'bg-gray-200'
                                        }`}
                                        style={{ minHeight: '24px' }}
                                    />
                                )}
                            </div>

                            {/* Contenu de l'étape */}
                            <div className={`pb-6 ${isLast ? 'pb-0' : ''}`}>
                                <p
                                    className={`text-sm font-medium ${
                                        step.status === 'completed'
                                            ? 'text-gray-900'
                                            : step.status === 'current'
                                              ? 'text-brand-700'
                                              : step.status === 'failed'
                                                ? 'text-danger-700'
                                                : 'text-gray-400'
                                    }`}
                                >
                                    {step.label}
                                </p>
                                {step.timestamp && (
                                    <p className="mt-0.5 text-xs text-gray-500">
                                        {formatDate(step.timestamp)}
                                    </p>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

export default TransferTimeline;
