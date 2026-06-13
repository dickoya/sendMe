import Card from '@/components/ui/Card';

/**
 * Tableau de bord utilisateur — placeholder.
 * Sera complété à l'Étape 5.
 */
function Dashboard() {
    return (
        <div className="flex flex-col gap-6">
            <Card title="Bienvenue sur SendMe 👋">
                <p className="text-sm text-gray-600">
                    Votre tableau de bord sera complété à l'Étape 5.
                </p>
            </Card>
        </div>
    );
}

export default Dashboard;