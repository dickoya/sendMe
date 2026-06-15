import { useState, useRef, useEffect } from 'react';
import { ChevronDown } from 'lucide-react';

/**
 * Drapeaux SVG inline pour les devises.
 */
function FlagEu({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#039" />
            <g fill="#FC0">
                <circle cx="1.5" cy="1" r="0.35" />
                {Array.from({ length: 12 }).map((_, i) => {
                    const angle = (i * 30 - 15) * (Math.PI / 180);
                    const x = 1.5 + 0.5 * Math.cos(angle);
                    const y = 1 + 0.5 * Math.sin(angle);
                    return <circle key={i} cx={x} cy={y} r="0.08" />;
                })}
            </g>
        </svg>
    );
}

function FlagUs({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#BD3D44" />
            {Array.from({ length: 13 }).map((_, i) =>
                i % 2 === 0 && <rect key={i} y={i * (2 / 13)} width="3" height={2 / 13} fill="#FFF" />
            )}
            <rect width="1.4" height="1.077" fill="#192F5D" />
            {Array.from({ length: 50 }).map((_, i) => {
                const row = Math.floor(i / 10);
                const col = i % 10;
                const x = 0.07 + col * 0.14;
                const y = 0.054 + row * 0.108;
                return <circle key={i} cx={x} cy={y} r="0.03" fill="#FFF" />;
            })}
        </svg>
    );
}

function FlagXof({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#009A44" />
            <rect width="2" height="2" fill="#FFF" />
            <rect width="1" height="2" fill="#FDEF42" />
        </svg>
    );
}

function FlagGn({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#009460" />
            <rect width="2" height="2" fill="#FCD116" />
            <rect width="1" height="2" fill="#CE1126" />
        </svg>
    );
}

export interface CurrencyOption {
    code: string;
    label: string;
    flag: 'eu' | 'us' | 'xof' | 'gn';
}

const FLAG_MAP: Record<string, React.FC<{ className?: string }>> = {
    eu: FlagEu,
    us: FlagUs,
    xof: FlagXof,
    gn: FlagGn,
};

interface CurrencySelectProps {
    options: CurrencyOption[];
    value: string;
    onChange: (code: string) => void;
    label: string;
}

/**
 * Menu déroulant pour sélectionner une devise avec drapeau.
 * Extensible : il suffit d'ajouter des options dans le tableau.
 */
export default function CurrencySelect({ options, value, onChange, label }: CurrencySelectProps) {
    const [open, setOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

    const selected = options.find((o) => o.code === value) ?? options[0];
    const FlagIcon = FLAG_MAP[selected.flag];

    useEffect(() => {
        function handleClickOutside(e: MouseEvent) {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                setOpen(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <div ref={ref} className="relative">
            <label className="mb-1.5 block text-sm font-medium text-white/80">{label}</label>
            <button
                type="button"
                onClick={() => setOpen(!open)}
                className="flex w-full items-center gap-2 rounded-lg bg-white/10 px-3 py-2 text-sm font-medium text-white ring-1 ring-white/20 transition-all hover:bg-white/20 focus:outline-none focus:ring-2 focus:ring-white/50"
            >
                {FlagIcon && <FlagIcon />}
                <span className="flex-1 text-left">{selected.code}</span>
                <ChevronDown size={14} className={`text-white/60 transition-transform ${open ? 'rotate-180' : ''}`} />
            </button>

            {open && (
                <div className="absolute left-0 right-0 z-50 mt-1 overflow-hidden rounded-lg border border-white/10 bg-brand-800 shadow-xl backdrop-blur-xl">
                    {options.map((option) => {
                        const ItemFlag = FLAG_MAP[option.flag];
                        const isSelected = option.code === value;
                        return (
                            <button
                                key={option.code}
                                type="button"
                                onClick={() => {
                                    onChange(option.code);
                                    setOpen(false);
                                }}
                                className={`flex w-full items-center gap-2 px-3 py-2 text-left text-sm transition-all ${
                                    isSelected
                                        ? 'bg-white/15 text-white'
                                        : 'text-white/70 hover:bg-white/10 hover:text-white'
                                }`}
                            >
                                {ItemFlag && <ItemFlag />}
                                <span className="font-medium">{option.code}</span>
                                <span className="text-white/50">{option.label}</span>
                            </button>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
