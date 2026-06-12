/**
 * Composant racine de l'application SendMe.
 *
 * Pour l'instant, simple page de test pour valider
 * que le setup fonctionne. Sera remplacé par AppRoutes
 * à l'étape 3 (Layouts + Routes).
 */
function App() {
  return (
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        gap: '1rem'
      }}>
        <h1 style={{ color: 'var(--color-primary)' }}>
          SendMe 🚀
        </h1>
        <p style={{ color: 'var(--color-text-secondary)' }}>
          Setup du projet réussi — Étape 1 terminée
        </p>
      </div>
  );
}

export default App;