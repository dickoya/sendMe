import { loadStripe, type Stripe, type StripeElementLocale } from '@stripe/stripe-js';
import { STRIPE_PUBLIC_KEY } from '@/utils/constants';

/**
 * Instances Stripe.js mises en cache par locale.
 *
 * loadStripe() doit être appelé avec la locale souhaitée pour que
 * Stripe traduise ses propres messages (erreurs de carte, labels
 * du CardElement). Comme la locale peut changer en cours de session
 * (LanguageSwitcher), on ne peut plus utiliser un singleton unique —
 * on met en cache une promesse par locale pour éviter de recharger
 * le script Stripe à chaque changement de langue.
 */
const stripeInstances = new Map<string, Promise<Stripe | null>>();

/**
 * Retourne (ou crée) l'instance Stripe.js pour la locale donnée.
 *
 * @param locale Code de langue i18next (ex: "fr", "en") — converti
 *               vers les locales supportées par Stripe.
 *
 * @example
 * const stripe = getStripe(i18n.language);
 * <Elements stripe={stripe}>...</Elements>
 */
export function getStripe(locale: string): Promise<Stripe | null> {
    const stripeLocale = toStripeLocale(locale);

    if (!stripeInstances.has(stripeLocale)) {
        stripeInstances.set(stripeLocale, loadStripe(STRIPE_PUBLIC_KEY, { locale: stripeLocale }));
    }

    return stripeInstances.get(stripeLocale)!;
}

/**
 * Convertit un code de langue i18next vers une locale Stripe valide.
 * Stripe utilise "en" pour l'anglais et "fr" pour le français —
 * conversion directe pour ces deux langues, "auto" en repli.
 */
function toStripeLocale(locale: string): StripeElementLocale {
    if (locale === 'fr' || locale === 'en') return locale;
    return 'auto';
}