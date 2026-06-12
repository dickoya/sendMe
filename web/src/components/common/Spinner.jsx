import './Spinner.css';

/**
 * Indicateur de chargement réutilisable.
 *
 * @param {string} size - taille du spinner : sm, md, lg
 * @param {boolean} fullPage - centre le spinner sur toute la page
 * @param {string} label - texte affiché sous le spinner
 */
function Spinner({ size = 'md', fullPage = false, label }) {
    const spinner = (
        <div className="spinner-container">
            <div className={`spinner spinner--${size}`} />
            {label && <span className="spinner__label">{label}</span>}
        </div>
    );

    if (fullPage) {
        return <div className="spinner-fullpage">{spinner}</div>;
    }

    return spinner;
}

export default Spinner;