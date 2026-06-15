/**
 * Drapeaux SVG inline pour le sélecteur de langue.
 * Garantit un affichage fiable sur tous les OS (Windows, Mac, Linux).
 */

export function FlagFr({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 3 2"
            className={`${className} rounded-sm shadow-sm`}
        >
            <rect width="3" height="2" fill="#ED2939" />
            <rect width="2" height="2" fill="#fff" />
            <rect width="1" height="2" fill="#002395" />
        </svg>
    );
}

export function FlagGb({ className = 'h-4 w-5' }: { className?: string }) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 60 30"
            className={`${className} rounded-sm shadow-sm`}
        >
            <clipPath id="uk_clip_svg">
                <rect width="60" height="30" />
            </clipPath>
            <g clipPath="url(#uk_clip_svg)">
                <rect width="60" height="30" fill="#012169" />
                <path d="M0 0l60 30m0-30L0 30" stroke="#fff" strokeWidth="6" />
                <path d="M0 0l60 30m0-30L0 30" stroke="#C8102E" strokeWidth="3" />
                <path d="M30 0v30M0 15h60" stroke="#fff" strokeWidth="10" />
                <path d="M30 0v30M0 15h60" stroke="#C8102E" strokeWidth="5" />
            </g>
        </svg>
    );
}
