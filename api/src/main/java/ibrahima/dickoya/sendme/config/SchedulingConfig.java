package ibrahima.dickoya.sendme.config;

import ibrahima.dickoya.sendme.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration de la planification des tâches.
 * Active les tâches périodiques (@Scheduled) dans l'application.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {

    private final RateLimitingFilter rateLimitingFilter;

    /**
     * Nettoie les blocages IP expirés toutes les 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredIpBlocks() {
        log.debug("[SCHEDULER] Nettoyage des blocages IP expirés...");
        rateLimitingFilter.cleanup();
    }
}
