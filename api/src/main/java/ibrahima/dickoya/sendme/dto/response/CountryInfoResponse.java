package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

/**
 * DTO représentant les informations d'un pays supporté par SendMe.
 * 
 * Contient le code du pays, son nom et sa devise associée.
 * Utilisé par l'endpoint API pour exposer la liste des pays supportés.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryInfoResponse {

    /**
     * Code ISO du pays (ex: "FR", "CI", "SN").
     */
    private String countryCode;

    /**
     * Nom complet du pays (ex: "France", "Côte d'Ivoire", "Sénégal").
     */
    private String countryName;

    /**
     * Code ISO de la devise (ex: "EUR", "XOF", "USD").
     */
    private String currencyCode;

    /**
     * Nom de la devise (ex: "Euro", "Franc CFA", "Dollar US").
     */
    private String currencyName;

    /**
     * Symbole de la devise (ex: "€", "CFA", "$").
     */
    private String currencySymbol;
}
