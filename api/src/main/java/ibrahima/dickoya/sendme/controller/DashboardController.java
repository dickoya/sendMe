package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.dto.response.LiveExchangeRateResponse;
import ibrahima.dickoya.sendme.dto.response.TransferEvolutionResponse;
import ibrahima.dickoya.sendme.dto.response.UserDashboardStatsResponse;
import ibrahima.dickoya.sendme.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour le tableau de bord utilisateur.
 * Expose les statistiques personnelles, l'évolution des transferts
 * et les taux de change en direct.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard utilisateur", description = "Statistiques personnelles, évolution des transferts et taux de change")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Statistiques personnelles", description = "Retourne les statistiques de l'utilisateur connecté : total envoyé, économies, bénéficiaire favori, etc.")
    public ResponseEntity<ApiResponse<UserDashboardStatsResponse>> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserDashboardStatsResponse stats = dashboardService.getStats(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Statistiques personnelles", stats));
    }

    @GetMapping("/evolution")
    @Operation(summary = "Évolution des transferts", description = "Retourne l'évolution mensuelle et annuelle des transferts de l'utilisateur connecté.")
    public ResponseEntity<ApiResponse<TransferEvolutionResponse>> getTransferEvolution(
            @AuthenticationPrincipal UserDetails userDetails) {
        TransferEvolutionResponse evolution = dashboardService.getTransferEvolution(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Évolution des transferts", evolution));
    }

    @GetMapping("/exchange-rates")
    @Operation(summary = "Taux de change en direct", description = "Retourne les taux de change actuels pour les paires de devises supportées.")
    public ResponseEntity<ApiResponse<List<LiveExchangeRateResponse>>> getLiveExchangeRates() {
        List<LiveExchangeRateResponse> rates = dashboardService.getLiveExchangeRates();
        return ResponseEntity.ok(ApiResponse.success("Taux de change en direct", rates));
    }

    @GetMapping("/exchange-rates/history/{from}/{to}")
    @Operation(summary = "Historique des taux de change", description = "Retourne l'historique des taux pour un couple de devises sur les 7 derniers jours.")
    public ResponseEntity<ApiResponse<List<LiveExchangeRateResponse.ExchangeRateHistoryPoint>>> getExchangeRateHistory(
            @PathVariable String from,
            @PathVariable String to) {
        List<LiveExchangeRateResponse.ExchangeRateHistoryPoint> history = dashboardService.getExchangeRateHistory(from,
                to);
        return ResponseEntity.ok(ApiResponse.success("Historique des taux de change", history));
    }
}
