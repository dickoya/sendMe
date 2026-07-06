import { cn } from '@/lib/utils';

interface PaymentChannelLogoProps {
    channelType: string;
    size?: number;
    className?: string;
}

/**
 * Mapping entre le channelType et le nom du fichier logo.
 * Les fichiers sont dans /logo/ (servis depuis web/public/logo/).
 */
const LOGO_MAP: Record<string, string> = {
    ORANGE_MONEY: '/logo/ORANGE_MONEY-logo.jpg',
    MTN_MOBILE_MONEY: '/logo/MTN-Logo.png',
    WAVE: '/logo/WAVE-logo.png',
    MOOV_MONEY: '/logo/MOOVE_MONEY-logo.png',
    AIRTEL_MONEY: '/logo/AIRTEL-logo.png',
    BANK_TRANSFER: '/logo/VIREMENT_BANCAIRE-logo.png',
};

/**
 * Affiche le logo réel du moyen de paiement correspondant au channelType.
 * Utilise les fichiers images PNG/JPG situés dans /logo/.
 */
function PaymentChannelLogo({ channelType, size = 40, className }: PaymentChannelLogoProps) {
    const type = channelType.toUpperCase();

    // Chercher le logo correspondant
    let logoSrc: string | undefined;
    for (const [key, src] of Object.entries(LOGO_MAP)) {
        if (type.includes(key)) {
            logoSrc = src;
            break;
        }
    }

    if (logoSrc) {
        return (
            <div
                className={cn('flex items-center justify-center rounded-lg overflow-hidden', className)}
                style={{ width: size, height: size }}
            >
                <img
                    src={logoSrc}
                    alt={channelType}
                    style={{ width: size, height: size }}
                    className="object-contain"
                />
            </div>
        );
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

export default PaymentChannelLogo;
