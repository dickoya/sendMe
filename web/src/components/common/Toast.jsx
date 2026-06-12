import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-react';
import './Toast.css';

/**
 * Notification éphémère affichée en haut à droite de l'écran.
 *
 * Types : success, error, warning, info
 */
const ICONS = {
    success: <CheckCircle size={18} />,
    error: <XCircle size={18} />,
    warning: <AlertTriangle size={18} />,
    info: <Info size={18} />,
};

function Toast({ message, type = 'info', onClose }) {
    return (
        <div className={`toast toast--${type}`}>
            <span className="toast__icon">{ICONS[type]}</span>
            <span className="toast__message">{message}</span>
            <button className="toast__close" onClick={onClose} aria-label="Fermer">
                <X size={14} />
            </button>
        </div>
    );
}

export default Toast;