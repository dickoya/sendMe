package ibrahima.dickoya.sendme.config;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuration centralisée des devises supportées par SendMe.
 * <p>
 * Définit pour chaque devise :
 * - code ISO 4217 (EUR, USD, GNF...)
 * - symbole monétaire (€, $, FG...)
 * - nombre de décimales
 * - nom du pays associé (affiché dans l'UI)
 * - type : SEND (envoi) ou RECEIVE (réception)
 */
@Getter
public enum CurrencyConfig {

    // Devises d'envoi (pays développés)
    EUR("EUR", "€", 2, "France", CurrencyType.SEND),
    USD("USD", "$", 2, "États-Unis", CurrencyType.SEND),
    GBP("GBP", "£", 2, "Royaume-Uni", CurrencyType.SEND),
    CAD("CAD", "C$", 2, "Canada", CurrencyType.SEND),
    CHF("CHF", "CHF", 2, "Suisse", CurrencyType.SEND),

    // Devises de réception (Afrique)
    XOF("XOF", "CFA", 0, "UEMOA (Bénin, Burkina, Côte d'Ivoire...)", CurrencyType.RECEIVE),
    XAF("XAF", "CFA", 0, "CEMAC (Cameroun, Congo, Gabon...)", CurrencyType.RECEIVE),
    GNF("GNF", "FG", 0, "Guinée", CurrencyType.RECEIVE),
    NGN("NGN", "₦", 2, "Nigeria", CurrencyType.RECEIVE),
    GHS("GHS", "₵", 2, "Ghana", CurrencyType.RECEIVE),
    ZAR("ZAR", "R", 2, "Afrique du Sud", CurrencyType.RECEIVE),
    MAD("MAD", "DH", 2, "Maroc", CurrencyType.RECEIVE),
    TND("TND", "DT", 3, "Tunisie", CurrencyType.RECEIVE);

    private final String code;
    private final String symbol;
    private final int decimals;
    private final String countryName;
    private final CurrencyType type;

    CurrencyConfig(String code, String symbol, int decimals, String countryName, CurrencyType type) {
        this.code = code;
        this.symbol = symbol;
        this.decimals = decimals;
        this.countryName = countryName;
        this.type = type;
    }

    public enum CurrencyType {
        SEND, // Devise d'envoi (EUR, USD, GBP...)
        RECEIVE // Devise de réception (XOF, GNF, NGN...)
    }

    /**
     * Récupère la configuration d'une devise par son code ISO.
     */
    public static CurrencyConfig fromCode(String code) {
        if (code == null)
            return EUR;
        return Arrays.stream(values())
                .filter(c -> c.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(EUR);
    }

    /**
     * Retourne toutes les devises d'envoi disponibles.
     */
    public static List<CurrencyConfig> getSendCurrencies() {
        return Arrays.stream(values())
                .filter(c -> c.type == CurrencyType.SEND)
                .collect(Collectors.toList());
    }

    /**
     * Retourne toutes les devises de réception disponibles.
     */
    public static List<CurrencyConfig> getReceiveCurrencies() {
        return Arrays.stream(values())
                .filter(c -> c.type == CurrencyType.RECEIVE)
                .collect(Collectors.toList());
    }

    /**
     * Retourne tous les codes de devises supportés.
     */
    public static String[] getAllCurrencyCodes() {
        return Arrays.stream(values())
                .map(CurrencyConfig::getCode)
                .toArray(String[]::new);
    }

    /**
     * Vérifie si une devise est supportée.
     */
    public static boolean isSupported(String code) {
        return Arrays.stream(values())
                .anyMatch(c -> c.code.equalsIgnoreCase(code));
    }

    /**
     * Retourne le nombre de décimales pour une devise donnée.
     * Utile pour Stripe (centimes) et l'affichage.
     */
    public static int getDecimals(String code) {
        return fromCode(code).getDecimals();
    }

    /**
     * Retourne le symbole monétaire pour une devise donnée.
     */
    public static String getSymbol(String code) {
        return fromCode(code).getSymbol();
    }

    /**
     * Retourne le nom du pays pour une devise donnée.
     */
    public static String getCountryName(String code) {
        return fromCode(code).getCountryName();
    }

    /**
     * Map des paires de devises pour le dashboard (taux de change en direct).
     * Génère toutes les combinaisons SEND → RECEIVE + SEND → SEND principales.
     */
    public static List<String[]> getExchangeRatePairs() {
        return List.of(
                // EUR → toutes
                new String[] { "EUR", "XOF" }, new String[] { "EUR", "XAF" },
                new String[] { "EUR", "GNF" }, new String[] { "EUR", "NGN" },
                new String[] { "EUR", "GHS" }, new String[] { "EUR", "ZAR" },
                new String[] { "EUR", "MAD" }, new String[] { "EUR", "TND" },
                new String[] { "EUR", "USD" }, new String[] { "EUR", "GBP" },
                new String[] { "EUR", "CAD" }, new String[] { "EUR", "CHF" },
                // USD → africaines
                new String[] { "USD", "XOF" }, new String[] { "USD", "XAF" },
                new String[] { "USD", "GNF" }, new String[] { "USD", "NGN" },
                new String[] { "USD", "GHS" }, new String[] { "USD", "ZAR" },
                new String[] { "USD", "MAD" }, new String[] { "USD", "TND" },
                new String[] { "USD", "EUR" },
                // GBP → africaines
                new String[] { "GBP", "XOF" }, new String[] { "GBP", "GNF" },
                new String[] { "GBP", "NGN" }, new String[] { "GBP", "GHS" },
                new String[] { "GBP", "ZAR" }, new String[] { "GBP", "EUR" },
                new String[] { "GBP", "USD" },
                // CAD → africaines
                new String[] { "CAD", "XOF" }, new String[] { "CAD", "GNF" },
                new String[] { "CAD", "NGN" }, new String[] { "CAD", "EUR" },
                // CHF → africaines
                new String[] { "CHF", "XOF" }, new String[] { "CHF", "GNF" },
                new String[] { "CHF", "NGN" }, new String[] { "CHF", "EUR" });
    }
}
