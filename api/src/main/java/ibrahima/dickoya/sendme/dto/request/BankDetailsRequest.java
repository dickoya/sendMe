package ibrahima.dickoya.sendme.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO pour les détails d'un compte bancaire.
 * Utilisé uniquement quand le canal choisi est BANK_TRANSFER (RG-02).
 * Contient les informations nécessaires pour un virement SEPA/SWIFT.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankDetailsRequest {

    /**
     * Nom de la banque destinataire.
     * Exemple : "Société Générale", "BNP Paribas"
     */
    @NotBlank(message = "Le nom de la banque est obligatoire")
    private String bankName;

    /**
     * IBAN (International Bank Account Number).
     * Format international : jusqu'à 34 caractères alphanumériques.
     * Exemple : FR7630006000011234567890189
     */
    @NotBlank(message = "L'IBAN est obligatoire")
    @Size(max = 34, message = "L'IBAN ne peut pas depasser 34 caracteres")
    private String iban;

    /**
     * BIC/SWIFT (Bank Identifier Code).
     * Identifiant unique de la banque au niveau international.
     * Format : 8 ou 11 caractères.
     * Exemple : BNPAFRPPXXX
     */
    @NotBlank(message = "Le BIC est obligatoire")
    @Size(max = 11, message = "Le BIC ne peut pas depasser 11 caracteres")
    private String bic;
}