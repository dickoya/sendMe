import { loadStripe, type Stripe, type StripeElementLocale } from '@stripe/stripe-js';
import { STRIPE_PUBLIC_KEY, EXPECTED_STRIPE_ACCOUNT } from '@/utils/constants';

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
        assertStripeKeyAccount();
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

/**
 * Garde-fou : refuse de charger Stripe si la clé publique embarquée dans le
 * bundle provient d'un AUTRE compte que le compte attendu.
 *
 * Vite "fige" les variables d'env au démarrage/build. Si web/.env est modifié
 * mais que le serveur Vite / le build n'est pas relancé, le bundle garde l'ancienne
 * clé (autre compte) → le backend reçoit des pm_ du mauvais compte et renvoie
 * "resource_missing". Ce garde-fou fait échouer le chargement avec un message clair.
 */
function assertStripeKeyAccount(): void {
    if (!STRIPE_PUBLIC_KEY) {
        throw new Error(
            'VITE_STRIPE_PUBLIC_KEY n\'est pas défini. '
                + 'Ajoute-le dans web/.env puis relance/rebuild le frontend.'
        );
    }
    const account = stripeAccountFromPublicKey(STRIPE_PUBLIC_KEY);
    if (account && account !== EXPECTED_STRIPE_ACCOUNT) {
        throw new Error(
            'Clé publique Stripe du compte "' + account + '" alors que le compte attendu est "'
                + EXPECTED_STRIPE_ACCOUNT + '". '
                + 'Le bundle utilise une ANCIENNE clé. '
                + 'Arrête et relance Vite (npm run dev) ou rebuild (npm run build) pour recharger web/.env.'
        );
    }
}

/**
 * Extrait le préfixe de compte d'une clé publique Stripe.
 * Ex : "pk_test_51U15b7..."  →  "1U15b7"
 */
function stripeAccountFromPublicKey(key: string): string {
    const marker = key.startsWith('pk_live_') ? 'pk_live_' : 'pk_test_';
    const idx = key.indexOf(marker);
    if (idx < 0) return '';
    let after = key.slice(idx + marker.length); // ex "51U15b7KC..."
    if (after.startsWith('5')) after = after.slice(1); // "1U15b7KC..."
    return after.slice(0, EXPECTED_STRIPE_ACCOUNT.length); // "1U15b7"
}