package ibrahima.dickoya.sendme.dto.response;

import lombok.*;

/**
 * DTO de réponse pour les détails bancaires d'un bénéficiaire.
 * Retourné uniquement quand le canal est BANK_TRANSFER.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BankDetailsResponse {

    /** Identifiant des détails bancaires. */
    private Long id;

    /** Nom de la banque destinataire. */
    private String bankName;

    /**
     * IBAN masqué pour la sécurité.
     * Exemple : FR76 **** **** **** **** 189
     */
    private String iban;

    /** Code BIC/SWIFT de la banque. */
    private String bic;
}