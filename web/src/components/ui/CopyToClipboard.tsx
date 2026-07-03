import { useState, type ReactNode } from 'react';
import { Copy, Check } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface CopyToClipboardProps {
    text: string;
    label?: string;
    onCopy?: () => void;
    size?: 'sm' | 'md';
    className?: string;
    children?: ReactNode;
}

const sizeMap = {
    sm: { button: 'h-7 w-7', icon: 14 },
    md: { button: 'h-8 w-8', icon: 16 },
};

/**
 * Bouton copier avec feedback visuel.
 *
 * @example
 * <CopyToClipboard text={referralCode} label="Copier le code" />
 * <CopyToClipboard text="Texte à copier">
 *   <span className="font-mono">{code}</span>
 * </CopyToClipboard>
 */
function CopyToClipboard({ text, label, onCopy, size = 'md', className, children }: CopyToClipboardProps) {
    const [copied, setCopied] = useState(false);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(text);
            setCopied(true);
            onCopy?.();
            setTimeout(() => setCopied(false), 2000);
        } catch {
            // Fallback silencieux
        }
    };

    const s = sizeMap[size];

    return (
        <div className={cn('flex items-center gap-2', className)}>
            {children}
            <button
                onClick={handleCopy}
                className={cn(
                    'flex items-center justify-center rounded-lg transition-all',
                    copied
                        ? 'bg-accent-50 text-accent-600 dark:bg-accent-900/30 dark:text-accent-400'
                        : 'text-gray-400 hover:bg-gray-100 hover:text-gray-600 dark:text-[#5A5E72] dark:hover:bg-[#222222] dark:hover:text-[#8B8FA3]'
                )}
                title={label ?? 'Copier'}
                aria-label={label ?? 'Copier'}
            >
                {copied ? <Check size={s.icon} /> : <Copy size={s.icon} />}
            </button>
        </div>
    );
}

export default CopyToClipboard;
