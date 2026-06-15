package ibrahima.dickoya.sendme.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service récupérant les taux de change en temps réel depuis
 * l'API OpenExchangeRates (https://openexchangerates.org).
 *
 * Les taux sont mis en cache pendant 30 minutes pour éviter
 * de saturer l'API gratuite (1 000 req/mois).
 *
 * Une marge interne de 2% est appliquée sur le taux brut :
 * taux_final = taux_brut × (1 - 0.02)
 *
 * En cas d'indisponibilité de l'API, le cache renvoie
 * la dernière valeur connue (grace à Caffeine).
 */
@Service
@Slf4j
public class ExchangeRateService {

    /** URL de l'API OpenExchangeRates (plan gratuit : latest.json). */
    private static final String API_URL = "https://openexchangerates.org/api/latest.json?app_id={appId}";

    /** Marge interne de 2% appliquée sur le taux brut. */
    private static final BigDecimal MARGIN = new BigDecimal("0.98");

    /** Durée de vie du cache : 30 minutes. */
    private static final int CACHE_DURATION_MINUTES = 30;

    @Value("${app.openexchangerates.app-id}")
    private String appId;

    private final RestTemplate restTemplate = new RestTemplate();

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

    @PostConstruct
    public void init() {
        log.info("[EXCHANGE_RATE] Service initialise avec l'API OpenExchangeRates");
        log.info("[EXCHANGE_RATE] Marge interne : {}%",
                BigDecimal.ONE.subtract(MARGIN).multiply(new BigDecimal("100")));
        log.info("[EXCHANGE_RATE] Cache : {} minutes", CACHE_DURATION_MINUTES);
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

        // 1. Récupérer les taux bruts depuis le cache (ou appeler l'API)
        Map<String, Object> rates = ratesCache.get(from, key -> fetchRates(key));

        if (rates == null || !rates.containsKey(to)) {
            log.warn("[EXCHANGE_RATE] Taux non trouve pour {} -> {}. Fallback vers 1:1", from, to);
            return BigDecimal.ONE;
        }

        // 2. Extraire le taux brut
        BigDecimal rawRate = new BigDecimal(rates.get(to).toString());

        // 3. Appliquer la marge de 2% (on achète à 98% du taux du marché)
        BigDecimal finalRate = rawRate.multiply(MARGIN)
                .setScale(6, RoundingMode.HALF_UP);

        log.debug("[EXCHANGE_RATE] {} -> {} : brut={}, final={}", from, to, rawRate, finalRate);
        return finalRate;
    }

    /**
     * Appelle l'API OpenExchangeRates pour récupérer les derniers taux.
     *
     * @param baseCurrency devise de base (ex: "EUR")
     * @return Map des taux bruts (clé: code devise, valeur: taux)
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
            return relativeRates;

        } catch (Exception e) {
            log.error("[EXCHANGE_RATE] Erreur lors de l'appel API : {}", e.getMessage());
            return null; // Caffeine conservera l'ancienne valeur
        }
    }
}
