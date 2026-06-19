import { useState, useEffect, useCallback } from 'react';

const ONBOARDING_STORAGE_KEY = 'sendme_onboarding_completed';

export interface OnboardingStep {
    id: string;
    targetSelector: string;
    position?: 'top' | 'bottom' | 'left' | 'right';
}

const STEPS: OnboardingStep[] = [
    {
        id: 'welcome',
        targetSelector: '#onboarding-welcome',
        position: 'bottom',
    },
    {
        id: 'stats',
        targetSelector: '#onboarding-stats',
        position: 'top',
    },
    {
        id: 'exchange-rates',
        targetSelector: '#onboarding-exchange-rates',
        position: 'top',
    },
    {
        id: 'cta',
        targetSelector: '#onboarding-cta',
        position: 'bottom',
    },
];

interface UseOnboardingReturn {
    isActive: boolean;
    currentStep: number;
    totalSteps: number;
    step: OnboardingStep;
    next: () => void;
    previous: () => void;
    skip: () => void;
    finish: () => void;
    targetRect: DOMRect | null;
}

export function useOnboarding(isNewUser: boolean): UseOnboardingReturn {
    const [isActive, setIsActive] = useState(false);
    const [currentStep, setCurrentStep] = useState(0);
    const [targetRect, setTargetRect] = useState<DOMRect | null>(null);

    useEffect(() => {
        const completed = localStorage.getItem(ONBOARDING_STORAGE_KEY);
        if (isNewUser && !completed) {
            // Petit délai pour laisser le DOM se stabiliser
            const timer = setTimeout(() => setIsActive(true), 500);
            return () => clearTimeout(timer);
        }
    }, [isNewUser]);

    // Recalculer la position de la cible à chaque étape et au scroll/resize
    const updateTargetRect = useCallback(() => {
        const step = STEPS[currentStep];
        if (!step) return;

        const el = document.querySelector(step.targetSelector);
        if (el) {
            setTargetRect(el.getBoundingClientRect());
        }
    }, [currentStep]);

    useEffect(() => {
        if (!isActive) return;

        updateTargetRect();

        const handleScroll = () => updateTargetRect();
        const handleResize = () => updateTargetRect();

        window.addEventListener('scroll', handleScroll, true);
        window.addEventListener('resize', handleResize);

        return () => {
            window.removeEventListener('scroll', handleScroll, true);
            window.removeEventListener('resize', handleResize);
        };
    }, [isActive, updateTargetRect]);

    const next = useCallback(() => {
        if (currentStep < STEPS.length - 1) {
            setCurrentStep((prev) => prev + 1);
        } else {
            finish();
        }
    }, [currentStep]);

    const previous = useCallback(() => {
        if (currentStep > 0) {
            setCurrentStep((prev) => prev - 1);
        }
    }, [currentStep]);

    const skip = useCallback(() => {
        localStorage.setItem(ONBOARDING_STORAGE_KEY, 'true');
        setIsActive(false);
        setCurrentStep(0);
    }, []);

    const finish = useCallback(() => {
        localStorage.setItem(ONBOARDING_STORAGE_KEY, 'true');
        setIsActive(false);
        setCurrentStep(0);
    }, []);

    return {
        isActive,
        currentStep,
        totalSteps: STEPS.length,
        step: STEPS[currentStep],
        next,
        previous,
        skip,
        finish,
        targetRect,
    };
}
