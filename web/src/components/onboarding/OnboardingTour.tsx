import { useTranslation } from 'react-i18next';
import { createPortal } from 'react-dom';
import { X, ArrowLeft, ArrowRight, Check } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { OnboardingStep } from '@/hooks/useOnboarding';

interface OnboardingTourProps {
    isActive: boolean;
    currentStep: number;
    totalSteps: number;
    step: OnboardingStep;
    targetRect: DOMRect | null;
    onNext: () => void;
    onPrevious: () => void;
    onSkip: () => void;
    onFinish: () => void;
}

/**
 * Palette moderne inspirée de Canva — couleurs vives et attractives.
 * - Violet/indigo comme couleur primaire (#6C5CE7 → équivalent brand)
 * - Rose/rouge pour les accents (#FF6B6B)
 * - Dégradés dynamiques
 */
const CANVA_GRADIENT = 'from-[#6C5CE7] via-[#A29BFE] to-[#FD79A8]';
const CANVA_GRADIENT_DARK = 'from-[#6C5CE7] via-[#A29BFE] to-[#FD79A8]';

function OnboardingTour({
                            isActive,
                            currentStep,
                            totalSteps,
                            step,
                            targetRect,
                            onNext,
                            onPrevious,
                            onSkip,
                            onFinish,
                        }: OnboardingTourProps) {
    const { t } = useTranslation('onboarding');

    if (!isActive || !targetRect) return null;

    const isLastStep = currentStep === totalSteps - 1;
    const stepKey = `step${currentStep + 1}` as const;

    // Calcul de la position du tooltip
    const tooltipStyle = getTooltipPosition(step, targetRect);

    return createPortal(
        <>
            {/* Overlay avec effet de spotlight sur l'élément ciblé */}
            <div className="fixed inset-0 z-[9999]">
                {/* Overlay sombre avec trou (spotlight) */}
                <svg className="absolute inset-0 h-full w-full" width="100%" height="100%">
                    <defs>
                        <mask id="onboarding-spotlight">
                            <rect width="100%" height="100%" fill="white" />
                            <rect
                                x={targetRect.left - 8}
                                y={targetRect.top - 8}
                                width={targetRect.width + 16}
                                height={targetRect.height + 16}
                                rx={16}
                                fill="black"
                            />
                        </mask>
                    </defs>
                    <rect
                        width="100%"
                        height="100%"
                        fill="rgba(0,0,0,0.5)"
                        mask="url(#onboarding-spotlight)"
                        className="transition-all duration-300"
                    />
                </svg>

                {/* Bordure lumineuse autour de l'élément ciblé */}
                <div
                    className="pointer-events-none absolute rounded-2xl border-2 border-[#6C5CE7] shadow-[0_0_20px_rgba(108,92,231,0.4)] transition-all duration-300"
                    style={{
                        left: targetRect.left - 10,
                        top: targetRect.top - 10,
                        width: targetRect.width + 20,
                        height: targetRect.height + 20,
                    }}
                />

                {/* Tooltip */}
                <div
                    className={cn(
                        'absolute z-[10000] w-80 rounded-2xl bg-white p-5 shadow-2xl transition-all duration-300',
                        'dark:bg-[#1A1A1A] dark:border dark:border-[#333]'
                    )}
                    style={tooltipStyle}
                >
                    {/* Barre de progression avec dégradé Canva */}
                    <div className="mb-4 flex items-center gap-2">
                        <div className="flex-1">
                            <div className="h-1.5 w-full overflow-hidden rounded-full bg-gray-100 dark:bg-[#333]">
                                <div
                                    className={cn(
                                        'h-full rounded-full bg-gradient-to-r transition-all duration-500',
                                        CANVA_GRADIENT
                                    )}
                                    style={{ width: `${((currentStep + 1) / totalSteps) * 100}%` }}
                                />
                            </div>
                        </div>
                        <span className="text-xs font-medium text-gray-400 dark:text-[#8B8FA3]">
                            {t('stepIndicator', { current: currentStep + 1, total: totalSteps })}
                        </span>
                    </div>

                    {/* Contenu */}
                    <div className="mb-5">
                        <h3 className="mb-2 text-lg font-bold text-gray-900 dark:text-[#EDEEF0]">
                            {t(`${stepKey}.title`)}
                        </h3>
                        <p className="text-sm leading-relaxed text-gray-600 dark:text-[#8B8FA3]">
                            {t(`${stepKey}.description`)}
                        </p>
                    </div>

                    {/* Actions */}
                    <div className="flex items-center justify-between">
                        <button
                            onClick={onSkip}
                            className="text-sm font-medium text-gray-400 transition-colors hover:text-gray-600 dark:text-[#5A5E72] dark:hover:text-[#8B8FA3]"
                        >
                            {t('skip')}
                        </button>

                        <div className="flex items-center gap-2">
                            {currentStep > 0 && (
                                <button
                                    onClick={onPrevious}
                                    className="flex h-9 w-9 items-center justify-center rounded-xl border border-gray-200 text-gray-600 transition-all hover:bg-gray-50 hover:border-gray-300 dark:border-[#333] dark:text-[#8B8FA3] dark:hover:bg-[#222]"
                                >
                                    <ArrowLeft size={16} />
                                </button>
                            )}

                            {isLastStep ? (
                                <button
                                    onClick={onFinish}
                                    className={cn(
                                        'flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold text-white transition-all',
                                        'bg-gradient-to-r shadow-lg hover:shadow-xl hover:scale-105 active:scale-95',
                                        CANVA_GRADIENT
                                    )}
                                >
                                    <Check size={16} />
                                    {t('finish')}
                                </button>
                            ) : (
                                <button
                                    onClick={onNext}
                                    className={cn(
                                        'flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold text-white transition-all',
                                        'bg-gradient-to-r shadow-lg hover:shadow-xl hover:scale-105 active:scale-95',
                                        CANVA_GRADIENT
                                    )}
                                >
                                    {t('next')}
                                    <ArrowRight size={16} />
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </>,
        document.body
    );
}

/**
 * Calcule la position du tooltip en fonction de la position de l'élément cible
 * et de l'espace disponible dans la fenêtre.
 */
function getTooltipPosition(
    step: OnboardingStep,
    targetRect: DOMRect
): React.CSSProperties {
    const tooltipWidth = 320; // w-80
    const tooltipHeight = 240; // hauteur approximative
    const gap = 16;
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    const position = step.position || 'bottom';

    switch (position) {
        case 'bottom': {
            let left = targetRect.left + targetRect.width / 2 - tooltipWidth / 2;
            let top = targetRect.bottom + gap;

            // Ajustements pour rester dans le viewport
            if (left < 16) left = 16;
            if (left + tooltipWidth > viewportWidth - 16) {
                left = viewportWidth - tooltipWidth - 16;
            }
            if (top + tooltipHeight > viewportHeight) {
                // Pas assez de place en bas → placer au-dessus
                top = targetRect.top - tooltipHeight - gap;
            }

            return { left, top };
        }
        case 'top': {
            let left = targetRect.left + targetRect.width / 2 - tooltipWidth / 2;
            let top = targetRect.top - tooltipHeight - gap;

            if (left < 16) left = 16;
            if (left + tooltipWidth > viewportWidth - 16) {
                left = viewportWidth - tooltipWidth - 16;
            }
            if (top < 16) {
                // Pas assez de place en haut → placer en dessous
                top = targetRect.bottom + gap;
            }

            return { left, top };
        }
        case 'left': {
            let left = targetRect.left - tooltipWidth - gap;
            let top = targetRect.top + targetRect.height / 2 - tooltipHeight / 2;

            if (left < 16) left = targetRect.right + gap;
            if (top < 16) top = 16;
            if (top + tooltipHeight > viewportHeight - 16) {
                top = viewportHeight - tooltipHeight - 16;
            }

            return { left, top };
        }
        case 'right': {
            let left = targetRect.right + gap;
            let top = targetRect.top + targetRect.height / 2 - tooltipHeight / 2;

            if (left + tooltipWidth > viewportWidth - 16) {
                left = targetRect.left - tooltipWidth - gap;
            }
            if (top < 16) top = 16;
            if (top + tooltipHeight > viewportHeight - 16) {
                top = viewportHeight - tooltipHeight - 16;
            }

            return { left, top };
        }
        default:
            return { left: 16, top: 16 };
    }
}

export default OnboardingTour;
