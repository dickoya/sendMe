import { useTranslation } from 'react-i18next';
import Card from '@/components/ui/Card';
import FeeConfigForm from '@/components/forms/FeeConfigForm';
import ExchangeRateConfigForm from '@/components/forms/ExchangeRateConfigForm';
import MaintenanceForm from '@/components/forms/MaintenanceForm';

/**
 * Page de paramètres de la plateforme (admin).
 *
 * Migration i18n (Phase 11) : titres de Card via namespace "admin".
 */
function AdminSettings() {
    const { t } = useTranslation('admin');

    return (
        <div className="flex flex-col gap-6">
            <Card title={t('settings.fees.title')}>
                <FeeConfigForm />
            </Card>

            <Card title={t('settings.exchangeRate.title')}>
                <ExchangeRateConfigForm />
            </Card>

            <Card title={t('settings.maintenance.title')}>
                <MaintenanceForm />
            </Card>
        </div>
    );
}

export default AdminSettings;
