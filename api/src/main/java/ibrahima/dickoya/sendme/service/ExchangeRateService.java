package ibrahima.dickoya.sendme.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
 * - Cache Caffeine de 30 minutes pour éviter de saturer l'API gratuite
 * - Marge interne de 2% appliquée sur le taux brut
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

    /** Marge interne de 2% appliquée sur le taux brut. */
    private static final BigDecimal MARGIN = new BigDecimal("0.98");

    /** Pourcentage de marge affiché dans les logs. */
    private static final BigDecimal MARGIN_PCT = BigDecimal.ONE.subtract(MARGIN)
            .multiply(new BigDecimal("100"));

    /** Durée de vie du cache : 30 minutes. */
    private static final int CACHE_DURATION_MINUTES = 30;

    @Value("${app.openexchangerates.app-id}")
    private String appId;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * Cache Caffeine : stocke les taux bruts (sans marge) par devise de base.
     * Clé : devise de base (ex: "EUR", "USD")
     * Valeur : Map des taux bruts (ex: {"XOF": 655.957, "GNF": 9500.00})
     *
     * Expire 30 minutes après écriture.
     * Tant que l'API répond, le cache est rafraîchi.
     * Si l'API est down, l'ancienne valeur est conservée.
     */
    private final Cache<String, Map<String, Object>> ratesCache = Caffeine.newBuilder()
            .expireAfterWrite(CACHE_DURATION_MINUTES, TimeUnit.MINUTES)
            .maximumSize(10)
            .build();

    private final Map<String, LocalDateTime> cacheRefreshTimestamps = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("[EXCHANGE_RATE] Service initialise avec l'API OpenExchangeRates");
        log.info("[EXCHANGE_RATE] Marge interne : {}%", MARGIN_PCT);
        log.info("[EXCHANGE_RATE] Cache : {} minutes", CACHE_DURATION_MINUTES);
        log.info("[EXCHANGE_RATE] Fallback BDD : active");
        log.info("[EXCHANGE_RATE] Historique audit : active");
    }

    /**
     * Récupère le taux de change entre deux devises, avec marge de 2%.
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
            // ✅ API OK — taux récupéré normalement
            BigDecimal rawRate = new BigDecimal(rates.get(targetCurrency).toString());
            BigDecimal finalRate = rawRate.multiply(MARGIN)
                    .setScale(6, RoundingMode.HALF_UP);

            log.info("[EXCHANGE_RATE] {} -> {} : brut={}, final={} (marge {}%)",
                    baseCurrency, targetCurrency, rawRate, finalRate, MARGIN_PCT);

            // Stocker en base pour historique (asynchrone)
            saveRateToDatabase(baseCurrency, targetCurrency, rawRate, finalRate, "SUCCESS");

            return finalRate;
        }

        // ⚠️ API indisponible ou taux manquant → fallback base de données
        log.warn("[EXCHANGE_RATE] API indisponible pour {} -> {}. Fallback BDD...",
                baseCurrency, targetCurrency);

        BigDecimal fallbackRate = getFallbackRate(baseCurrency, targetCurrency);
        if (fallbackRate != null) {
            log.warn("[EXCHANGE_RATE] Fallback reussi : {} -> {} = {}",
                    baseCurrency, targetCurrency, fallbackRate);
            return fallbackRate;
        }

        // 🚨 Aucun taux disponible → retour 1:1 (évite de bloquer les transferts)
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
                    .marginPct(MARGIN_PCT)
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

    // ────────────────────────────────────────────────────────────────
    // Méthodes publiques pour le debug et le dashboard admin
    // ────────────────────────────────────────────────────────────────

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
            return rawRate.multiply(MARGIN)
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
     * Retourne la marge appliquée (ex: 2.00).
     */
    public BigDecimal getMarginPercentage() {
        return MARGIN_PCT;
    }

    /**
     * Récupère les taux debug pour toutes les paires de devises supportées.
     * Utilisé par l'endpoint GET /api/debug/rates.
     *
     * @return Liste de maps contenant pour chaque paire :
     *         from, to, rawRate, marginRate, cacheTimestamp
     */
    public java.util.List<java.util.Map<String, Object>> getAllDebugRates() {
        String[] currencies = { "EUR", "USD", "XOF", "GNF" };
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
