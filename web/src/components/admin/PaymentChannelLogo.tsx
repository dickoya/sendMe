import { cn } from '@/lib/utils';

interface PaymentChannelLogoProps {
    channelType: string;
    size?: number;
    className?: string;
}

/**
 * Affiche le logo réel du moyen de paiement correspondant au channelType.
 * Utilise des SVG inline pour chaque opérateur.
 */
function PaymentChannelLogo({ channelType, size = 40, className }: PaymentChannelLogoProps) {
    const type = channelType.toUpperCase();

    if (type.includes('ORANGE_MONEY') || type.includes('ORANGE')) {
        return <OrangeMoneyLogo size={size} className={className} />;
    }
    if (type.includes('MTN_MOBILE_MONEY') || type.includes('MTN')) {
        return <MtnLogo size={size} className={className} />;
    }
    if (type.includes('WAVE')) {
        return <WaveLogo size={size} className={className} />;
    }
    if (type.includes('MOOV_MONEY') || type.includes('MOOV')) {
        return <MoovLogo size={size} className={className} />;
    }
    if (type.includes('AIRTEL_MONEY') || type.includes('AIRTEL')) {
        return <AirtelLogo size={size} className={className} />;
    }
    if (type.includes('BANK_TRANSFER') || type.includes('BANK') || type.includes('VIREMENT')) {
        return <BankLogo size={size} className={className} />;
    }

    // Fallback : icône générique
    return (
        <div
            className={cn(
                'flex items-center justify-center rounded-lg bg-gray-100 dark:bg-[#1A1A1A]',
                className
            )}
            style={{ width: size, height: size }}
        >
            <span className="text-lg font-bold text-gray-400">?</span>
        </div>
    );
}

/* ─── Orange Money ─────────────────────────────────────────── */
function OrangeMoneyLogo({ size, className }: { size: number; className?: string }) {
    return (
        <div
            className={cn('flex items-center justify-center rounded-lg', className)}
            style={{ width: size, height: size }}
        >
            <svg viewBox="0 0 48 48" width={size} height={size}>
                <rect width="48" height="48" rx="8" fill="#FF7900" />
                <text
                    x="24"
                    y="20"
                    textAnchor="middle"
                    fill="white"
                    fontSize="10"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    ORANGE
                </text>
                <text
                    x="24"
                    y="32"
                    textAnchor="middle"
                    fill="white"
                    fontSize="8"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    MONEY
                </text>
            </svg>
        </div>
    );
}

/* ─── MTN Mobile Money ─────────────────────────────────────── */
function MtnLogo({ size, className }: { size: number; className?: string }) {
    return (
        <div
            className={cn('flex items-center justify-center rounded-lg', className)}
            style={{ width: size, height: size }}
        >
            <svg viewBox="0 0 48 48" width={size} height={size}>
                <rect width="48" height="48" rx="8" fill="#FFCC00" />
                <text
                    x="24"
                    y="18"
                    textAnchor="middle"
                    fill="#000"
                    fontSize="9"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    MTN
                </text>
                <text
                    x="24"
                    y="28"
                    textAnchor="middle"
                    fill="#000"
                    fontSize="7"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    Mobile
                </text>
                <text
                    x="24"
                    y="37"
                    textAnchor="middle"
                    fill="#000"
                    fontSize="7"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    Money
                </text>
            </svg>
        </div>
    );
}

/* ─── Wave ─────────────────────────────────────────────────── */
function WaveLogo({ size, className }: { size: number; className?: string }) {
    return (
        <div
            className={cn('flex items-center justify-center rounded-lg', className)}
            style={{ width: size, height: size }}
        >
            <svg viewBox="0 0 48 48" width={size} height={size}>
                <rect width="48" height="48" rx="8" fill="#0088FF" />
                <text
                    x="24"
                    y="26"
                    textAnchor="middle"
                    dominantBaseline="central"
                    fill="white"
                    fontSize="16"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    W
                </text>
                <text
                    x="24"
                    y="38"
                    textAnchor="middle"
                    fill="white"
                    fontSize="7"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    wave
                </text>
            </svg>
        </div>
    );
}

/* ─── Moov Money ───────────────────────────────────────────── */
function MoovLogo({ size, className }: { size: number; className?: string }) {
    return (
        <div
            className={cn('flex items-center justify-center rounded-lg', className)}
            style={{ width: size, height: size }}
        >
            <svg viewBox="0 0 48 48" width={size} height={size}>
                <rect width="48" height="48" rx="8" fill="#00A3E0" />
                <text
                    x="24"
                    y="18"
                    textAnchor="middle"
                    fill="white"
                    fontSize="10"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    MOOV
                </text>
                <text
                    x="24"
                    y="30"
                    textAnchor="middle"
                    fill="white"
                    fontSize="7"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    Money
                </text>
            </svg>
        </div>
    );
}

/* ─── Airtel Money ─────────────────────────────────────────── */
function AirtelLogo({ size, className }: { size: number; className?: string }) {
    return (
        <div
            className={cn('flex items-center justify-center rounded-lg', className)}
            style={{ width: size, height: size }}
        >
            <svg viewBox="0 0 48 48" width={size} height={size}>
                <rect width="48" height="48" rx="8" fill="#E0004D" />
                <text
                    x="24"
                    y="18"
                    textAnchor="middle"
                    fill="white"
                    fontSize="9"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    AIRTEL
                </text>
                <text
                    x="24"
                    y="30"
                    textAnchor="middle"
                    fill="white"
                    fontSize="7"
                    fontWeight="bold"
                    fontFamily="Arial"
                >
                    Money
                </text>
            </svg>
        </div>
    );
}

/* ─── Virement bancaire ────────────────────────────────────── */
function BankLogo({ size, className }: { size: number; className?: string }) {
    return (
        <div
            className={cn('flex items-center justify-center rounded-lg', className)}
            style={{ width: size, height: size }}
        >
            <svg viewBox="0 0 48 48" width={size} height={size}>
                <rect width="48" height="48" rx="8" fill="#1A5C8A" />
                {/* Bâtiment de banque stylisé */}
                <rect x="14" y="22" width="20" height="16" rx="1" fill="white" opacity="0.9" />
                <rect x="18" y="26" width="4" height="6" rx="1" fill="#1A5C8A" />
                <rect x="26" y="26" width="4" height="6" rx="1" fill="#1A5C8A" />
                <polygon points="24,12 12,22 36,22" fill="white" opacity="0.9" />
                <rect x="10" y="38" width="28" height="2" rx="1" fill="white" opacity="0.7" />
            </svg>
        </div>
    );
}

export default PaymentChannelLogo;
