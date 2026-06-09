package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO pour initier un nouveau transfert d'argent.
 *
 * Contient toutes les informations nécessaires pour créer une transaction :
 * - Le montant et la devise d'envoi
 * - L'identifiant du bénéficiaire déjà sauvegardé
 * - La clé d'idempotence pour éviter les doublons (RG-06)
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferRequest {

    /**
     * Montant à envoyer.
     * Doit être strictement positif et avoir au maximum 2 décimales.
     * Exemple : 200.00
     */
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1.00", message = "Le montant minimum est de 1.00")
    @Digits(integer = 10, fraction = 2, message = "Format de montant invalide")
    private BigDecimal amount;

    /**
     * Devise d'envoi (code ISO 4217).
     * Exemples : EUR, USD, GBP, XOF
     */
    @NotBlank(message = "La devise est obligatoire")
    @Size(min = 3, max = 5, message = "Code devise invalide")
    private String currency;

    /**
     * Identifiant du bénéficiaire sauvegardé dans le carnet d'adresses.
     * Doit appartenir à l'utilisateur connecté.
     */
    @NotNull(message = "L'identifiant du beneficiaire est obligatoire")
    private Long beneficiaryId;

    /**
     * Clé d'idempotence générée côté frontend (UUID v4).
     * Garantit qu'un même transfert ne peut pas être exécuté deux fois
     * même en cas de double clic ou de perte réseau (RG-06).
     * Exemple : "550e8400-e29b-41d4-a716-446655440000"
     */
    @NotBlank(message = "La cle d'idempotence est obligatoire")
    private String idempotencyKey;
}