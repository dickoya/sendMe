import { useState, useRef, useEffect } from 'react';
import { ChevronDown } from 'lucide-react';

/**
 * Drapeaux SVG inline pour les devises.
 * Chaque drapeau est un composant React qui accepte className.
 */

// ── Union Européenne ──────────────────────────────────────────────
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

// ── États-Unis ────────────────────────────────────────────────────
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

// ── Royaume-Uni ───────────────────────────────────────────────────
function FlagGb({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#012169" />
            <path d="M0 0l3 2M3 0L0 2" stroke="#FFF" strokeWidth="0.6" />
            <path d="M0 0l3 2M3 0L0 2" stroke="#C8102E" strokeWidth="0.25" />
            <rect x="1.2" width="0.6" height="2" fill="#FFF" />
            <rect y="0.7" width="3" height="0.6" fill="#FFF" />
            <rect x="1.3" width="0.4" height="2" fill="#C8102E" />
            <rect y="0.8" width="3" height="0.4" fill="#C8102E" />
        </svg>
    );
}

// ── Canada ────────────────────────────────────────────────────────
function FlagCa({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#FFF" />
            <rect width="0.8" height="2" fill="#D52B1E" />
            <rect x="2.2" width="0.8" height="2" fill="#D52B1E" />
            <path d="M1.5 0.3l-0.2 0.4 0.2 0.1-0.1 0.3 0.3-0.1 0.1 0.3 0.1-0.3 0.3 0.1-0.1-0.3 0.2-0.1z" fill="#D52B1E" />
        </svg>
    );
}

// ── Suisse ────────────────────────────────────────────────────────
function FlagCh({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#D52B1E" />
            <rect x="1.1" y="0.6" width="0.8" height="0.8" fill="#FFF" />
            <rect x="0.9" y="0.85" width="1.2" height="0.3" fill="#FFF" />
        </svg>
    );
}

// ── UEMOA (Bénin, Burkina, Côte d'Ivoire...) ──────────────────────
function FlagXof({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#009A44" />
            <rect width="2" height="2" fill="#FFF" />
            <rect width="1" height="2" fill="#FDEF42" />
        </svg>
    );
}

// ── CEMAC (Cameroun, Congo, Gabon...) ─────────────────────────────
function FlagXaf({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#007A5E" />
            <rect width="2" height="2" fill="#FCD116" />
            <rect width="1" height="2" fill="#CE1126" />
        </svg>
    );
}

// ── Guinée ────────────────────────────────────────────────────────
function FlagGn({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#009460" />
            <rect width="2" height="2" fill="#FCD116" />
            <rect width="1" height="2" fill="#CE1126" />
        </svg>
    );
}

// ── Nigeria ───────────────────────────────────────────────────────
function FlagNg({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#FFF" />
            <rect width="1" height="2" fill="#008751" />
            <rect x="2" width="1" height="2" fill="#008751" />
        </svg>
    );
}

// ── Ghana ─────────────────────────────────────────────────────────
function FlagGh({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#CE1126" />
            <rect y="0.666" width="3" height="0.667" fill="#FCD116" />
            <rect y="1.333" width="3" height="0.667" fill="#006B3F" />
            <circle cx="1.5" cy="1" r="0.25" fill="#000" />
        </svg>
    );
}

// ── Afrique du Sud ────────────────────────────────────────────────
function FlagZa({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#DE3831" />
            <rect y="0.5" width="3" height="1" fill="#FFF" />
            <rect y="0.75" width="3" height="0.5" fill="#003DA5" />
            <polygon points="0,0 1.2,1 0,2" fill="#FFB612" />
            <polygon points="0,0 0.8,1 0,2" fill="#000" />
            <polygon points="0,0.3 0.5,1 0,1.7" fill="#007A4D" />
        </svg>
    );
}

// ── Maroc ─────────────────────────────────────────────────────────
function FlagMa({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#C1272D" />
            <polygon points="1.5,0.4 1.7,0.9 2.2,0.9 1.8,1.2 1.9,1.7 1.5,1.4 1.1,1.7 1.2,1.2 0.8,0.9 1.3,0.9" fill="#006233" />
        </svg>
    );
}

// ── Tunisie ───────────────────────────────────────────────────────
function FlagTn({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 3 2" className={`${className} rounded-sm shadow-sm`}>
            <rect width="3" height="2" fill="#D52B1E" />
            <circle cx="1.5" cy="1" r="0.5" fill="#FFF" />
            <circle cx="1.5" cy="1" r="0.35" fill="#D52B1E" />
            <polygon points="1.5,0.7 1.6,0.9 1.8,0.9 1.65,1.0 1.7,1.2 1.5,1.1 1.3,1.2 1.35,1.0 1.2,0.9 1.4,0.9" fill="#FFF" />
        </svg>
    );
}

export type CurrencyFlag =
    | 'eu' | 'us' | 'gb' | 'ca' | 'ch'
    | 'xof' | 'xaf' | 'gn' | 'ng' | 'gh' | 'za' | 'ma' | 'tn';

export interface CurrencyOption {
    code: string;
    label: string;
    flag: CurrencyFlag;
}

const FLAG_MAP: Record<string, React.FC<{ className?: string }>> = {
    eu: FlagEu,
    us: FlagUs,
    gb: FlagGb,
    ca: FlagCa,
    ch: FlagCh,
    xof: FlagXof,
    xaf: FlagXaf,
    gn: FlagGn,
    ng: FlagNg,
    gh: FlagGh,
    za: FlagZa,
    ma: FlagMa,
    tn: FlagTn,
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
