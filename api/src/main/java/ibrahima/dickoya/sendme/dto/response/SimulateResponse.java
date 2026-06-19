package ibrahima.dickoya.sendme.dto.response;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO de réponse pour la simulation d'un transfert.
 *
 * Retourné au frontend pour afficher le récapitulatif complet
 * avant que l'utilisateur confirme et paie par carte bancaire.
 *
 * Exemple affiché à l'utilisateur :
 * - Montant envoyé : 200.00 EUR
 * - Frais (1.5%) : 3.00 EUR
 * - Total débité : 203.00 EUR
 * - Taux de change : 1 EUR = 655.957 XOF
 * - Montant reçu : 128 745.14 XOF
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulateResponse {

    /** Montant initial saisi par l'utilisateur. */
    private BigDecimal amountSent;

    /** Devise d'envoi. */
    private String currencyFrom;

    /**
     * Frais de la plateforme calculés sur le montant envoyé.
     * Taux actuel : 1.5% (paramétrable par l'admin).
     */
    private BigDecimal fees;

    /** Taux de la commission en pourcentage. Exemple : 1.5 */
    private BigDecimal feeRate;

    /** Montant total débité sur la carte = montant envoyé + frais. */
    private BigDecimal totalDebited;

    /** Taux de change appliqué entre currencyFrom et currencyTo. */
    private BigDecimal exchangeRate;

    /** Devise de réception du bénéficiaire. */
    private String currencyTo;

    /** Montant exact que recevra le bénéficiaire après conversion. */
    private BigDecimal amountReceived;

    /**
     * Montant de la réduction appliquée (parrainage).
     * 0 si aucun coupon valide n'est disponible.
     */
    private BigDecimal discount;
}
