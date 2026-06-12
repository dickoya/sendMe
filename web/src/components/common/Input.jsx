import { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import './Input.css';

/**
 * Champ de saisie réutilisable avec label, erreur et icônes.
 *
 * @param {string} label - libellé affiché au-dessus du champ
 * @param {string} error - message d'erreur (affiché en rouge sous le champ)
 * @param {string} type - type HTML (text, email, password, number...)
 * @param {React.ReactNode} icon - icône affichée à gauche du champ
 * @param {string} hint - texte d'aide affiché sous le champ
 */
function Input({
                   label,
                   error,
                   type = 'text',
                   icon = null,
                   hint,
                   required = false,
                   ...rest
               }) {
    const [showPassword, setShowPassword] = useState(false);

    // Permet de basculer l'affichage du mot de passe
    const isPassword = type === 'password';
    const inputType = isPassword && showPassword ? 'text' : type;

    return (
        <div className="input-group">
            {label && (
                <label className="input-label">
                    {label}
                    {required && <span className="input-required">*</span>}
                </label>
            )}

            <div className={`input-wrapper ${error ? 'input-wrapper--error' : ''}`}>
                {icon && <span className="input-icon">{icon}</span>}

                <input
                    type={inputType}
                    className="input-field"
                    {...rest}
                />

                {isPassword && (
                    <button
                        type="button"
                        className="input-toggle"
                        onClick={() => setShowPassword(!showPassword)}
                        aria-label={showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                    >
                        {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                    </button>
                )}
            </div>

            {error && <span className="input-error">{error}</span>}
            {hint && !error && <span className="input-hint">{hint}</span>}
        </div>
    );
}

export default Input;