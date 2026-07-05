package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsable du rate limiting des requêtes de rafraîchissement de
 * token.
 * <p>
 * Limite à 5 rafraîchissements par minute par adresse email pour prévenir les
 * abus.
 */
@Service
@Slf4j
public class RefreshTokenService {

    private static final int MAX_REFRESH_PER_MINUTE = 5;

    private final Map<String, Integer> refreshCounts = new ConcurrentHashMap<>();
    private final Map<String, Instant> refreshWindowStart = new ConcurrentHashMap<>();

    /**
     * Vérifie que le taux de rafraîchissement n'a pas été dépassé pour un email
     * donné.
     * <p>
     * Si la fenêtre d'une minute est expirée, elle est réinitialisée.
     * Si le nombre de requêtes dans la fenêtre dépasse le seuil, une exception
     * {@link RateLimitExceededException} est levée.
     *
     * @param email l'adresse email pour laquelle vérifier le rate limit
     * @throws RateLimitExceededException si le seuil est dépassé
     */
    public void checkRateLimit(String email) {
        Instant now = Instant.now();
        Instant windowStart = refreshWindowStart.get(email);

        if (windowStart == null || Duration.between(windowStart, now).getSeconds() >= 60) {
            refreshWindowStart.put(email, now);
            refreshCounts.put(email, 1);
            return;
        }

        int count = refreshCounts.merge(email, 1, Integer::sum);

        if (count > MAX_REFRESH_PER_MINUTE) {
            long retryAfter = 60 - Duration.between(windowStart, now).getSeconds();
            log.warn("[AUTH] Rate limit de refresh dépassé pour : {} — {} tentatives en 1 min", email, count);
            throw new RateLimitExceededException(
                    "Trop de tentatives de rafraîchissement. Veuillez réessayer dans " + retryAfter + " secondes.",
                    retryAfter);
        }
    }

    /**
     * Réinitialise le compteur de rafraîchissement pour un email.
     *
     * @param email l'adresse email à réinitialiser
     */
    public void resetRateLimit(String email) {
        refreshCounts.remove(email);
        refreshWindowStart.remove(email);
    }
}
