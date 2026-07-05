import { useState } from 'react';
import { Plus, Power, PowerOff, Loader2, AlertCircle, CreditCard } from 'lucide-react';
import { usePaymentChannels } from '@/hooks/usePaymentChannels';
import PageHeader from '@/components/ui/PageHeader';
import Alert from '@/components/ui/Alert';
import Spinner from '@/components/ui/Spinner';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import PaymentChannelLogo from '@/components/admin/PaymentChannelLogo';
import { cn } from '@/lib/utils';

/**
 * Page admin de gestion des canaux de paiement.
 *
 * Permet à l'administrateur de :
 * - Visualiser tous les canaux (actifs/inactifs)
 * - Activer/désactiver un canal
 * - Ajouter un nouveau canal
 */
function AdminPaymentChannels() {
    const { channels, loading, error, addChannel, toggleChannel } = usePaymentChannels();

    // État du formulaire d'ajout
    const [showAddForm, setShowAddForm] = useState(false);
    const [newChannelType, setNewChannelType] = useState('');
    const [newDisplayName, setNewDisplayName] = useState('');
    const [newIcon, setNewIcon] = useState('');
    const [adding, setAdding] = useState(false);
    const [addError, setAddError] = useState<string | null>(null);

    // Confirmation toggle
    const [toggleConfirm, setToggleConfirm] = useState<{ id: number; name: string; active: boolean } | null>(null);

    const handleAdd = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newChannelType.trim() || !newDisplayName.trim()) return;

        setAdding(true);
        setAddError(null);

        const success = await addChannel({
            channelType: newChannelType.trim().toUpperCase().replace(/\s+/g, '_'),
            displayName: newDisplayName.trim(),
            icon: newIcon.trim() || undefined,
        });

        setAdding(false);

        if (success) {
            setShowAddForm(false);
            setNewChannelType('');
            setNewDisplayName('');
            setNewIcon('');
        } else {
            setAddError("Erreur lors de l'ajout du canal");
        }
    };

    const handleToggleConfirm = async () => {
        if (!toggleConfirm) return;
        await toggleChannel(toggleConfirm.id);
        setToggleConfirm(null);
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center py-20">
                <Spinner size="lg" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <PageHeader
                title="Canaux de paiement"
                subtitle="Gérez les canaux de réception disponibles sur la plateforme"
                actions={
                    <Button onClick={() => setShowAddForm(!showAddForm)}>
                        <Plus size={16} className="mr-2" />
                        Ajouter un canal
                    </Button>
                }
            />

            {error && (
                <Alert type="error" message={error} />
            )}

            {/* Formulaire d'ajout */}
            {showAddForm && (
                <div className="rounded-xl border border-gray-200 bg-white p-6 dark:border-[#222222] dark:bg-[#0D0D0D]">
                    <h3 className="mb-4 text-lg font-semibold text-gray-900 dark:text-[#EDEEF0]">
                        Nouveau canal de paiement
                    </h3>
                    <form onSubmit={handleAdd} className="space-y-4">
                        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                            <Input
                                label="Type technique"
                                placeholder="Ex: PAYPAL, STRIPE"
                                value={newChannelType}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewChannelType(e.target.value)}
                                required
                            />
                            <Input
                                label="Nom d'affichage"
                                placeholder="Ex: PayPal"
                                value={newDisplayName}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewDisplayName(e.target.value)}
                                required
                            />
                            <Input
                                label="Icône (emoji)"
                                placeholder="Ex: 💳"
                                value={newIcon}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewIcon(e.target.value)}
                                maxLength={10}
                            />
                        </div>

                        {addError && (
                            <p className="text-sm text-red-500">{addError}</p>
                        )}

                        <div className="flex gap-3">
                            <Button type="submit" disabled={adding}>
                                {adding ? (
                                    <>
                                        <Loader2 size={16} className="mr-2 animate-spin" />
                                        Ajout en cours...
                                    </>
                                ) : (
                                    'Ajouter le canal'
                                )}
                            </Button>
                            <Button
                                type="button"
                                variant="ghost"
                                onClick={() => {
                                    setShowAddForm(false);
                                    setAddError(null);
                                }}
                            >
                                Annuler
                            </Button>
                        </div>
                    </form>
                </div>
            )}

            {/* Liste des canaux */}
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
                {channels.map((channel) => (
                    <div
                        key={channel.id}
                        className={cn(
                            'rounded-xl border p-5 transition-all duration-200',
                            channel.isActive
                                ? 'border-green-200 bg-green-50/50 dark:border-green-900/50 dark:bg-green-950/20'
                                : 'border-gray-200 bg-white dark:border-[#222222] dark:bg-[#0D0D0D]'
                        )}
                    >
                        <div className="flex items-start justify-between">
                            <div className="flex items-center gap-3">
                                <PaymentChannelLogo channelType={channel.channelType} size={40} />
                                <div>
                                    <h3 className="font-semibold text-gray-900 dark:text-[#EDEEF0]">
                                        {channel.displayName}
                                    </h3>
                                    <p className="text-xs text-gray-500 dark:text-[#8B8FA3]">
                                        {channel.channelType}
                                    </p>
                                </div>
                            </div>

                            <button
                                onClick={() =>
                                    setToggleConfirm({
                                        id: channel.id,
                                        name: channel.displayName,
                                        active: channel.isActive,
                                    })
                                }
                                className={cn(
                                    'flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors',
                                    channel.isActive
                                        ? 'bg-green-100 text-green-700 hover:bg-green-200 dark:bg-green-900/30 dark:text-green-400 dark:hover:bg-green-900/50'
                                        : 'bg-gray-100 text-gray-500 hover:bg-gray-200 dark:bg-[#1A1A1A] dark:text-[#8B8FA3] dark:hover:bg-[#222222]'
                                )}
                            >
                                {channel.isActive ? (
                                    <>
                                        <Power size={12} />
                                        Actif
                                    </>
                                ) : (
                                    <>
                                        <PowerOff size={12} />
                                        Inactif
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {channels.length === 0 && !loading && (
                <div className="flex flex-col items-center justify-center py-16 text-gray-500 dark:text-[#8B8FA3]">
                    <CreditCard size={48} className="mb-4 opacity-40" />
                    <p className="text-lg font-medium">Aucun canal de paiement</p>
                    <p className="text-sm">Ajoutez un canal pour commencer.</p>
                </div>
            )}

            {/* Dialogue de confirmation */}
            <ConfirmDialog
                isOpen={!!toggleConfirm}
                onClose={() => setToggleConfirm(null)}
                title={toggleConfirm?.active ? 'Désactiver le canal' : 'Activer le canal'}
                message={
                    toggleConfirm?.active
                        ? `Êtes-vous sûr de vouloir désactiver "${toggleConfirm?.name}" ? Les utilisateurs ne pourront plus utiliser ce canal pour leurs transferts.`
                        : `Êtes-vous sûr de vouloir réactiver "${toggleConfirm?.name}" ? Les utilisateurs pourront à nouveau utiliser ce canal.`
                }
                confirmLabel={toggleConfirm?.active ? 'Désactiver' : 'Activer'}
                variant={toggleConfirm?.active ? 'danger' : 'primary'}
                onConfirm={handleToggleConfirm}
            />
        </div>
    );
}

export default AdminPaymentChannels;
