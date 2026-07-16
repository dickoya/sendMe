import { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import i18n from 'i18next';
import { useNavigate } from 'react-router-dom';
import {
    User,
    Mail,
    Phone,
    Lock,
    Shield,
    Globe,
    Moon,
    Sun,
    Trash2,
    Clock,
    CheckCircle,
    XCircle,
    Monitor,
    AlertTriangle,
    Gift,
    Ticket,
} from 'lucide-react';
import Card from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import Avatar from '@/components/ui/Avatar';
import Alert from '@/components/ui/Alert';
import EmptyState from '@/components/ui/EmptyState';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import Modal from '@/components/ui/Modal';
import { useAuth } from '@/store/auth/authContext';
import { useTheme } from '@/store/theme/themeContext';
import { useToast } from '@/store/notification/toastContext';
import { profileService, type ProfileData, type LoginHistoryEntry } from '@/services/profileService';
import { referralService } from '@/services/referralService';
import { getErrorMessage } from '@/lib/utils';
import { LANGUAGES } from '@/utils/constants';
import { formatDateTime } from '@/utils/formatDate';
import type { ReferralCouponResponse } from '@/types';

/**
 * Page de profil utilisateur complète.
 *
 * Sections :
 * 1. Informations personnelles (nom, email, téléphone)
 * 2. Changement de mot de passe
 * 3. Préférences (langue, thème)
 * 4. Historique de connexion
 * 5. Suppression de compte
 */
function Profile() {
    const { t } = useTranslation('profile');
    const { t: tCommon } = useTranslation('common');
    const { logout } = useAuth();
    const { theme, setTheme } = useTheme();
    const { showToast } = useToast();
    const navigate = useNavigate();

    // État du profil
    const [profile, setProfile] = useState<ProfileData | null>(null);
    const [isLoadingProfile, setIsLoadingProfile] = useState(true);
    const [profileError, setProfileError] = useState<string | null>(null);

    // Formulaire d'informations personnelles
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        phoneNumber: '',
    });
    const [isSavingProfile, setIsSavingProfile] = useState(false);

    // Formulaire de changement de mot de passe
    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });
    const [isChangingPassword, setIsChangingPassword] = useState(false);

    // État des préférences
    const [selectedLanguage, setSelectedLanguage] = useState(() => {
        return localStorage.getItem('sendme_language') || 'fr';
    });

    // Historique de connexion
    const [loginHistory, setLoginHistory] = useState<LoginHistoryEntry[]>([]);
    const [isLoadingHistory, setIsLoadingHistory] = useState(false);

    // Coupons de réduction
    const [coupons, setCoupons] = useState<ReferralCouponResponse[]>([]);
    const [isLoadingCoupons, setIsLoadingCoupons] = useState(false);

    // Suppression de compte
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deletePassword, setDeletePassword] = useState('');
    const [isDeleting, setIsDeleting] = useState(false);

    // Chargement du profil
    // Chargé une seule fois au montage du composant
    useEffect(() => {
        let cancelled = false;
        const loadProfile = async () => {
            setIsLoadingProfile(true);
            setProfileError(null);
            try {
                const data = await profileService.getProfile();
                if (!cancelled) {
                    setProfile(data);
                    setFormData({
                        firstName: data.firstName,
                        lastName: data.lastName,
                        email: data.email,
                        phoneNumber: data.phoneNumber ?? '',
                    });
                }
            } catch (err) {
                if (!cancelled) {
                    setProfileError(getErrorMessage(err));
                    showToast(t('errors.loadProfile'), 'error');
                }
            } finally {
                if (!cancelled) {
                    setIsLoadingProfile(false);
                }
            }
        };
        loadProfile();
        return () => { cancelled = true; };
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    // Chargement de l'historique de connexion
    // Chargé une seule fois au montage du composant
    useEffect(() => {
        let cancelled = false;
        const loadLoginHistory = async () => {
            setIsLoadingHistory(true);
            try {
                const history = await profileService.getLoginHistory();
                if (!cancelled) {
                    setLoginHistory(history);
                }
            } catch (err) {
                if (!cancelled) {
                    showToast(t('errors.loadHistory'), 'error');
                }
            } finally {
                if (!cancelled) {
                    setIsLoadingHistory(false);
                }
            }
        };
        loadLoginHistory();
        return () => { cancelled = true; };
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    // Chargement des coupons de réduction
    useEffect(() => {
        let cancelled = false;
        const loadCoupons = async () => {
            setIsLoadingCoupons(true);
            try {
                const data = await referralService.getCoupons();
                if (!cancelled) setCoupons(data);
            } catch {
                // Silencieux — les coupons ne sont pas bloquants
            } finally {
                if (!cancelled) setIsLoadingCoupons(false);
            }
        };
        loadCoupons();
        return () => { cancelled = true; };
    }, []);

    // Fonction de rechargement pour le bouton "Réessayer"
    const retryLoadProfile = useCallback(async () => {
        setIsLoadingProfile(true);
        setProfileError(null);
        try {
            const data = await profileService.getProfile();
            setProfile(data);
            setFormData({
                firstName: data.firstName,
                lastName: data.lastName,
                email: data.email,
                phoneNumber: data.phoneNumber ?? '',
            });
        } catch (err) {
            setProfileError(getErrorMessage(err));
            showToast(t('errors.loadProfile'), 'error');
        } finally {
            setIsLoadingProfile(false);
        }
    }, [t, showToast]);

    // Gestionnaires de formulaire
    const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setPasswordData((prev) => ({ ...prev, [name]: value }));
    };

    // Mise à jour du profil
    const handleSaveProfile = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSavingProfile(true);
        try {
            const updated = await profileService.updateProfile({
                firstName: formData.firstName,
                lastName: formData.lastName,
                email: formData.email,
                phoneNumber: formData.phoneNumber || undefined,
            });
            setProfile(updated);
            showToast(t('success.profileUpdated'), 'success');
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        } finally {
            setIsSavingProfile(false);
        }
    };

    // Changement de mot de passe
    const handleChangePassword = async (e: React.FormEvent) => {
        e.preventDefault();

        if (passwordData.newPassword !== passwordData.confirmPassword) {
            showToast(t('password.mismatch'), 'error');
            return;
        }

        if (passwordData.currentPassword === passwordData.newPassword) {
            showToast(t('password.samePassword'), 'error');
            return;
        }

        setIsChangingPassword(true);
        try {
            await profileService.changePassword({
                currentPassword: passwordData.currentPassword,
                newPassword: passwordData.newPassword,
                confirmPassword: passwordData.confirmPassword,
            });
            showToast(t('password.success'), 'success');
            setPasswordData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: '',
            });
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        } finally {
            setIsChangingPassword(false);
        }
    };

    // Changement de langue
    const handleLanguageChange = (langCode: string) => {
        i18n.changeLanguage(langCode);
        setSelectedLanguage(langCode);
    };

    // Suppression de compte
    const handleDeleteAccount = async () => {
        setIsDeleting(true);
        try {
            await profileService.deleteAccount({ password: deletePassword });
            showToast(t('deleteAccount.success'), 'success');
            setShowDeleteModal(false);
            setTimeout(() => {
                logout();
                navigate('/login');
            }, 1500);
        } catch (err) {
            showToast(getErrorMessage(err), 'error');
        } finally {
            setIsDeleting(false);
        }
    };

    // Affichage du chargement
    if (isLoadingProfile) {
        return <Spinner fullPage label={tCommon('states.loading')} />;
    }

    // Affichage d'erreur
    if (profileError && !profile) {
        return (
            <div className="flex min-h-[60vh] items-center justify-center">
                <div className="text-center">
                    <AlertTriangle size={48} className="mx-auto text-danger-500" />
                    <p className="mt-4 text-gray-600 dark:text-gray-300">{profileError}</p>
                    <Button variant="primary" className="mt-4" onClick={retryLoadProfile}>
                        {tCommon('actions.retry')}
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-6">
            {/* En-tête du profil */}
            <div className="rounded-2xl bg-gradient-to-br from-brand-600 to-brand-400 p-6 text-white shadow-[0_4px_15px_rgba(33,97,140,0.3)] dark:from-brand-700 dark:to-brand-500 dark:shadow-[0_0_30px_rgba(90,159,224,0.15)]">
                <div className="flex items-center gap-4">
                    <Avatar firstName={profile?.firstName ?? ''} lastName={profile?.lastName ?? ''} size="xl" className="ring-2 ring-white/30" />
                    <div>
                        <h2 className="text-xl font-bold">
                            {profile?.firstName} {profile?.lastName}
                        </h2>
                        <p className="mt-1 text-sm text-brand-50 dark:text-brand-200">{profile?.email}</p>
                        <p className="text-xs text-brand-100 dark:text-brand-300">
                            {tCommon('nav.dashboard')} — {t('title')}
                        </p>
                    </div>
                </div>
            </div>

            <div className="grid gap-6 lg:grid-cols-2">
                {/* Section 1 : Informations personnelles */}
                <Card title={t('sections.personalInfo')} className="lg:col-span-2">
                    <form onSubmit={handleSaveProfile} className="grid gap-4 sm:grid-cols-2">
                        <Input
                            label={t('fields.firstName')}
                            name="firstName"
                            value={formData.firstName}
                            onChange={handleProfileChange}
                            placeholder={t('placeholders.firstName')}
                            icon={<User size={16} />}
                            required
                        />
                        <Input
                            label={t('fields.lastName')}
                            name="lastName"
                            value={formData.lastName}
                            onChange={handleProfileChange}
                            placeholder={t('placeholders.lastName')}
                            icon={<User size={16} />}
                            required
                        />
                        <Input
                            label={t('fields.email')}
                            name="email"
                            type="email"
                            value={formData.email}
                            onChange={handleProfileChange}
                            placeholder={t('placeholders.email')}
                            icon={<Mail size={16} />}
                            required
                        />
                        <Input
                            label={t('fields.phoneNumber')}
                            name="phoneNumber"
                            value={formData.phoneNumber}
                            onChange={handleProfileChange}
                            placeholder={t('placeholders.phoneNumber')}
                            icon={<Phone size={16} />}
                        />
                        <div className="sm:col-span-2 flex justify-end">
                            <Button type="submit" variant="primary" loading={isSavingProfile}>
                                {isSavingProfile ? t('actions.saving') : tCommon('actions.save')}
                            </Button>
                        </div>
                    </form>
                </Card>

                {/* Section 2 : Changement de mot de passe */}
                <Card title={t('password.title')}>
                    <form onSubmit={handleChangePassword} className="flex flex-col gap-4">
                        <Input
                            label={t('fields.currentPassword')}
                            name="currentPassword"
                            type="password"
                            value={passwordData.currentPassword}
                            onChange={handlePasswordChange}
                            placeholder={t('placeholders.currentPassword')}
                            icon={<Lock size={16} />}
                            required
                        />
                        <Input
                            label={t('fields.newPassword')}
                            name="newPassword"
                            type="password"
                            value={passwordData.newPassword}
                            onChange={handlePasswordChange}
                            placeholder={t('placeholders.newPassword')}
                            icon={<Shield size={16} />}
                            hint={t('password.requirements')}
                            required
                        />
                        <Input
                            label={t('fields.confirmPassword')}
                            name="confirmPassword"
                            type="password"
                            value={passwordData.confirmPassword}
                            onChange={handlePasswordChange}
                            placeholder={t('placeholders.confirmPassword')}
                            icon={<Shield size={16} />}
                            required
                        />
                        <Button type="submit" variant="primary" loading={isChangingPassword} className="mt-2">
                            {isChangingPassword ? t('actions.changingPassword') : t('actions.changePassword')}
                        </Button>
                    </form>
                </Card>

                {/* Section 3 : Préférences */}
                <Card title={t('sections.preferences')}>
                    <div className="flex flex-col gap-6">
                        {/* Langue */}
                        <div>
                            <label className="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300">
                                <Globe size={16} className="mr-1.5 inline" />
                                {t('language.label')}
                            </label>
                            <div className="flex gap-2">
                                {LANGUAGES.map((lang) => (
                                    <button
                                        key={lang.code}
                                        type="button"
                                        onClick={() => handleLanguageChange(lang.code)}
                                        className={`flex items-center gap-2 rounded-lg border px-4 py-2.5 text-sm font-medium transition-all duration-200 ${
                                            selectedLanguage === lang.code
                                                ? 'border-brand-500 bg-brand-50 text-brand-700 dark:border-brand-400 dark:bg-brand-500/10 dark:text-brand-400'
                                                : 'border-gray-300 bg-white text-gray-700 hover:border-gray-400 dark:border-[#333333] dark:bg-transparent dark:text-[#8B8FA3] dark:hover:border-[#555555]'
                                        }`}
                                    >
                                        <span className="text-lg">
                                            {lang.code === 'fr' ? '🇫🇷' : '🇬🇧'}
                                        </span>
                                        <span>{lang.label}</span>
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Thème */}
                        <div>
                            <label className="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300">
                                {theme === 'dark' ? <Moon size={16} className="mr-1.5 inline" /> : <Sun size={16} className="mr-1.5 inline" />}
                                {t('theme.label')}
                            </label>
                            <div className="flex gap-2">
                                <button
                                    type="button"
                                    onClick={() => setTheme('light')}
                                    className={`flex items-center gap-2 rounded-lg border px-4 py-2.5 text-sm font-medium transition-all duration-200 ${
                                        theme === 'light'
                                            ? 'border-brand-500 bg-brand-50 text-brand-700 dark:border-brand-400 dark:bg-brand-500/10 dark:text-brand-400'
                                            : 'border-gray-300 bg-white text-gray-700 hover:border-gray-400 dark:border-[#333333] dark:bg-transparent dark:text-[#8B8FA3] dark:hover:border-[#555555]'
                                    }`}
                                >
                                    <Sun size={16} />
                                    <span>{t('theme.light')}</span>
                                </button>
                                <button
                                    type="button"
                                    onClick={() => setTheme('dark')}
                                    className={`flex items-center gap-2 rounded-lg border px-4 py-2.5 text-sm font-medium transition-all duration-200 ${
                                        theme === 'dark'
                                            ? 'border-brand-500 bg-brand-50 text-brand-700 dark:border-brand-400 dark:bg-brand-500/10 dark:text-brand-400'
                                            : 'border-gray-300 bg-white text-gray-700 hover:border-gray-400 dark:border-[#333333] dark:bg-transparent dark:text-[#8B8FA3] dark:hover:border-[#555555]'
                                    }`}
                                >
                                    <Moon size={16} />
                                    <span>{t('theme.dark')}</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </Card>

                {/* Section 4 : Mes coupons de réduction */}
                <Card title={t('sections.referralCoupons', 'Mes coupons de réduction')} className="lg:col-span-2">
                    {isLoadingCoupons ? (
                        <Spinner size="sm" />
                    ) : coupons.length === 0 ? (
                        <EmptyState
                            icon={<Ticket size={24} />}
                            title={t('referralCoupons.empty', 'Aucun coupon de réduction pour le moment')}
                        />
                    ) : (
                        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                            {coupons.map((coupon) => (
                                <div
                                    key={coupon.id}
                                    className={`rounded-xl border p-4 transition-all ${
                                        coupon.isUsed
                                            ? 'border-gray-200 bg-gray-50 opacity-60 dark:border-[#2a2a2a] dark:bg-[#1a1a1a]'
                                            : 'border-brand-200 bg-brand-50 dark:border-brand-800 dark:bg-brand-900/20'
                                    }`}
                                >
                                    <div className="flex items-center justify-between">
                                        <span className="text-xs font-medium uppercase tracking-wider text-gray-500">
                                            {coupon.couponType === 'SPONSOR' ? t('referralCoupons.sponsor', 'Parrain') : t('referralCoupons.referred', 'Filleul')}
                                        </span>
                                        {!coupon.isUsed && (
                                            <span className="rounded-full bg-accent-100 px-2 py-0.5 text-xs font-medium text-accent-700 dark:bg-accent-900/30 dark:text-accent-400">
                                                {t('referralCoupons.available', 'Disponible')}
                                            </span>
                                        )}
                                    </div>
                                    <p className="mt-2 text-lg font-bold text-brand-700 dark:text-brand-300">
                                        {coupon.discountAmount} {coupon.currency}
                                    </p>
                                    <p className="mt-1 text-xs text-gray-500">
                                        {coupon.isUsed
                                            ? t('referralCoupons.usedOn', 'Utilisé le {{date}}', { date: new Date(coupon.usedAt!).toLocaleDateString() })
                                            : t('referralCoupons.expiresOn', 'Expire le {{date}}', { date: new Date(coupon.expiresAt).toLocaleDateString() })}
                                    </p>
                                    {coupon.referredUserName && (
                                        <p className="mt-1 text-xs text-gray-400">
                                            {t('referralCoupons.referredBy', 'Parrainé par {{name}}', { name: coupon.referredUserName })}
                                        </p>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}
                </Card>

                {/* Section 5 : Historique de connexion */}
                <Card title={t('loginHistory.title')} className="lg:col-span-2">
                    {isLoadingHistory ? (
                        <Spinner size="sm" />
                    ) : loginHistory.length === 0 ? (
                        <EmptyState
                            icon={<Clock size={24} />}
                            title={t('loginHistory.empty')}
                        />
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-left text-sm">
                                <thead>
                                    <tr className="border-b border-gray-100 dark:border-[#222222]">
                                        <th className="pb-3 pr-4 font-medium text-gray-500 dark:text-gray-300">{t('loginHistory.date')}</th>
                                        <th className="pb-3 pr-4 font-medium text-gray-500 dark:text-gray-300">{t('loginHistory.ipAddress')}</th>
                                        <th className="pb-3 pr-4 font-medium text-gray-500 dark:text-gray-300">{t('loginHistory.device')}</th>
                                        <th className="pb-3 font-medium text-gray-500 dark:text-gray-300">{t('loginHistory.status')}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {loginHistory.map((entry) => (
                                        <tr key={entry.id} className="border-b border-gray-50 dark:border-[#1a1a1a] last:border-0">
                                            <td className="py-3 pr-4 text-gray-900 dark:text-[#EDEEF0]">
                                                {formatDateTime(entry.loginTime)}
                                            </td>
                                            <td className="py-3 pr-4 font-mono text-xs text-gray-600 dark:text-gray-300">
                                                {entry.ipAddress}
                                            </td>
                                            <td className="py-3 pr-4 text-gray-600 dark:text-gray-300">
                                                <div className="flex items-center gap-1.5">
                                                    <Monitor size={14} className="flex-shrink-0" />
                                                    <span className="truncate max-w-[200px]" title={entry.userAgent}>
                                                        {entry.userAgent ? entry.userAgent.split(' ')[0] : '—'}
                                                    </span>
                                                </div>
                                            </td>
                                            <td className="py-3">
                                                {entry.success ? (
                                                    <span className="inline-flex items-center gap-1 rounded-full bg-accent-50 px-2.5 py-0.5 text-xs font-medium text-accent-600 dark:bg-accent-900/30 dark:text-accent-400">
                                                        <CheckCircle size={12} />
                                                        {t('loginHistory.success')}
                                                    </span>
                                                ) : (
                                                    <span className="inline-flex items-center gap-1 rounded-full bg-danger-50 px-2.5 py-0.5 text-xs font-medium text-danger-600 dark:bg-danger-900/30 dark:text-danger-400">
                                                        <XCircle size={12} />
                                                        {t('loginHistory.failed')}
                                                    </span>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </Card>

                {/* Section 5 : Zone dangereuse — Suppression de compte */}
                <Card title={t('sections.dangerZone')} className="lg:col-span-2 border-danger-200 dark:border-danger-800">
                    <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                            <h4 className="font-medium text-gray-900 dark:text-[#EDEEF0]">{t('deleteAccount.title')}</h4>
                            <p className="mt-1 text-sm text-gray-500 dark:text-gray-300">
                                {t('deleteAccount.description')}
                            </p>
                        </div>
                        <Button
                            variant="danger"
                            onClick={() => setShowDeleteModal(true)}
                            icon={<Trash2 size={16} />}
                        >
                            {t('deleteAccount.button')}
                        </Button>
                    </div>
                </Card>
            </div>

            {/* Modal de confirmation de suppression */}
            <ConfirmDialog
                isOpen={showDeleteModal}
                onClose={() => {
                    setShowDeleteModal(false);
                    setDeletePassword('');
                }}
                onConfirm={handleDeleteAccount}
                title={t('deleteAccount.confirmTitle')}
                message={
                    <div className="flex flex-col gap-4">
                        <Alert
                            type="warning"
                            message={t('deleteAccount.confirmMessage')}
                        />
                        <Input
                            label={t('deleteAccount.confirmLabel')}
                            type="password"
                            value={deletePassword}
                            onChange={(e) => setDeletePassword(e.target.value)}
                            placeholder="••••••••"
                            icon={<Lock size={16} />}
                            required
                        />
                    </div>
                }
                confirmLabel={tCommon('actions.confirm')}
                cancelLabel={tCommon('actions.cancel')}
                variant="danger"
                isLoading={isDeleting}
            />
        </div>
    );
}

export default Profile;
