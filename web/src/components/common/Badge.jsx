import './Badge.css';

/**
 * Étiquette colorée pour afficher un statut.
 *
 * @param {string} variant - couleur : success, danger, warning, info, purple, gray
 * @param {string} size - taille : sm, md
 */
function Badge({ children, variant = 'gray', size = 'md' }) {
    return (
        <span className={`badge badge--${variant} badge--${size}`}>
      {children}
    </span>
    );
}

export default Badge;