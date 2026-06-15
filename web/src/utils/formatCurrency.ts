/**
 * Formate un montant avec sa devise selon les conventions françaises.
 *
 * @example
 * formatCurrency(128745, 'XOF') // "128 745 XOF"
 * formatCurrency(203.5, 'EUR')  // "203,50 EUR"
 */
export function formatCurrency(amount: number, currency: string): string {
    const noDecimalCurrencies = ['XOF', 'XAF', 'GNF'];
    const formatted = new Intl.NumberFormat('fr-FR', {
        minimumFractionDigits: noDecimalCurrencies.includes(currency) ? 0 : 2,
        maximumFractionDigits: noDecimalCurrencies.includes(currency) ? 0 : 2,
    }).format(amount);

    return `${formatted} ${currency}`;
}