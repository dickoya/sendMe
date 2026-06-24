package ibrahima.dickoya.sendme.utils;

import java.util.Collections;
import java.util.Map;

/**
 * Utilitaire de mapping entre le code pays ISO 3166-1 alpha-3
 * et la devise ISO 4217 correspondante.
 * 
 * Utilisé pour déterminer la devise de réception d'un bénéficiaire
 * en fonction du pays d'enregistrement de son numéro de téléphone.
 */
public final class CurrencyByCountry {

    private static final Map<String, String> COUNTRY_TO_CURRENCY = Map.ofEntries(
            // Afrique de l'Ouest - UEMOA (XOF)
            Map.entry("BEN", "XOF"), // Bénin
            Map.entry("BFA", "XOF"), // Burkina Faso
            Map.entry("CIV", "XOF"), // Côte d'Ivoire
            Map.entry("GNB", "XOF"), // Guinée-Bissau
            Map.entry("MLI", "XOF"), // Mali
            Map.entry("NER", "XOF"), // Niger
            Map.entry("SEN", "XOF"), // Sénégal
            Map.entry("TGO", "XOF"), // Togo

            // Afrique centrale - CEMAC (XAF)
            Map.entry("CMR", "XAF"), // Cameroun
            Map.entry("CAF", "XAF"), // République centrafricaine
            Map.entry("TCD", "XAF"), // Tchad
            Map.entry("COG", "XAF"), // Congo
            Map.entry("GNQ", "XAF"), // Guinée équatoriale
            Map.entry("GAB", "XAF"), // Gabon

            // Guinée (GNF)
            Map.entry("GIN", "GNF"), // Guinée

            // Europe
            Map.entry("FRA", "EUR"), // France
            Map.entry("BEL", "EUR"), // Belgique
            Map.entry("DEU", "EUR"), // Allemagne
            Map.entry("ESP", "EUR"), // Espagne
            Map.entry("ITA", "EUR"), // Italie
            Map.entry("PRT", "EUR"), // Portugal
            Map.entry("NLD", "EUR"), // Pays-Bas
            Map.entry("LUX", "EUR"), // Luxembourg
            Map.entry("CHE", "CHF"), // Suisse
            Map.entry("GBR", "GBP"), // Royaume-Uni

            // Amérique du Nord
            Map.entry("USA", "USD"), // États-Unis
            Map.entry("CAN", "CAD"), // Canada

            // Autres
            Map.entry("MAR", "MAD"), // Maroc
            Map.entry("DZA", "DZD"), // Algérie
            Map.entry("TUN", "TND"), // Tunisie
            Map.entry("EGY", "EGP"), // Égypte
            Map.entry("ZAF", "ZAR") // Afrique du Sud
    );

    private CurrencyByCountry() {
        // Classe utilitaire - pas d'instanciation
    }

    /**
     * Retourne la Map complète des pays et devises.
     * Utilisé par l'API REST pour exposer la liste des pays supportés.
     * 
     * @return Map immuable avec code pays → code devise
     */
    public static Map<String, String> getAllCountries() {
        return Collections.unmodifiableMap(COUNTRY_TO_CURRENCY);
    }

    /**
     * Retourne la devise correspondant au code pays.
     * 
     * @param countryCode code pays ISO 3166-1 alpha-3 (ex: GIN, SEN, FRA)
     * @return code devise ISO 4217, ou null si le pays n'est pas reconnu
     */
    public static String getCurrency(String countryCode) {
        if (countryCode == null)
            return null;
        return COUNTRY_TO_CURRENCY.get(countryCode.toUpperCase());
    }

    /**
     * Retourne la devise par défaut pour un canal donné quand le pays n'est pas
     * spécifié.
     * 
     * @param channelName le nom du canal
     * @return code devise ISO 4217
     */
    public static String getDefaultCurrencyForChannel(String channelName) {
        return switch (channelName) {
            case "BANK_TRANSFER" -> "EUR";
            case "ORANGE_MONEY" -> "GNF";
            case "MTN_MOBILE_MONEY" -> "GNF";
            case "WAVE" -> "XOF";
            case "MOOV_MONEY" -> "XOF";
            case "AIRTEL_MONEY" -> "XOF";
            default -> "XOF";
        };
    }
}
