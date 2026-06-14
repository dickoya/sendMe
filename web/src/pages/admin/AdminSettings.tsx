import Card from '@/components/ui/Card';
import FeeConfigForm from '@/components/forms/FeeConfigForm';
import MaintenanceForm from '@/components/forms/MaintenanceForm';

/**
 * Page de paramètres de la plateforme (admin).
 *
 * - Configuration du taux de commission
 * - Notification de maintenance programmée
 */
function AdminSettings() {
    return (
        <div className="flex flex-col gap-6">
            <Card title="Frais de la plateforme">
                <FeeConfigForm />
            </Card>

            <Card title="Notification de maintenance">
                <MaintenanceForm />
            </Card>
        </div>
    );
}

export default AdminSettings;