package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.entity.ExchangeRate;
import ibrahima.dickoya.sendme.repository.ExchangeRateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur exposant l'historique des taux de change
 * pour le dashboard administrateur.
 *
 * Accessible uniquement aux administrateurs (sécurisé via SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/exchange-rates")
@RequiredArgsConstructor
@Tag(name = "Admin - Taux de change", description = "Historique des taux de change pour le dashboard admin")
public class ExchangeRateHistoryController {

    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * Récupère les 50 derniers taux de change enregistrés.
     * Utilisé par le dashboard admin pour afficher l'historique.
     */
    @GetMapping("/history")
    @Operation(summary = "Historique des taux de change", description = "Retourne les 50 derniers taux récupérés depuis OpenExchangeRates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistory() {
        List<ExchangeRate> rates = exchangeRateRepository.findTop50ByOrderByFetchedAtDesc();

        Map<String, Object> data = new HashMap<>();
        data.put("rates", rates);
        data.put("total", rates.size());
        data.put("fetchedAt", LocalDateTime.now());

        return ResponseEntity.ok(
                ApiResponse.success("Historique des taux de change", data));
    }

    /**
     * Récupère l'historique filtré par couple de devises.
     */
    @GetMapping("/history/{from}/{to}")
    @Operation(summary = "Historique filtré par devises")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHistoryByPair(
            @PathVariable String from,
            @PathVariable String to) {

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<ExchangeRate> rates = exchangeRateRepository
                .findByBaseCurrencyAndTargetCurrencyAndFetchedAtBetweenOrderByFetchedAtDesc(
                        from.toUpperCase(), to.toUpperCase(), weekAgo, LocalDateTime.now());

        Map<String, Object> data = new HashMap<>();
        data.put("rates", rates);
        data.put("total", rates.size());
        data.put("from", from.toUpperCase());
        data.put("to", to.toUpperCase());
        data.put("period", "7 jours");

        return ResponseEntity.ok(
                ApiResponse.success("Historique des taux", data));
    }
}
