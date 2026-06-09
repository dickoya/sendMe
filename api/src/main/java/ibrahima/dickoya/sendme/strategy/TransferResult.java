package ibrahima.dickoya.sendme.strategy;

import lombok.*;

/**
 * Objet résultat retourné par chaque stratégie de transfert.
 *
 * Encapsule le résultat de l'appel vers l'API de l'opérateur :
 * - success     : indique si le transfert a réussi ou échoué
 * - message     : message descriptif du résultat
 * - externalRef : référence générée par l'opérateur externe (utile en production)
 *
 * Utilisé par le TransferService pour décider de valider ou
 * d'annuler la transaction (@Transactional rollback).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransferResult {

    /**
     * Indique si le transfert a été accepté par l'opérateur.
     * true  → transaction passe à COMPLETED
     * false → transaction passe à FAILED + rollback
     */
    private boolean success;

    /**
     * Message retourné par l'opérateur.
     * Exemple : "Transfer successful", "Insufficient balance", "Network error"
     */
    private String message;

    /**
     * Référence externe générée par l'opérateur.
     * En production, ce serait l'ID de transaction côté opérateur.
     * Utile pour les réclamations et le support client.
     */
    private String externalRef;

    /**
     * Méthode utilitaire pour créer un résultat de succès.
     *
     * @param externalRef référence externe de l'opérateur
     * @return TransferResult avec success=true
     */
    public static TransferResult success(String externalRef) {
        return TransferResult.builder()
                .success(true)
                .message("Transfert effectue avec succes")
                .externalRef(externalRef)
                .build();
    }

    /**
     * Méthode utilitaire pour créer un résultat d'échec.
     *
     * @param reason la raison de l'échec
     * @return TransferResult avec success=false
     */
    public static TransferResult failure(String reason) {
        return TransferResult.builder()
                .success(false)
                .message(reason)
                .externalRef(null)
                .build();
    }
}