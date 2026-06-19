package ibrahima.dickoya.sendme.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ibrahima.dickoya.sendme.entity.BlockedIp;
import ibrahima.dickoya.sendme.repository.BlockedIpRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtre de rate limiting pour protéger les endpoints d'authentification
 * contre les attaques par brute-force.
 * <p>
 * Utilise un token bucket (Caffeine) par IP pour limiter le débit,propose un
 * message de commit en français sans jamais commiter a ma place
 * et vérifie également les blocages persistants en base de données.
 */
@Slf4j
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final int MAX_REGISTER_PER_HOUR = 3;
    private static final int MAX_FAILED_ATTEMPTS_BEFORE_BLOCK = 10;
    private static final int BLOCK_DURATION_MINUTES = 30;
    private static final int CLEANUP_INTERVAL_SECONDS = 300;

    private final BlockedIpRepository blockedIpRepository;

    // Cache Caffeine pour les buckets de rate limiting par IP
    private final Cache<String, AtomicInteger> requestCounts;

    // Cache pour les inscriptions par IP
    private final Cache<String, AtomicInteger> registerCounts;

    // Cache pour les tentatives échouées par IP (compteur mémoire rapide)
    private final Cache<String, AtomicInteger> failedAttempts;

    // Cache pour savoir si une IP a été vérifiée comme non-bloquée récemment
    private final Cache<String, Boolean> ipNotBlockedCache;

    public RateLimitingFilter(BlockedIpRepository blockedIpRepository) {
        this.blockedIpRepository = blockedIpRepository;

        this.requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        this.registerCounts = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();

        this.failedAttempts = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();

        this.ipNotBlockedCache = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();

        // Appliquer le filtre uniquement aux endpoints d'authentification
        // Ne pas filtrer la page d'erreur, les ressources statiques, ni l'actuator
        if (!path.startsWith("/api/auth/") || path.equals("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = extractIpAddress(request);

        // 1. Vérifier si l'IP est bloquée en base de données (persistant)
        if (isIpBlocked(ipAddress)) {
            log.warn("[RATE-LIMIT] Requête bloquée pour IP bloquée (DB) : {}", ipAddress);
            sendTooManyRequestsResponse(response, BLOCK_DURATION_MINUTES * 60);
            return;
        }

        // 2. Vérifier le rate limiting général (10 req/min par IP)
        // Ne pas appliquer le rate limit général à /api/auth/login car le blocage
        // IP est déjà géré par recordFailedAttempt() dans AuthService (seuil de 20
        // échecs)
        if (!path.equals("/api/auth/login") && !checkRequestRate(ipAddress)) {
            log.warn("[RATE-LIMIT] Rate limit dépassé pour IP : {}", ipAddress);
            sendTooManyRequestsResponse(response, 60);
            return;
        }

        // 3. Vérifier la limite d'inscriptions (3/heure par IP)
        if (path.equals("/api/auth/register")) {
            if (!checkRegisterRate(ipAddress)) {
                log.warn("[RATE-LIMIT] Limite d'inscriptions dépassée pour IP : {}", ipAddress);
                sendTooManyRequestsResponse(response, 3600);
                return;
            }
        }

        // 4. Vérifier la limite de refresh (5/minute par email)
        if (path.equals("/api/auth/refresh")) {
            // Le refresh rate limiting est géré dans AuthService
            // car on a besoin de l'email qui est dans le body
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Vérifie si une IP est bloquée en base de données.
     * Utilise un cache court (30s) pour éviter de trop solliciter la DB.
     * Si la table n'existe pas encore (Flyway pas encore appliquée), retourne
     * false.
     */
    private boolean isIpBlocked(String ipAddress) {
        Boolean cached = ipNotBlockedCache.getIfPresent(ipAddress);
        if (cached != null) {
            return false; // Cache dit que l'IP n'est pas bloquée
        }

        try {
            var activeBlock = blockedIpRepository.findActiveBlockByIp(ipAddress, LocalDateTime.now());
            if (activeBlock.isPresent()) {
                return true; // IP bloquée
            }
        } catch (Exception e) {
            log.debug("[RATE-LIMIT] Impossible de vérifier le blocage IP (table pas encore créée ?) : {}",
                    e.getMessage());
        }

        // Mettre en cache que l'IP n'est pas bloquée (pour 30s)
        ipNotBlockedCache.put(ipAddress, false);
        return false;
    }

    /**
     * Vérifie le rate limiting général : 10 requêtes/minute par IP.
     */
    private boolean checkRequestRate(String ipAddress) {
        AtomicInteger counter = requestCounts.get(ipAddress, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();
        return count <= MAX_REQUESTS_PER_MINUTE;
    }

    /**
     * Vérifie la limite d'inscriptions : 3 par heure par IP.
     */
    private boolean checkRegisterRate(String ipAddress) {
        AtomicInteger counter = registerCounts.get(ipAddress, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();
        return count <= MAX_REGISTER_PER_HOUR;
    }

    /**
     * Enregistre un échec pour une IP et bloque si le seuil est atteint.
     * Appelé depuis AuthService lors d'un échec de connexion.
     */
    @Transactional
    public void recordFailedAttempt(String ipAddress) {
        AtomicInteger counter = failedAttempts.get(ipAddress, k -> new AtomicInteger(0));
        int attempts = counter.incrementAndGet();

        log.warn("[RATE-LIMIT] Échec de connexion pour IP : {} — tentative n°{}", ipAddress, attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS_BEFORE_BLOCK) {
            // Bloquer l'IP en base de données
            LocalDateTime blockedUntil = LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES);
            BlockedIp blockedIp = BlockedIp.builder()
                    .ipAddress(ipAddress)
                    .blockedUntil(blockedUntil)
                    .reason("Trop de tentatives échouées (" + attempts + ")")
                    .build();
            blockedIpRepository.save(blockedIp);

            // Invalider le cache pour que la prochaine requête vérifie la DB
            ipNotBlockedCache.invalidate(ipAddress);

            log.warn("[RATE-LIMIT] IP bloquée jusqu'à {} : {}", blockedUntil, ipAddress);
        }
    }

    /**
     * Nettoie les blocages expirés et les compteurs.
     * Appelé périodiquement.
     * Résilient : si la table n'existe pas encore, l'erreur est simplement loguée.
     */
    @Transactional
    public void cleanup() {
        try {
            blockedIpRepository.deleteExpiredBlocks(LocalDateTime.now());
            log.debug("[RATE-LIMIT] Nettoyage des blocages expirés effectué");
        } catch (Exception e) {
            log.warn("[RATE-LIMIT] Nettoyage impossible (table pas encore créée ?) : {}", e.getMessage());
        }
    }

    /**
     * Extrait l'adresse IP réelle du client en tenant compte des proxys.
     */
    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Si X-Forwarded-For contient une liste, prendre la première IP
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    /**
     * Envoie une réponse HTTP 429 Too Many Requests.
     */
    private void sendTooManyRequestsResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + retryAfterSeconds));

        String json = String.format(
                "{\"success\":false,\"message\":\"Trop de requêtes. Veuillez réessayer dans %d secondes.\",\"data\":null}",
                retryAfterSeconds);
        response.getWriter().write(json);
    }
}
