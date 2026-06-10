package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.SimulateRequest;
import ibrahima.dickoya.sendme.dto.response.SimulateResponse;
import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Service gérant la simulation des frais et du taux de change.
 *
 * Permet à l'utilisateur de voir le récapitulatif complet AVANT
 * de confirmer et débiter sa carte bancaire.
 *
 * En production, les taux de change seraient récupérés en temps réel
 * via une API externe comme ExchangeRate-API ou Frankfurter.
 * Ici, ils sont simulés avec des valeurs fixes réalistes.
 *
 * Taux de commission : 1.5% (paramétrable via application.properties)
 */
@Service
@Slf4j
public class SimulatorService {

    /**
     * Taux de commission de la plateforme : 1.5%.
     * En production, ce taux serait lu depuis la base de données
     * et modifiable par l'administrateur.
     */
    private static final BigDecimal FEE_RATE = new BigDecimal("0.015");

    /**
     * Table des taux de change simulés.
     * Clé : "EUR_XOF" (devise source + devise destination)
     * Valeur : taux de change
     *
     * En production, ces taux seraient mis à jour en temps réel
     * depuis une API externe (ExchangeRate-API, Open Exchange Rates, etc.)
     */
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    static {
        // Initialisation des taux de change simulés
        // EUR vers devises africaines
        EXCHANGE_RATES.put("EUR_XOF", new BigDecimal("655.957"));  // Franc CFA (UEMOA)
        EXCHANGE_RATES.put("EUR_XAF", new BigDecimal("655.957"));  // Franc CFA (CEMAC)
        EXCHANGE_RATES.put("EUR_GNF", new BigDecimal("9500.00"));  // Franc Guinéen
        EXCHANGE_RATES.put("EUR_MAD", new BigDecimal("10.85"));    // Dirham Marocain
        EXCHANGE_RATES.put("EUR_TND", new BigDecimal("3.35"));     // Dinar Tunisien
        EXCHANGE_RATES.put("EUR_DZD", new BigDecimal("146.00"));   // Dinar Algérien
        EXCHANGE_RATES.put("EUR_USD", new BigDecimal("1.08"));     // Dollar US
        EXCHANGE_RATES.put("EUR_GBP", new BigDecimal("0.86"));     // Livre Sterling

        // USD vers devises africaines
        EXCHANGE_RATES.put("USD_XOF", new BigDecimal("607.37"));
        EXCHANGE_RATES.put("USD_GNF", new BigDecimal("8796.30"));

        // Taux identique (même devise)
        EXCHANGE_RATES.put("EUR_EUR", BigDecimal.ONE);
        EXCHANGE_RATES.put("USD_USD", BigDecimal.ONE);
        EXCHANGE_RATES.put("XOF_XOF", BigDecimal.ONE);
    }

    /**
     * Calcule la simulation complète d'un transfert.
     *
     * Formule :
     * - frais          = montant × 1.5%
     * - total débité   = montant + frais
     * - montant reçu   = montant × taux de change
     *
     * @param request contient le montant, les devises et le canal
     * @return SimulateResponse avec tous les détails calculés
     */
    public SimulateResponse simulate(SimulateRequest request) {

        BigDecimal amount = request.getAmount();
        String currencyFrom = request.getCurrencyFrom().toUpperCase();
        String currencyTo = request.getCurrencyTo().toUpperCase();

        // 1. Calculer les frais de la plateforme (1.5% du montant)
        BigDecimal fees = amount.multiply(FEE_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Calculer le total débité sur la carte (montant + frais)
        BigDecimal totalDebited = amount.add(fees)
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Récupérer le taux de change
        String rateKey = currencyFrom + "_" + currencyTo;
        BigDecimal exchangeRate = EXCHANGE_RATES.getOrDefault(
                rateKey, BigDecimal.ONE); // taux 1:1 par défaut si non trouvé

        // 4. Calculer le montant reçu par le bénéficiaire
        BigDecimal amountReceived = amount.multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Simulation : {} {} → {} {} (frais: {}, taux: {})",
                amount, currencyFrom, amountReceived, currencyTo, fees, exchangeRate);

        // 5. Construire et retourner la réponse
        return SimulateResponse.builder()
                .amountSent(amount)
                .currencyFrom(currencyFrom)
                .fees(fees)
                .feeRate(FEE_RATE.multiply(new BigDecimal("100"))) // afficher en %
                .totalDebited(totalDebited)
                .exchangeRate(exchangeRate)
                .currencyTo(currencyTo)
                .amountReceived(amountReceived)
                .build();
    }
    /**
     * Taux de commission modifiable par l'administrateur.
     * volatile garantit la visibilité entre les threads.
     * Initialisé à 1.5% et modifiable à chaud sans redémarrage.
     */
    private volatile BigDecimal feeRate = new BigDecimal("0.015");

    /**
     * Met à jour le taux de commission de la plateforme.
     * Appelé par l'AdminService quand l'admin change le tarif.
     * Le nouveau taux est appliqué immédiatement à tous
     * les nouveaux transferts.
     *
     * @param newRate le nouveau taux en pourcentage (ex: 2.0 pour 2%)
     */
    public void updateFeeRate(BigDecimal newRate) {
        // Convertir le pourcentage en décimal (2.0% → 0.02)
        this.feeRate = newRate.divide(new BigDecimal("100"));
        log.info("[SIMULATOR] Taux de commission mis a jour : {}%", newRate);
    }
}