package ibrahima.dickoya.sendme.utils;

/**
 * Utilitaires pour masquer les données sensibles dans les logs et PDFs.
 * 
 * Cette classe centralise les méthodes de masquage pour éviter
 * la duplication de code et assurer une cohérence dans toute l'application.
 */
public final class DataMaskingUtils {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     * Cette classe ne contient que des méthodes statiques utilitaires.
     */
    private DataMaskingUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Masque un numéro de téléphone pour la sécurité dans les logs.
     * 
     * Affiche seulement les premiers caractères et masque les 4 derniers.
     * 
     * @param phone le numéro de téléphone complet
     * @return le numéro masqué
     * 
     * @example
     * 
     *          <pre>
     * maskPhone("+22507123456") → "+2250712****"
     * maskPhone("+33612345678") → "+336123456****"
     * maskPhone("123") → "****"
     * maskPhone(null) → "****"
     *          </pre>
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return "****";
        }
        return phone.substring(0, phone.length() - 4) + "****";
    }

    /**
     * Masque un IBAN pour la sécurité dans les logs et PDFs.
     * 
     * Affiche seulement les 4 premiers et 6 derniers caractères.
     * 
     * @param iban l'IBAN complet
     * @return l'IBAN masqué
     * 
     * @example
     * 
     *          <pre>
     * maskIban("FR7630006000011234567890189") → "FR76****890189"
     * maskIban("DE89370400440532013000") → "DE89****013000"
     * maskIban("FR76") → "****"
     * maskIban(null) → "****"
     *          </pre>
     */
    public static String maskIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return "****";
        }
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 6);
    }

    /**
     * Masque un numéro de carte bancaire pour la sécurité.
     * 
     * Affiche seulement les 4 premiers et 4 derniers chiffres.
     * 
     * @param cardNumber le numéro de carte complet
     * @return le numéro masqué
     * 
     * @example
     * 
     *          <pre>
     * maskCardNumber("4532123456789012") → "4532 **** **** 9012"
     * maskCardNumber("1234") → "****"
     * maskCardNumber(null) → "****"
     *          </pre>
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + " **** **** " +
                cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Masque une adresse email pour la sécurité.
     * 
     * Affiche seulement les 2 premiers caractères du nom d'utilisateur
     * et le domaine complet.
     * 
     * @param email l'adresse email complète
     * @return l'email masqué
     * 
     * @example
     * 
     *          <pre>
     * maskEmail("john.doe@example.com") → "jo****@example.com"
     * maskEmail("a@test.fr") → "****@test.fr"
     * maskEmail("invalid") → "****"
     * maskEmail(null) → "****"
     *          </pre>
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) {
            return "****@" + parts[1];
        }
        return parts[0].substring(0, 2) + "****@" + parts[1];
    }
}
