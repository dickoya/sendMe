package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.exception.RateLimitExceededException;
import ibrahima.dickoya.sendme.security.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsable de la gestion du blocage par IP avec exponential backoff.
 * <p>
 * Après 3 échecs consécutifs, un délai exponentiel est imposé (2ⁿ secondes,
 * max 8s).
 * Délègue également le blocage IP persistant à {@link RateLimitingFilter}
 * lorsque le seuil de 20 échecs est atteint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IpBlockingService {

    private static final int BACKOFF_THRESHOLD = 3;

    private final Map<String, Integer> ipFailedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> ipLastFailedTime = new ConcurrentHashMap<>();
    private final RateLimitingFilter rateLimitingFilter;

    /**
     * Vérifie et applique l'exponential backoff pour une adresse IP.
     * <p>
     * Si le nombre d'échecs consécutifs dépasse le seuil (3), un délai
     * exponentiel est calculé. Si le temps écoulé depuis le dernier échec
     * est inférieur à ce délai, une exception {@link RateLimitExceededException}
     * est levée.
     *
     * @param ipAddress l'adresse IP à vérifier
     * @throws RateLimitExceededException si le temps d'attente n'est pas écoulé
     */
    public void applyBackoff(String ipAddress) {
        Integer failedCount = ipFailedAttempts.get(ipAddress);
        if (failedCount == null || failedCount < BACKOFF_THRESHOLD) {
            return;
        }

        int delaySeconds = Math.min(
                (int) Math.pow(2, failedCount - BACKOFF_THRESHOLD),
                8);

        Instant lastFailed = ipLastFailedTime.get(ipAddress);
        if (lastFailed != null) {
            Instant now = Instant.now();
            long elapsedSeconds = Duration.between(lastFailed, now).getSeconds();
            if (elapsedSeconds < delaySeconds) {
                long retryAfterSeconds = delaySeconds - elapsedSeconds;
                log.warn("[BACKOFF] IP {} : trop de tentatives. Reessayez dans {} secondes", ipAddress,
                        retryAfterSeconds);
                throw new RateLimitExceededException(
                        "Trop de tentatives de connexion. Veuillez réessayer dans " + retryAfterSeconds + " secondes.",
                        retryAfterSeconds);
            }
        }
    }

    /**
     * Enregistre un échec de connexion pour une adresse IP.
     * <p>
     * Incrémente le compteur d'échecs et enregistre l'instant du dernier échec.
     * Délègue également au {@link RateLimitingFilter} pour le blocage IP
     * persistant en base de données.
     *
     * @param ipAddress l'adresse IP à enregistrer
     */
    public void recordFailure(String ipAddress) {
        ipFailedAttempts.merge(ipAddress, 1, Integer::sum);
        ipLastFailedTime.put(ipAddress, Instant.now());
        rateLimitingFilter.recordFailedAttempt(ipAddress);
    }

    /**
     * Réinitialise les compteurs d'échecs pour une adresse IP
     * (appelé après une connexion réussie).
     *
     * @param ipAddress l'adresse IP à réinitialiser
     */
    public void resetForIp(String ipAddress) {
        ipFailedAttempts.remove(ipAddress);
        ipLastFailedTime.remove(ipAddress);
    }
}
