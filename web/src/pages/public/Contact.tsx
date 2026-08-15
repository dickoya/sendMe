import { useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, LifeBuoy, Clock, Scale, Send, CheckCircle, AlertCircle } from 'lucide-react';
import Input from '@/components/ui/Input';
import Button from '@/components/ui/Button';
import api from '@/services/api';

/**
 * Page Contact — design moderne et épuré, cohérent avec l'accueil.
 *
 * Le formulaire n'a pas d'endpoint backend : à l'envoi, il construit un
 * lien mailto: pré-rempli qui ouvre le client de messagerie de l'utilisateur
 * avec son message. C'est une solution honnête et fonctionnelle pour un TFE.
 */
function Contact() {
    const { t } = useTranslation('contact');

    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [subject, setSubject] = useState('general');
    const [message, setMessage] = useState('');
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [submitted, setSubmitted] = useState(false);
    const [sending, setSending] = useState(false);
    const [sendError, setSendError] = useState<string | null>(null);

    const validate = () => {
        const next: Record<string, string> = {};
        if (!name.trim()) next.name = t('page.errors.name');
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) next.email = t('page.errors.email');
        if (!message.trim()) next.message = t('page.errors.message');
        setErrors(next);
        return Object.keys(next).length === 0;
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        setSending(true);
        setSendError(null);
        try {
            await api.post('/contact', {
                name,
                email,
                subject: t(`subjects.${subject}`),
                message,
            });
            setSubmitted(true);
        } catch {
            setSendError(t('page.sendError'));
        } finally {
            setSending(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-b from-gray-50 to-white dark:from-[#0D0D0D] dark:to-[#111111]">
            {/* Héro */}
            <section className="border-b border-gray-100 bg-white pb-14 pt-12 dark:border-[#222222] dark:bg-[#0D0D0D]">
                <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                    <div className="mx-auto max-w-2xl text-center">
                        <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 to-brand-600 shadow-lg shadow-brand-500/15">
                            <Mail size={26} className="text-white" />
                        </div>
                        <h1 className="text-3xl font-bold tracking-tight text-gray-900 sm:text-4xl dark:text-gray-100">
                            {t('page.title')}
                        </h1>
                        <p className="mt-4 text-lg leading-relaxed text-gray-500 dark:text-gray-400">
                            {t('page.subtitle')}
                        </p>
                    </div>
                </div>
            </section>

            {/* Formulaire + coordonnées */}
            <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
                <div className="grid gap-8 lg:grid-cols-3">
                    {/* Formulaire */}
                    <div className="lg:col-span-2">
                        <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm sm:p-8 dark:border-[#1E1E30] dark:bg-[#12121A]">
                            <h2 className="text-xl font-semibold text-gray-900 dark:text-gray-100">{t('page.formTitle')}</h2>
                            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{t('page.formSubtitle')}</p>

                            {submitted && (
                                <div className="mt-5 flex items-start gap-3 rounded-xl border border-emerald-200 bg-emerald-50 p-4 dark:border-emerald-700/40 dark:bg-emerald-900/20">
                                    <CheckCircle size={18} className="mt-0.5 shrink-0 text-emerald-600 dark:text-emerald-400" />
                                    <p className="text-sm text-emerald-800 dark:text-emerald-300">{t('page.success')}</p>
                                </div>
                            )}

                            {sendError && (
                                <div className="mt-5 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 p-4 dark:border-red-700/40 dark:bg-red-900/20">
                                    <AlertCircle size={18} className="mt-0.5 shrink-0 text-red-600 dark:text-red-400" />
                                    <p className="text-sm text-red-700 dark:text-red-300">{sendError}</p>
                                </div>
                            )}

                            <form onSubmit={handleSubmit} noValidate className="mt-6 space-y-5">
                                <div className="grid gap-5 sm:grid-cols-2">
                                    <Input
                                        label={t('page.name')}
                                        value={name}
                                        onChange={(e) => setName(e.target.value)}
                                        placeholder={t('page.namePlaceholder')}
                                        error={errors.name}
                                        required
                                    />
                                    <Input
                                        label={t('page.email')}
                                        type="email"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        placeholder={t('page.emailPlaceholder')}
                                        error={errors.email}
                                        required
                                    />
                                </div>

                                <div>
                                    <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                                        {t('page.subject')}
                                    </label>
                                    <select
                                        value={subject}
                                        onChange={(e) => setSubject(e.target.value)}
                                        className="w-full rounded-xl border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 outline-none ring-1 ring-gray-200 transition-all focus:ring-2 focus:ring-brand-400/60 dark:border-[#2A2A40] dark:bg-[#0A0A0F] dark:text-gray-100 dark:ring-[#2A2A40]"
                                    >
                                        <option value="general">{t('subjects.general')}</option>
                                        <option value="support">{t('subjects.support')}</option>
                                        <option value="feedback">{t('subjects.feedback')}</option>
                                        <option value="other">{t('subjects.other')}</option>
                                    </select>
                                </div>

                                <div>
                                    <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                                        {t('page.message')}
                                    </label>
                                    <textarea
                                        value={message}
                                        onChange={(e) => setMessage(e.target.value)}
                                        rows={5}
                                        placeholder={t('page.messagePlaceholder')}
                                        className="w-full rounded-xl border border-gray-200 bg-gray-50 px-3 py-2.5 text-sm text-gray-900 outline-none ring-1 ring-gray-200 transition-all placeholder:text-gray-400 focus:ring-2 focus:ring-brand-400/60 dark:border-[#2A2A40] dark:bg-[#0A0A0F] dark:text-gray-100 dark:placeholder:text-gray-500 dark:ring-[#2A2A40]"
                                    />
                                    {errors.message && (
                                        <p className="mt-1 text-xs text-danger-600 dark:text-danger-400">{errors.message}</p>
                                    )}
                                </div>

                                <Button type="submit" size="lg" icon={<Send size={16} />} loading={sending}>
                                    {t('page.submit')}
                                </Button>
                            </form>
                        </div>
                    </div>
                    {/* Coordonnées */}
                    <div className="space-y-5">
                        {[
                            { icon: Mail, title: t('info.email'), value: t('info.emailValue'), href: 'mailto:contact@sendme-tfe.fr' },
                            { icon: LifeBuoy, title: t('info.support'), value: t('info.supportValue'), href: 'mailto:support@sendme.com' },
                            { icon: Clock, title: t('info.hours'), value: t('info.hoursValue'), href: undefined },
                        ].map((item) => (
                            <div
                                key={item.title}
                                className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm dark:border-[#1E1E30] dark:bg-[#12121A]"
                            >
                                <div className="flex items-center gap-3">
                                    <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-300">
                                        <item.icon size={20} />
                                    </div>
                                    <div>
                                        <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{item.title}</p>
                                        {item.href ? (
                                            <a
                                                href={item.href}
                                                className="text-sm text-gray-500 transition-colors hover:text-brand-600 dark:text-gray-400 dark:hover:text-brand-400"
                                            >
                                                {item.value}
                                            </a>
                                        ) : (
                                            <p className="text-sm text-gray-500 dark:text-gray-400">{item.value}</p>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))}

                        <div className="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm dark:border-[#1E1E30] dark:bg-[#12121A]">
                            <div className="flex items-center gap-3">
                                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-600 dark:bg-brand-500/10 dark:text-brand-300">
                                    <Scale size={20} />
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{t('info.legal')}</p>
                                    <Link
                                        to="/legal"
                                        className="text-sm text-gray-500 transition-colors hover:text-brand-600 dark:text-gray-400 dark:hover:text-brand-400"
                                    >
                                        {t('info.legalLink')}
                                    </Link>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Footer compact */}
            <footer className="border-t border-gray-100 bg-white py-8 dark:border-[#1E1E30] dark:bg-[#0A0A0F]">
                <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-4 px-4 sm:flex-row sm:px-6 lg:px-8">
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                        © {new Date().getFullYear()} SendMe. {t('footer.rights')}
                    </p>
                    <div className="flex items-center gap-6">
                        <Link to="/" className="text-sm text-gray-500 transition-colors hover:text-brand-600 dark:text-gray-400 dark:hover:text-brand-400">
                            {t('footer.home')}
                        </Link>
                        <Link to="/legal" className="text-sm text-gray-500 transition-colors hover:text-brand-600 dark:text-gray-400 dark:hover:text-brand-400">
                            {t('footer.legal')}
                        </Link>
                    </div>
                </div>
            </footer>
        </div>
    );
}

export default Contact;


