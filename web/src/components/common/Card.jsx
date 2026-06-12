import './Card.css';

/**
 * Conteneur de carte réutilisable avec en-tête optionnel.
 *
 * @param {string} title - titre affiché dans l'en-tête de la carte
 * @param {React.ReactNode} actions - éléments affichés à droite du titre (boutons...)
 * @param {boolean} noPadding - supprime le padding interne (utile pour les tableaux)
 */
function Card({ title, actions, noPadding = false, children, className = '', ...rest }) {
    return (
        <div className={`card ${className}`} {...rest}>
            {title && (
                <div className="card__header">
                    <h3 className="card__title">{title}</h3>
                    {actions && <div className="card__actions">{actions}</div>}
                </div>
            )}
            <div className={`card__body ${noPadding ? 'card__body--no-padding' : ''}`}>
                {children}
            </div>
        </div>
    );
}

export default Card;