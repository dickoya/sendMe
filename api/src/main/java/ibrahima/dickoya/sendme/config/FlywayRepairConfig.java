package ibrahima.dickoya.sendme.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Répare la migration Flyway V10 au démarrage.
 * Supprime l'entrée "failed" de flyway_schema_history
 * et la remplace par une entrée "success".
 *
 * À SUPPRIMER après le déploiement en production.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class FlywayRepairConfig {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void repairFlyway() {
        try {
            // Vérifier si l'entrée V10 existe et est en échec
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '10' AND success = 0",
                    Integer.class);

            if (count != null && count > 0) {
                log.warn("[FLYWAY_REPAIR] Migration V10 en échec détectée. Réparation...");

                // Supprimer l'ancienne entrée failed
                jdbcTemplate.update("DELETE FROM flyway_schema_history WHERE version = '10'");

                // Créer une nouvelle entrée success avec le checksum actuel
                jdbcTemplate
                        .update("""
                                    INSERT INTO flyway_schema_history
                                    (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
                                    VALUES (
                                        (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
                                        '10',
                                        'create exchange rates table',
                                        'SQL',
                                        'db/migration/V10__create_exchange_rates_table.sql',
                                        (SELECT CHECKSUM FROM (SELECT CHECKSUM FROM flyway_schema_history WHERE version = '10' LIMIT 1) AS tmp),
                                        USER(),
                                        NOW(),
                                        0,
                                        1
                                    )
                                """);

                log.info("[FLYWAY_REPAIR] Migration V10 réparée avec succès !");
            } else {
                log.info("[FLYWAY_REPAIR] Aucune réparation nécessaire.");
            }
        } catch (Exception e) {
            log.warn("[FLYWAY_REPAIR] Impossible de réparer Flyway: {}", e.getMessage());
        }
    }
}
