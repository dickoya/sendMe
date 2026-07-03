import { cn } from '@/lib/utils';

export interface ToggleProps {
    checked: boolean;
    onChange: (checked: boolean) => void;
    label?: string;
    disabled?: boolean;
    size?: 'sm' | 'md';
    className?: string;
}

const sizeMap = {
    sm: { track: 'h-5 w-9', thumb: 'h-3.5 w-3.5', translateOn: 'translate-x-4', translateOff: 'translate-x-0.5' },
    md: { track: 'h-6 w-11', thumb: 'h-4 w-4', translateOn: 'translate-x-6', translateOff: 'translate-x-1' },
};

/**
 * Interrupteur (toggle/switch) pour les options binaires.
 *
 * @example
 * <Toggle checked={enabled} onChange={setEnabled} label="Activer les notifications" />
 */
function Toggle({ checked, onChange, label, disabled, size = 'md', className }: ToggleProps) {
    const s = sizeMap[size];

    return (
        <label className={cn('inline-flex items-center gap-2', disabled && 'opacity-50 cursor-not-allowed', className)}>
            <button
                type="button"
                role="switch"
                aria-checked={checked}
                disabled={disabled}
                onClick={() => !disabled && onChange(!checked)}
                className={cn(
                    'relative inline-flex flex-shrink-0 cursor-pointer rounded-full transition-colors duration-200 ease-in-out focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2',
                    checked ? 'bg-brand-600 dark:bg-brand-500' : 'bg-gray-300 dark:bg-[#444444]',
                    disabled && 'cursor-not-allowed',
                    s.track
                )}
            >
                <span
                    className={cn(
                        'inline-block transform rounded-full bg-white shadow-sm transition-transform duration-200 ease-in-out',
                        s.thumb,
                        checked ? s.translateOn : s.translateOff
                    )}
                />
            </button>
            {label && (
                <span className="text-sm text-gray-700 dark:text-gray-300 select-none">{label}</span>
            )}
        </label>
    );
}

export default Toggle;
