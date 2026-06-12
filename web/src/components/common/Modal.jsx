import { X } from 'lucide-react';
import './Modal.css';

/**
 * Fenêtre modale réutilisable.
 *
 * @param {boolean} isOpen - affiche ou masque la modale
 * @param {function} onClose - appelé à la fermeture (clic overlay ou bouton X)
 * @param {string} title - titre affiché en en-tête
 * @param {string} size - taille de la modale : sm, md, lg
 * @param {React.ReactNode} footer - contenu du pied de modale (boutons d'action)
 */
function Modal({ isOpen, onClose, title, size = 'md', footer, children }) {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div
                className={`modal modal--${size}`}
                onClick={(e) => e.stopPropagation()} // empêche la fermeture au clic interne
            >
                <div className="modal__header">
                    <h3 className="modal__title">{title}</h3>
                    <button className="modal__close" onClick={onClose} aria-label="Fermer">
                        <X size={18} />
                    </button>
                </div>

                <div className="modal__body">{children}</div>

                {footer && <div className="modal__footer">{footer}</div>}
            </div>
        </div>
    );
}

export default Modal;