package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO pour initier un nouveau transfert d'argent avec paiement Stripe.
 *
 * Flux de paiement sécurisé :
 * 1. Le frontend (React + Stripe.js) collecte les données de carte
 * 2. Stripe.js tokenise la carte → génère un paymentMethodId
 * 3. Le frontend envoie ce TransferRequest avec le paymentMethodId
 * 4. Le backend utilise ce paymentMethodId pour débiter via l'API Stripe
 *
 * IMPORTANT : Les données de carte bancaire ne transitent JAMAIS
 * par notre backend (conformité PCI DSS Level 1).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferRequest {

    /**
     * Montant à envoyer.
     * Doit être strictement positif.
     */
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1.00", message = "Le montant minimum est de 1.00")
    @Digits(integer = 10, fraction = 2, message = "Format de montant invalide")
    private BigDecimal amount;

    /**
     * Devise d'envoi (code ISO 4217).
     * Exemples : EUR, USD, GBP
     */
    @NotBlank(message = "La devise est obligatoire")
    @Size(min = 3, max = 5, message = "Code devise invalide")
    private String currency;

    /**
     * Identifiant du bénéficiaire dans le carnet d'adresses.
     * Doit appartenir à l'utilisateur connecté.
     */
    @NotNull(message = "L'identifiant du beneficiaire est obligatoire")
    private Long beneficiaryId;

    /**
     * Identifiant du moyen de paiement généré par Stripe.js.
     *
     * Généré côté frontend avec :
     * const {paymentMethod} = await stripe.createPaymentMethod({
     *     type: 'card',
     *     card: cardElement,
     * });
     * → paymentMethod.id = "pm_XXXXXXXXXXXX"
     *
     * Ce token est à usage unique et expire rapidement.
     * Il permet au backend de débiter la carte sans jamais
     * manipuler les données de carte brutes.
     */
    @NotBlank(message = "L'identifiant de paiement Stripe est obligatoire")
    private String paymentMethodId;

    /**
     * Clé d'idempotence pour éviter les doubles débits (RG-06).
     * Générée côté frontend avec UUID v4.
     * Exemple : "550e8400-e29b-41d4-a716-446655440000"
     */
    @NotBlank(message = "La cle d'idempotence est obligatoire")
    private String idempotencyKey;
}