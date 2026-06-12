import { ToastProvider, useToast } from './store/notification/toastContext';
import Button from './components/common/Button';
import Input from './components/common/Input';
import Card from './components/common/Card';
import Badge from './components/common/Badge';
import Spinner from './components/common/Spinner';
import Modal from './components/common/Modal';
import { useState } from 'react';
import { Mail, Send } from 'lucide-react';

/**
 * Page de test temporaire pour valider tous les composants common/
 * de l'Étape 2. Sera remplacée par AppRoutes à l'Étape 3.
 */
function TestComponents() {
    const { showToast } = useToast();
    const [modalOpen, setModalOpen] = useState(false);

    return (
        <div style={{ padding: '2rem', display: 'flex', flexDirection: 'column', gap: '1.5rem', maxWidth: '500px', margin: '0 auto' }}>

            <h1 style={{ color: 'var(--color-primary)' }}>SendMe — Test Composants</h1>

            <Card title="Boutons" actions={<Badge variant="info">Étape 2</Badge>}>
                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                    <Button variant="primary" icon={<Send size={16} />}>Primary</Button>
                    <Button variant="secondary">Secondary</Button>
                    <Button variant="outline">Outline</Button>
                    <Button variant="danger">Danger</Button>
                    <Button variant="success">Success</Button>
                    <Button loading>Chargement</Button>
                </div>
            </Card>

            <Card title="Champs de saisie">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    <Input label="Email" type="email" placeholder="jean@example.com" icon={<Mail size={16} />} required />
                    <Input label="Mot de passe" type="password" placeholder="••••••••" required />
                    <Input label="Avec erreur" error="Ce champ est obligatoire" />
                </div>
            </Card>

            <Card title="Badges de statut">
                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                    <Badge variant="success">Complétée</Badge>
                    <Badge variant="warning">En attente</Badge>
                    <Badge variant="danger">Échouée</Badge>
                    <Badge variant="purple">Remboursée</Badge>
                    <Badge variant="info">En cours</Badge>
                </div>
            </Card>

            <Card title="Spinner">
                <Spinner label="Chargement des données..." />
            </Card>

            <Card title="Toast & Modal">
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <Button onClick={() => showToast('Transfert effectué avec succès !', 'success')}>
                        Toast succès
                    </Button>
                    <Button variant="danger" onClick={() => showToast('Une erreur est survenue', 'error')}>
                        Toast erreur
                    </Button>
                    <Button variant="outline" onClick={() => setModalOpen(true)}>
                        Ouvrir modale
                    </Button>
                </div>
            </Card>

            <Modal
                isOpen={modalOpen}
                onClose={() => setModalOpen(false)}
                title="Confirmer le transfert"
                footer={
                    <>
                        <Button variant="outline" onClick={() => setModalOpen(false)}>Annuler</Button>
                        <Button variant="primary" onClick={() => setModalOpen(false)}>Confirmer</Button>
                    </>
                }
            >
                <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.875rem' }}>
                    Êtes-vous sûr de vouloir envoyer 200 € vers Mamadou Diallo via Orange Money ?
                </p>
            </Modal>

        </div>
    );
}

function App() {
    return (
        <ToastProvider>
            <TestComponents />
        </ToastProvider>
    );
}

export default App;