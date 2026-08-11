package ibrahima.dickoya.sendme.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Endpoint de santé public.
 *
 * Utilisé par les moniteurs de disponibilité (UptimeRobot, Railway healthcheck,
 * cron, etc.) pour garder le conteneur éveillé et éviter le "cold start" de la
 * JVM sur Railway (qui peut prendre 15-21 s).
 *
 * L'endpoint est volontairement ultra-léger : il ne touche PAS la base de
 * données et répond en quelques millisecondes.
 *
 * Accessible sans token JWT : "/api/health" est déclaré dans PUBLIC_ROUTES
 * de SecurityConfig.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Santé", description = "Endpoint de contrôle de disponibilité (keep-alive)")
public class HealthController {

    @Operation(summary = "Vérifie que le service est en vie")
    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "sendme-api",
                "timestamp", OffsetDateTime.now().toString()
        );
    }
}
