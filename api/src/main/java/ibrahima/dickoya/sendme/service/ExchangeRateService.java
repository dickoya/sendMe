package ibrahima.dickoya.sendme.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ibrahima.dickoya.sendme.config.CurrencyConfig;
import ibrahima.dickoya.sendme.entity.ExchangeRate;
import ibrahima.dickoya.sendme.repository.ExchangeRateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Service récupérant les taux de change en temps réel depuis
 * l'API OpenExchangeRates (https://openexchangerates.org).
 *
 * Fonctionnalités :
 * - Cache Caffeine (durée configurable par l'admin) pour éviter de saturer
 * l'API gratuite
 * - Marge interne configurable par l'admin appliquée sur le taux brut
 * - Fallback base de données si l'API est indisponible
 * - Historique en base pour audit et conformité
 * - Logging détaillé pour chaque récupération de taux
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

    /** URL de l'API OpenExchangeRates (plan gratuit : latest.json). */
    private static final String API_URL = "https://openexchangerates.org/api/latest.json?app_id={appId}";

    /**
     * Marge interne appliquée sur le taux brut.
     * Modifiable à chaud par l'administrateur via le dashboard.
     * Valeur par défaut : 0.98 (soit 2% de marge).
     * volatile garantit la visibilité entre les threads.
     */
    private volatile BigDecimal margin = new BigDecimal("0.98");

    /** Pourcentage de marge affiché dans les logs. */
    private volatile BigDecimal marginPct = BigDecimal.ONE.subtract(margin)
            .multiply(new BigDecimal("100"));

    /**
     * Durée de vie du cache en minutes.
     * Modifiable à chaud par l'administrateur via le dashboard.
     * Valeur par défaut : 360 minutes (6h).
     * volatile garantit la visibilité entre les threads.
     */
    private volatile int cacheDurationMinutes = 360;

    @Value("${app.openexchangerates.app-id}")
    private String appId;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * Cache Caffeine : stocke les taux bruts (sans marge) par devise de base.
     * Clé : devise de base (ex: "EUR", "USD")
     * Valeur : Map des taux bruts (ex: {"XOF": 655.957, "GNF": 9500.00})
     *
     * La durée d'expiration est configurable par l'admin.
     * Tant que l'API répond, le cache est rafraîchi.
     * Si l'API est down, l'ancienne valeur est conservée.
     */
    private Cache<String, Map<String, Object>> ratesCache = Caffeine.newBuilder()
            .expireAfterWrite(cacheDurationMinutes, TimeUnit.MINUTES)
            .maximumSize(10)
            .build();

    private final Map<String, LocalDateTime> cacheRefreshTimestamps = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("[EXCHANGE_RATE] Service initialise avec l'API OpenExchangeRates");
        log.info("[EXCHANGE_RATE] Marge interne : {}%", marginPct);
        log.info("[EXCHANGE_RATE] Cache : {} minutes", cacheDurationMinutes);
        log.info("[EXCHANGE_RATE] Fallback BDD : active");
        log.info("[EXCHANGE_RATE] Historique audit : active");
    }

    /**
     * Récupère le taux de change entre deux devises, avec marge configurable.
     *
     * @param from devise source (ex: "EUR")
     * @param to   devise destination (ex: "XOF")
     * @return taux de change final avec marge appliquée
     */
    public BigDecimal getRate(String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return BigDecimal.ONE;
        }

        String baseCurrency = from.toUpperCase();
        String targetCurrency = to.toUpperCase();

        // 1. Récupérer les taux bruts depuis le cache (ou appeler l'API)
        Map<String, Object> rates = ratesCache.get(baseCurrency, key -> fetchRates(key));

        if (rates != null && rates.containsKey(targetCurrency)) {
            // API OK — taux récupéré normalement
            BigDecimal rawRate = new BigDecimal(rates.get(targetCurrency).toString());
            BigDecimal finalRate = rawRate.multiply(margin)
                    .setScale(6, RoundingMode.HALF_UP);

            log.info("[EXCHANGE_RATE] {} -> {} : brut={}, final={} (marge {}%)",
                    baseCurrency, targetCurrency, rawRate, finalRate, marginPct);

            // Stocker en base pour historique (asynchrone)
            saveRateToDatabase(baseCurrency, targetCurrency, rawRate, finalRate, "SUCCESS");

            return finalRate;
        }

        // API indisponible ou taux manquant → fallback base de données
        log.warn("[EXCHANGE_RATE] API indisponible pour {} -> {}. Fallback BDD...",
                baseCurrency, targetCurrency);

        BigDecimal fallbackRate = getFallbackRate(baseCurrency, targetCurrency);
        if (fallbackRate != null) {
            log.warn("[EXCHANGE_RATE] Fallback reussi : {} -> {} = {}",
                    baseCurrency, targetCurrency, fallbackRate);
            return fallbackRate;
        }

        // Aucun taux disponible → retour 1:1 (évite de bloquer les transferts)
        log.error("[EXCHANGE_RATE] AUCUN taux disponible pour {} -> {}. Retour 1:1",
                baseCurrency, targetCurrency);
        return BigDecimal.ONE;
    }

    /**
     * Récupère le dernier taux connu depuis la base de données.
     * Utilisé comme fallback quand l'API est indisponible.
     */
    private BigDecimal getFallbackRate(String baseCurrency, String targetCurrency) {
        try {
            var lastRate = exchangeRateRepository
                    .findFirstByBaseCurrencyAndTargetCurrencyOrderByFetchedAtDesc(
                            baseCurrency, targetCurrency);

            if (lastRate.isPresent()) {
                BigDecimal marginRate = lastRate.get().getMarginRate();
                log.info("[EXCHANGE_RATE] Fallback BDD : {} -> {} = {} (date: {})",
                        baseCurrency, targetCurrency, marginRate,
                        lastRate.get().getFetchedAt());
                return marginRate;
            }
        } catch (Exception e) {
            log.error("[EXCHANGE_RATE] Erreur fallback BDD : {}", e.getMessage());
        }
        return null;
    }

    /**
     * Stocke un taux de change dans la base de données pour l'historique.
     * Appelé à chaque fois qu'un taux est récupéré avec succès depuis l'API.
     */
    private void saveRateToDatabase(String baseCurrency, String targetCurrency,
            BigDecimal rawRate, BigDecimal marginRate,
            String apiStatus) {
        try {
            ExchangeRate rate = ExchangeRate.builder()
                    .baseCurrency(baseCurrency)
                    .targetCurrency(targetCurrency)
                    .rawRate(rawRate)
                    .marginRate(marginRate)
                    .marginPct(marginPct)
                    .fetchedAt(LocalDateTime.now())
                    .apiStatus(apiStatus)
                    .build();

            exchangeRateRepository.save(rate);
            log.debug("[EXCHANGE_RATE] Taux sauvegarde en BDD : {} -> {}", baseCurrency, targetCurrency);
        } catch (Exception e) {
            log.error("[EXCHANGE_RATE] Erreur sauvegarde BDD : {}", e.getMessage());
        }
    }

    /**
     * Appelle l'API OpenExchangeRates pour récupérer les derniers taux.
     *
     * @param baseCurrency devise de base (ex: "EUR")
     * @return Map des taux bruts (clé: code devise, valeur: taux), ou null si
     *         erreur
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchRates(String baseCurrency) {
        try {
            log.info("[EXCHANGE_RATE] Appel API OpenExchangeRates pour {}", baseCurrency);

            // L'API OpenExchangeRates utilise USD comme base par défaut.
            // On récupère tout, puis on calcule le taux relatif.
            var response = restTemplate.getForObject(API_URL, Map.class, appId);

            if (response == null || !response.containsKey("rates")) {
                log.error("[EXCHANGE_RATE] Reponse API invalide : {}", response);
                return null;
            }

            Map<String, Object> usdRates = (Map<String, Object>) response.get("rates");

            // Si la devise de base est USD, on retourne directement
            if ("USD".equalsIgnoreCase(baseCurrency)) {
                cacheRefreshTimestamps.put(baseCurrency.toUpperCase(), LocalDateTime.now());
                log.info("[EXCHANGE_RATE] {} taux recuperes depuis USD", usdRates.size());
                return usdRates;
            }

            // Sinon, on convertit : rate(EUR->XOF) = rate(USD->XOF) / rate(USD->EUR)
            Object baseToUsdObj = usdRates.get(baseCurrency.toUpperCase());
            if (baseToUsdObj == null) {
                log.error("[EXCHANGE_RATE] Devise de base {} non trouvee dans la reponse", baseCurrency);
                return null;
            }

            BigDecimal baseToUsd = new BigDecimal(baseToUsdObj.toString());

            // Calculer les taux relatifs à la devise de base
            java.util.HashMap<String, Object> relativeRates = new java.util.HashMap<>();
            for (var entry : usdRates.entrySet()) {
                String currency = entry.getKey();
                BigDecimal usdRate = new BigDecimal(entry.getValue().toString());
                BigDecimal relativeRate = usdRate.divide(baseToUsd, 10, RoundingMode.HALF_UP);
                relativeRates.put(currency, relativeRate);
            }

            log.info("[EXCHANGE_RATE] {} taux recuperes depuis USD/{}", relativeRates.size(), baseCurrency);
            cacheRefreshTimestamps.put(baseCurrency.toUpperCase(), LocalDateTime.now());
            return relativeRates;

        } catch (Exception e) {
            log.error("[EXCHANGE_RATE] Erreur lors de l'appel API : {}", e.getMessage());
            return null; // Caffeine conservera l'ancienne valeur
        }
    }

    // Méthodes de configuration dynamique (pour l'admin)

    /**
     * Met à jour la durée de vie du cache.
     * Le cache est INVALIDÉ IMMÉDIATEMENT pour forcer un rafraîchissement
     * à la prochaine demande de taux.
     *
     * @param minutes nouvelle durée en minutes
     */
    public void updateCacheDuration(int minutes) {
        if (minutes < 1) {
            throw new IllegalArgumentException("La durée du cache doit être d'au moins 1 minute");
        }
        this.cacheDurationMinutes = minutes;
        // Invalider le cache immédiatement pour appliquer la nouvelle durée
        ratesCache.invalidateAll();
        // Recréer le cache avec la nouvelle durée
        ratesCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheDurationMinutes, TimeUnit.MINUTES)
                .maximumSize(10)
                .build();
        log.info("[EXCHANGE_RATE] Duree du cache mise a jour : {} minutes (cache invalide)", minutes);
    }

    /**
     * Met à jour la marge interne appliquée sur le taux brut.
     *
     * @param marginPercent la marge en pourcentage (ex: 2.0 pour 2%)
     */
    public void updateMargin(BigDecimal marginPercent) {
        if (marginPercent.compareTo(BigDecimal.ZERO) < 0 || marginPercent.compareTo(new BigDecimal("50")) > 0) {
            throw new IllegalArgumentException("La marge doit être entre 0% et 50%");
        }
        // Convertir le pourcentage en facteur multiplicateur (2% → 0.98)
        BigDecimal factor = BigDecimal.ONE.subtract(
                marginPercent.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
        this.margin = factor;
        this.marginPct = marginPercent;
        log.info("[EXCHANGE_RATE] Marge mise a jour : {}% (facteur: {})", marginPercent, factor);
    }

    /**
     * Retourne la durée actuelle du cache en minutes.
     */
    public int getCacheDurationMinutes() {
        return cacheDurationMinutes;
    }

    /**
     * Retourne le facteur de marge actuel (ex: 0.98 pour 2%).
     */
    public BigDecimal getMargin() {
        return margin;
    }

    // Méthodes publiques pour le debug et le dashboard admin

    /**
     * Récupère le taux brut (sans marge) pour un couple de devises.
     * Utilisé par l'endpoint debug.
     */
    public BigDecimal getRawRate(String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return BigDecimal.ONE;
        }

        Map<String, Object> rates = ratesCache.get(from.toUpperCase(),
                key -> fetchRates(key));

        if (rates != null && rates.containsKey(to.toUpperCase())) {
            return new BigDecimal(rates.get(to.toUpperCase()).toString());
        }

        // Fallback BDD pour le taux brut
        try {
            var lastRate = exchangeRateRepository
                    .findFirstByBaseCurrencyAndTargetCurrencyOrderByFetchedAtDesc(
                            from.toUpperCase(), to.toUpperCase());
            if (lastRate.isPresent()) {
                return lastRate.get().getRawRate();
            }
        } catch (Exception e) {
            log.error("[EXCHANGE_RATE] Erreur fallback BDD pour rawRate : {}", e.getMessage());
        }

        return null;
    }

    /**
     * Retourne le taux final (avec marge) pour un couple de devises.
     * Utilisé par l'endpoint debug.
     */
    public BigDecimal getMarginRate(String from, String to) {
        if (from.equalsIgnoreCase(to)) {
            return BigDecimal.ONE;
        }

        BigDecimal rawRate = getRawRate(from, to);
        if (rawRate != null) {
            return rawRate.multiply(margin)
                    .setScale(6, RoundingMode.HALF_UP);
        }
        return null;
    }

    /**
     * Retourne le timestamp du dernier rafraîchissement du cache
     * pour une devise de base donnée.
     */
    public String getCacheTimestamp(String baseCurrency) {
        String key = baseCurrency.toUpperCase();
        if (ratesCache.getIfPresent(key) != null && cacheRefreshTimestamps.containsKey(key)) {
            return cacheRefreshTimestamps.get(key).toString();
        }
        return "Aucun cache disponible";
    }

    /**
     * Retourne la marge appliquée en pourcentage (ex: 2.00).
     */
    public BigDecimal getMarginPercentage() {
        return marginPct;
    }

    /**
     * Récupère les taux debug pour toutes les paires de devises supportées.
     * Utilisé par l'endpoint GET /api/debug/rates.
     *
     * @return Liste de maps contenant pour chaque paire :
     *         from, to, rawRate, marginRate, cacheTimestamp
     */
    public java.util.List<java.util.Map<String, Object>> getAllDebugRates() {
        String[] currencies = CurrencyConfig.getAllCurrencyCodes();
        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();

        for (String from : currencies) {
            for (String to : currencies) {
                if (from.equals(to))
                    continue;

                java.util.Map<String, Object> entry = new java.util.HashMap<>();
                entry.put("from", from);
                entry.put("to", to);
                entry.put("rawRate", getRawRate(from, to));
                entry.put("marginRate", getMarginRate(from, to));
                entry.put("cacheTimestamp", getCacheTimestamp(from));
                results.add(entry);
            }
        }

        return results;
    }

}
