package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur de debug pour vérifier le fonctionnement
 * du service de taux de change.
 *
 * Accessible sans authentification (utile pour les tests).
 * En production, il faudrait le sécuriser.
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Tag(name = "Debug", description = "Endpoints de debug pour vérifier les taux de change")
public class DebugController {

        private final ExchangeRateService exchangeRateService;

        /**
         * Endpoint debug qui retourne toutes les informations
         * sur le taux de change entre deux devises.
         *
         * Exemple : GET /api/debug/rate?from=EUR&to=XOF
         *
         * Réponse :
         * - rawRate : taux brut sans marge
         * - marginRate : taux avec marge de 2%
         * - marginPercent : pourcentage de marge appliqué
         * - cacheTimestamp : date du dernier rafraîchissement
         */
        @GetMapping("/rate")
        @Operation(summary = "Debug taux de change", description = "Retourne le taux brut, le taux avec marge, " +
                        "et le timestamp du cache pour un couple de devises.")
        public ResponseEntity<ApiResponse<Map<String, Object>>> debugRate(
                        @RequestParam(defaultValue = "EUR") String from,
                        @RequestParam(defaultValue = "XOF") String to) {

                Map<String, Object> debugInfo = new HashMap<>();

                // Taux brut (sans marge)
                BigDecimal rawRate = exchangeRateService.getRawRate(from, to);
                debugInfo.put("rawRate", rawRate);

                // Taux avec marge (celui utilisé dans les simulations)
                BigDecimal marginRate = exchangeRateService.getRate(from, to);
                debugInfo.put("marginRate", marginRate);

                // Pourcentage de marge
                debugInfo.put("marginPercent", exchangeRateService.getMarginPercentage());

                // Timestamp du cache
                debugInfo.put("cacheTimestamp", exchangeRateService.getCacheTimestamp(from));

                // Informations sur la requête
                debugInfo.put("from", from.toUpperCase());
                debugInfo.put("to", to.toUpperCase());

                return ResponseEntity.ok(
                                ApiResponse.success("Debug taux de change", debugInfo));
        }

        /**
         * Endpoint debug qui retourne les taux pour TOUTES les paires de devises
         * supportées (EUR, USD, XOF, GNF).
         *
         * Exemple : GET /api/debug/rates
         *
         * Réponse :
         * Liste d'objets contenant pour chaque paire :
         * - from : devise source
         * - to : devise cible
         * - rawRate : taux brut (API OpenExchangeRates)
         * - marginRate : taux final avec marge de 2%
         * - cacheTimestamp : date du dernier rafraîchissement du cache
         */
        @GetMapping("/rates")
        @Operation(summary = "Debug tous les taux de change", description = "Retourne pour toutes les paires de devises supportées "
                        +
                        "(EUR, USD, XOF, GNF) : le taux brut, le taux avec marge, " +
                        "et le timestamp du cache.")
        public ResponseEntity<ApiResponse<List<Map<String, Object>>>> debugAllRates() {

                List<Map<String, Object>> allRates = exchangeRateService.getAllDebugRates();

                return ResponseEntity.ok(
                                ApiResponse.success("Tous les taux de change", allRates));
        }
}
