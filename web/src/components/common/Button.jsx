import './Button.css';

/**
 * Bouton réutilisable avec variantes de style.
 *
 * Variantes : primary, secondary, outline, danger, success, ghost
 * Tailles   : sm, md, lg
 *
 * @param {string} variant - style du bouton
 * @param {string} size - taille du bouton
 * @param {boolean} fullWidth - prend toute la largeur du parent
 * @param {boolean} loading - affiche un spinner et désactive le bouton
 * @param {React.ReactNode} icon - icône optionnelle (lucide-react)
 */
function Button({
                    children,
                    variant = 'primary',
                    size = 'md',
                    fullWidth = false,
                    loading = false,
                    icon = null,
                    disabled = false,
                    type = 'button',
                    onClick,
                    ...rest
                }) {
    return (
        <button
            type={type}
            className={`btn btn--${variant} btn--${size} ${fullWidth ? 'btn--full' : ''}`}
            disabled={disabled || loading}
            onClick={onClick}
            {...rest}
        >
            {loading ? (
                <span className="btn__spinner" />
            ) : (
                <>
                    {icon && <span className="btn__icon">{icon}</span>}
                    {children}
                </>
            )}
        </button>
    );
}

export default Button;