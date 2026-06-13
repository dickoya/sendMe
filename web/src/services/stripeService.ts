import { loadStripe, type Stripe } from '@stripe/stripe-js';
import { STRIPE_PUBLIC_KEY } from '@/utils/constants';

/**
 * Instance Stripe.js partagée — chargée une seule fois (singleton).
 * loadStripe() retourne une Promise mise en cache par le SDK Stripe.
 */
export const stripePromise: Promise<Stripe | null> = loadStripe(STRIPE_PUBLIC_KEY);