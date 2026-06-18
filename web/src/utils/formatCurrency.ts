/**
 * Devises sans décimale (monnaies avec zéro centime).
 * Utilisé par formatCurrency pour l'affichage.
 */
const NO_DECIMAL_CURRENCIES = ['XOF', 'XAF', 'GNF'];

/**
 * Devises avec 3 décimales (ex: TND).
 */
const THREE_DECIMAL_CURRENCIES = ['TND'];

/**
 * Formate un montant avec sa devise selon les conventions françaises.
 *
 * @example
 * formatCurrency(128745, 'XOF') // "128 745 XOF"
 * formatCurrency(203.5, 'EUR')  // "203,50 EUR"
 * formatCurrency(99.99, 'USD')  // "99,99 USD"
 * formatCurrency(50.123, 'TND') // "50,123 TND"
 */
export function formatCurrency(amount: number, currency: string): string {
    let decimals = 2;
    if (NO_DECIMAL_CURRENCIES.includes(currency)) {
        decimals = 0;
    } else if (THREE_DECIMAL_CURRENCIES.includes(currency)) {
        decimals = 3;
    }

    const formatted = new Intl.NumberFormat('fr-FR', {
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals,
    }).format(amount);

    return `${formatted} ${currency}`;
}

/**
 * Retourne le symbole monétaire pour une devise donnée.
 * @example
 * getCurrencySymbol('EUR') // '€'
 * getCurrencySymbol('USD') // '$'
 * getCurrencySymbol('XOF') // 'CFA'
 */
export function getCurrencySymbol(currency: string): string {
    const symbols: Record<string, string> = {
        EUR: '€',
        USD: '$',
        GBP: '£',
        CAD: 'C$',
        CHF: 'CHF',
        XOF: 'CFA',
        XAF: 'CFA',
        GNF: 'FG',
        NGN: '₦',
        GHS: '₵',
        ZAR: 'R',
        MAD: 'DH',
        TND: 'DT',
    };
    return symbols[currency] || currency;
}

/**
 * Retourne le nom du pays associé à une devise.
 * @example
 * getCurrencyCountry('EUR') // 'France'
 * getCurrencyCountry('NGN') // 'Nigeria'
 */
export function getCurrencyCountry(currency: string): string {
    const countries: Record<string, string> = {
        EUR: 'France',
        USD: 'États-Unis',
        GBP: 'Royaume-Uni',
        CAD: 'Canada',
        CHF: 'Suisse',
        XOF: 'UEMOA',
        XAF: 'CEMAC',
        GNF: 'Guinée',
        NGN: 'Nigeria',
        GHS: 'Ghana',
        ZAR: 'Afrique du Sud',
        MAD: 'Maroc',
        TND: 'Tunisie',
    };
    return countries[currency] || currency;
}
