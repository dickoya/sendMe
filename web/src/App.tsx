import { useState } from 'react';
import { Mail, Send } from 'lucide-react';
import { ToastProvider, useToast } from '@/store/notification/toastContext';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Card from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Spinner from '@/components/ui/Spinner';
import Modal from '@/components/ui/Modal.tsx'

/**
 * Page de test temporaire — Étape 2.
 * Sera remplacée par AppRoutes à l'Étape 3.
 */
function TestComponents() {
    const { showToast } = useToast();
    const [modalOpen, setModalOpen] = useState(false);

    return (
        <div className="mx-auto flex max-w-lg flex-col gap-6 p-8">
            <h1 className="text-2xl font-bold text-brand-600">SendMe — Test Composants</h1>

            <Card title="Boutons" actions={<Badge variant="info">Étape 2</Badge>}>
                <div className="flex flex-wrap gap-2">
                    <Button icon={<Send size={16} />}>Primary</Button>
                    <Button variant="secondary">Secondary</Button>
                    <Button variant="outline">Outline</Button>
                    <Button variant="danger">Danger</Button>
                    <Button variant="success">Success</Button>
                    <Button loading>Chargement</Button>

                </div>
            </Card>

            <Card title="Champs de saisie">
                <div className="flex flex-col gap-4">
                    <Input label="Email" type="email" placeholder="jean@example.com" icon={<Mail size={16} />} required />
                    <Input label="Mot de passe" type="password" placeholder="••••••••" required />
                    <Input label="Avec erreur" error="Ce champ est obligatoire" />
                </div>
            </Card>

            <Card title="Badges de statut">
                <div className="flex flex-wrap gap-2">
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
                <div className="flex gap-2">
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
                        <Button onClick={() => setModalOpen(false)}>Confirmer</Button>
                    </>
                }
            >
                <p className="text-sm text-gray-600">
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