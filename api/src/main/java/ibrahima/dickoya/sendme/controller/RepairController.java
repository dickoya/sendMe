package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de réparation pour corriger les problèmes
 * de migration Flyway et créer les tables manquantes.
 *
 * À SUPPRIMER après le déploiement en production.
 */
@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
@Tag(name = "Repair", description = "Endpoints de réparation (à supprimer en production)")
public class RepairController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/create-exchange-rates-table")
    @Operation(summary = "Crée la table exchange_rates si elle n'existe pas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createExchangeRatesTable() {
        Map<String, Object> result = new HashMap<>();

        try {
            // Vérifier si la table existe déjà
            jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'sendme_db' AND table_name = 'exchange_rates'",
                    Integer.class);

            // Supprimer l'entrée V10 de flyway_schema_history si elle existe
            jdbcTemplate.update("DELETE FROM flyway_schema_history WHERE version = '10'");

            // Créer la table
            jdbcTemplate.execute("""
                        CREATE TABLE IF NOT EXISTS exchange_rates (
                            id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                            base_currency VARCHAR(3)   NOT NULL,
                            target_currency VARCHAR(3) NOT NULL,
                            raw_rate    DECIMAL(18, 10) NOT NULL,
                            margin_rate DECIMAL(18, 10) NOT NULL,
                            margin_pct  DECIMAL(5, 2)  NOT NULL,
                            fetched_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            api_status  VARCHAR(20)    NOT NULL DEFAULT 'SUCCESS',
                            created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            INDEX idx_exchange_rates_base_target (base_currency, target_currency),
                            INDEX idx_exchange_rates_fetched_at (fetched_at)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);

            result.put("status", "OK");
            result.put("message", "Table exchange_rates créée avec succès");
            return ResponseEntity.ok(ApiResponse.success("Réparation réussie", result));

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("Réparation", result));
        }
    }
}
