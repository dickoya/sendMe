package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.SimulateRequest;
import ibrahima.dickoya.sendme.dto.response.SimulateResponse;
import ibrahima.dickoya.sendme.entity.ReferralCoupon;
import ibrahima.dickoya.sendme.enums.ChannelType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Service gérant la simulation des frais et du taux de change.
 *
 * Permet à l'utilisateur de voir le récapitulatif complet AVANT
 * de confirmer et débiter sa carte bancaire.
 *
 * Les taux de change sont récupérés en temps réel depuis
 * l'API OpenExchangeRates via {@link ExchangeRateService},
 * avec un cache de 30 minutes et une marge interne de 2%.
 *
 * Taux de commission : 1.5% (paramétrable via application.properties)
 *
 * Réduction parrainage : si l'utilisateur a un coupon valide pour la devise,
 * le total débité est réduit du montant du coupon.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SimulatorService {

        private final ExchangeRateService exchangeRateService;
        private final ReferralService referralService;

        /**
         * Taux de commission modifiable par l'administrateur.
         * volatile garantit la visibilité entre les threads.
         * Initialisé à 1.5% et modifiable à chaud sans redémarrage.
         */
        private volatile BigDecimal feeRate = new BigDecimal("0.015");

        /**
         * Calcule la simulation complète d'un transfert.
         *
         * Formule :
         * - frais = montant × 1.5%
         * - total débité = montant + frais
         * - montant reçu = montant × taux de change (via API OpenExchangeRates + marge
         * 2%)
         *
         * @param request contient le montant, les devises et le canal
         * @return SimulateResponse avec tous les détails calculés
         */
        public SimulateResponse simulate(SimulateRequest request) {
                return simulate(request, null);
        }

        /**
         * Calcule la simulation complète d'un transfert, avec application
         * d'un éventuel coupon de réduction (parrainage).
         *
         * @param request contient le montant, les devises et le canal
         * @param userId  ID de l'utilisateur (optionnel, pour appliquer la réduction)
         * @return SimulateResponse avec tous les détails calculés
         */
        public SimulateResponse simulate(SimulateRequest request, Long userId) {

                BigDecimal amount = request.getAmount();
                String currencyFrom = request.getCurrencyFrom().toUpperCase();
                String currencyTo = request.getCurrencyTo().toUpperCase();

                // 1. Calculer les frais de la plateforme (1.5% du montant par défaut,
                // modifiable)
                BigDecimal fees = amount.multiply(feeRate)
                                .setScale(2, RoundingMode.HALF_UP);

                // 2. Calculer le total débité sur la carte (montant + frais)
                BigDecimal totalDebited = amount.add(fees)
                                .setScale(2, RoundingMode.HALF_UP);

                // 3. Récupérer le taux de change depuis l'API OpenExchangeRates
                // (avec cache 30 min et marge interne de 2%)
                BigDecimal exchangeRate = exchangeRateService.getRate(currencyFrom, currencyTo);

                // 4. Calculer le montant reçu par le bénéficiaire
                BigDecimal amountReceived = amount.multiply(exchangeRate)
                                .setScale(2, RoundingMode.HALF_UP);

                // 5. Appliquer la réduction de parrainage si l'utilisateur a un coupon valide
                // ET si applyDiscount n'est pas explicitement désactivé
                BigDecimal discount = BigDecimal.ZERO;
                boolean shouldApplyDiscount = userId != null
                                && (request.getApplyDiscount() == null || request.getApplyDiscount());
                if (shouldApplyDiscount) {
                        Optional<ReferralCoupon> coupon = referralService.getValidCoupon(userId, currencyFrom);
                        if (coupon.isPresent()) {
                                discount = coupon.get().getDiscountAmount();
                                // La réduction s'applique sur le total débité (ne peut pas dépasser le total)
                                if (discount.compareTo(totalDebited) > 0) {
                                        discount = totalDebited;
                                }
                                log.info("Réduction de {} {} appliquée pour l'utilisateur {}",
                                                discount, currencyFrom, userId);
                        }
                }

                BigDecimal totalAfterDiscount = totalDebited.subtract(discount)
                                .setScale(2, RoundingMode.HALF_UP);

                log.info("Simulation : {} {} → {} {} (frais: {}, taux: {}, réduction: {})",
                                amount, currencyFrom, amountReceived, currencyTo, fees, exchangeRate, discount);

                // 6. Construire et retourner la réponse
                return SimulateResponse.builder()
                                .amountSent(amount)
                                .currencyFrom(currencyFrom)
                                .fees(fees)
                                .feeRate(feeRate.multiply(new BigDecimal("100"))) // afficher en %
                                .totalDebited(totalAfterDiscount)
                                .discount(discount)
                                .exchangeRate(exchangeRate)
                                .currencyTo(currencyTo)
                                .amountReceived(amountReceived)
                                .build();
        }

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

        /**
         * Retourne le taux de commission actuel sous forme décimale.
         * Exemple : si le taux est 1.5%, retourne 0.015
         *
         * Utilisé par AdminService.updateFeeRate() pour comparer
         * l'ancien et le nouveau taux dans l'email de notification.
         *
         * @return BigDecimal le taux actuel sous forme décimale
         */
        public BigDecimal getCurrentFeeRate() {
                return this.feeRate;
        }
}