package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.CountryInfoResponse;
import ibrahima.dickoya.sendme.utils.CurrencyByCountry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur REST exposant les informations des pays et devises supportés.
 * 
 * Ce contrôleur permet au frontend de récupérer dynamiquement la liste
 * des pays et devises supportés par SendMe, plutôt que de les hardcoder.
 * 
 * Endpoints :
 * - GET /api/countries : liste complète des pays avec leurs devises
 * - GET /api/currencies : liste des devises uniques supportées
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Pays et Devises", description = "API pour récupérer les pays et devises supportés")
public class CountryController {

    /**
     * Récupère la liste complète des pays supportés avec leurs devises.
     * 
     * Endpoint public accessible sans authentification pour permettre
     * l'affichage des sélecteurs de pays avant connexion.
     * 
     * @return Liste des pays avec code, nom, devise, etc.
     * 
     * @apiNote GET /api/countries
     * @apiResponse 200 OK avec liste des pays
     * 
     * @example
     * 
     *          <pre>
     * [
     *   {
     *     "countryCode": "FR",
     *     "countryName": "France",
     *     "currencyCode": "EUR",
     *     "currencyName": "Euro",
     *     "currencySymbol": "€"
     *   },
     *   {
     *     "countryCode": "CI",
     *     "countryName": "Côte d'Ivoire",
     *     "currencyCode": "XOF",
     *     "currencyName": "Franc CFA",
     *     "currencySymbol": "CFA"
     *   }
     * ]
     *          </pre>
     */
    @GetMapping("/countries")
    @Operation(summary = "Liste des pays supportés", description = "Retourne la liste complète des pays supportés par SendMe avec leurs devises associées")
    public ResponseEntity<List<CountryInfoResponse>> getAllCountries() {

        List<CountryInfoResponse> countries = CurrencyByCountry.getAllCountries().entrySet()
                .stream()
                .map(entry -> CountryInfoResponse.builder()
                        .countryCode(entry.getKey())
                        .countryName(getCountryName(entry.getKey()))
                        .currencyCode(entry.getValue())
                        .currencyName(getCurrencyName(entry.getValue()))
                        .currencySymbol(getCurrencySymbol(entry.getValue()))
                        .build())
                .sorted(Comparator.comparing(CountryInfoResponse::getCountryName))
                .collect(Collectors.toList());

        return ResponseEntity.ok(countries);
    }

    /**
     * Récupère la liste des devises uniques supportées.
     * 
     * Utile pour afficher un sélecteur de devises simplifié
     * sans dupliquer les devises communes à plusieurs pays.
     * 
     * @return Map avec code devise comme clé et infos complètes comme valeur
     * 
     * @apiNote GET /api/currencies
     * @apiResponse 200 OK avec map des devises
     * 
     * @example
     * 
     *          <pre>
     * {
     *   "EUR": {
     *     "code": "EUR",
     *     "name": "Euro",
     *     "symbol": "€"
     *   },
     *   "XOF": {
     *     "code": "XOF",
     *     "name": "Franc CFA",
     *     "symbol": "CFA"
     *   }
     * }
     *          </pre>
     */
    @GetMapping("/currencies")
    @Operation(summary = "Liste des devises supportées", description = "Retourne la liste des devises uniques supportées par SendMe")
    public ResponseEntity<Map<String, Map<String, String>>> getAllCurrencies() {

        Map<String, Map<String, String>> currencies = CurrencyByCountry.getAllCountries().values()
                .stream()
                .distinct()
                .collect(Collectors.toMap(
                        currencyCode -> currencyCode,
                        currencyCode -> {
                            Map<String, String> info = new HashMap<>();
                            info.put("code", currencyCode);
                            info.put("name", getCurrencyName(currencyCode));
                            info.put("symbol", getCurrencySymbol(currencyCode));
                            return info;
                        },
                        (existing, replacement) -> existing,
                        TreeMap::new // Tri alphabétique
                ));

        return ResponseEntity.ok(currencies);
    }

    // Méthodes utilitaires privées

    /**
     * Retourne le nom complet d'un pays à partir de son code ISO.
     */
    private String getCountryName(String countryCode) {
        Map<String, String> countryNames = Map.ofEntries(
                Map.entry("FR", "France"),
                Map.entry("CI", "Côte d'Ivoire"),
                Map.entry("SN", "Sénégal"),
                Map.entry("ML", "Mali"),
                Map.entry("BF", "Burkina Faso"),
                Map.entry("BJ", "Bénin"),
                Map.entry("TG", "Togo"),
                Map.entry("NE", "Niger"),
                Map.entry("GN", "Guinée"),
                Map.entry("CM", "Cameroun"),
                Map.entry("CD", "République Démocratique du Congo"),
                Map.entry("MA", "Maroc"),
                Map.entry("US", "États-Unis"),
                Map.entry("GB", "Royaume-Uni"),
                Map.entry("CA", "Canada"));
        return countryNames.getOrDefault(countryCode, countryCode);
    }

    /**
     * Retourne le nom complet d'une devise à partir de son code ISO.
     */
    private String getCurrencyName(String currencyCode) {
        Map<String, String> currencyNames = Map.of(
                "EUR", "Euro",
                "XOF", "Franc CFA",
                "MAD", "Dirham Marocain",
                "GNF", "Franc Guinéen",
                "CDF", "Franc Congolais",
                "USD", "Dollar US",
                "GBP", "Livre Sterling",
                "CAD", "Dollar Canadien");
        return currencyNames.getOrDefault(currencyCode, currencyCode);
    }

    /**
     * Retourne le symbole d'une devise à partir de son code ISO.
     */
    private String getCurrencySymbol(String currencyCode) {
        Map<String, String> currencySymbols = Map.of(
                "EUR", "€",
                "XOF", "CFA",
                "MAD", "DH",
                "GNF", "FG",
                "CDF", "FC",
                "USD", "$",
                "GBP", "£",
                "CAD", "C$");
        return currencySymbols.getOrDefault(currencyCode, currencyCode);
    }
}
