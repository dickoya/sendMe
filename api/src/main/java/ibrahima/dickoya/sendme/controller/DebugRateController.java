package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.config.CurrencyConfig;
import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur exposant les taux de change en mode "debug".
 *
 * Utilisé par le tableau de bord administrateur pour afficher le taux brut,
 * le taux avec marge et le timestamp du cache pour toutes les paires supportées.
 *
 * Routes :
 * - GET /api/debug/rates → liste des paires avec leurs taux debug
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Tag(name = "Debug", description = "Endpoints de diagnostic des taux de change (utilisés par le dashboard admin).")
public class DebugRateController {

        private final ExchangeRateService exchangeRateService;

        /**
         * Retourne les taux de change debug pour toutes les paires de devises supportées.
         *
         * GET /api/debug/rates
         *
         * @return 200 OK avec la liste des paires { from, to, rawRate, marginRate, cacheTimestamp }
         */
        @GetMapping("/rates")
        @Operation(summary = "Tous les taux de change debug", description = "Retourne le taux brut, le taux avec marge et le timestamp du cache pour toutes les paires de devises supportées.")
        public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllDebugRates() {
                List<Map<String, Object>> rates = new ArrayList<>();
                for (String[] pair : CurrencyConfig.getExchangeRatePairs()) {
                        String from = pair[0];
                        String to = pair[1];
                        Map<String, Object> entry = new HashMap<>();
                        entry.put("from", from);
                        entry.put("to", to);
                        entry.put("rawRate", exchangeRateService.getRawRate(from, to));
                        entry.put("marginRate", exchangeRateService.getMarginRate(from, to));
                        entry.put("cacheTimestamp", exchangeRateService.getCacheTimestamp(from));
                        rates.add(entry);
                }
                return ResponseEntity.ok(
                                ApiResponse.success("Taux de change debug", rates));
        }
}
