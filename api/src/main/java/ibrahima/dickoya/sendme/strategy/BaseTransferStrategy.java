package ibrahima.dickoya.sendme.strategy;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Classe abstraite de base pour toutes les stratégies de transfert.
 * 
 * Centralise le code commun partagé par toutes les implémentations :
 * - Génération de nombres aléatoires pour les simulations
 * - Délai de traitement simulé
 * - Masquage des données sensibles (IBAN, téléphone)
 * - Gestion uniforme des InterruptedException
 * 
 * Chaque stratégie concrète hérite de cette classe et implémente
 * uniquement la logique spécifique à son canal.
 */
@Slf4j
public abstract class BaseTransferStrategy implements TransferStrategy {

    /**
     * Générateur de nombres aléatoires partagé par toutes les stratégies.
     * Utilisé pour simuler les délais et les échecs aléatoires.
     */
    protected final Random random = new Random();

    /**
     * Simule un délai de traitement réseau/API.
     * 
     * En production, ce délai serait remplacé par un véritable appel HTTP
     * vers l'API de l'opérateur (Orange, MTN, banque, etc.).
     * 
     * @param minMs délai minimum en millisecondes
     * @param maxMs délai maximum en millisecondes
     * @throws InterruptedException si le thread est interrompu pendant le délai
     */
    protected void simulateProcessingDelay(int minMs, int maxMs)
            throws InterruptedException {
        int delay = minMs + random.nextInt(maxMs - minMs);
        Thread.sleep(delay);
    }

    /**
     * Masque un numéro de téléphone pour la sécurité dans les logs.
     * 
     * Affiche seulement les premiers caractères et masque les 4 derniers.
     * Exemple : +22507123456 → +2250712****
     * 
     * @param phone le numéro de téléphone complet
     * @return le numéro masqué
     */
    protected String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return "****";
        }
        return phone.substring(0, phone.length() - 4) + "****";
    }

    /**
     * Masque un IBAN pour la sécurité dans les logs.
     * 
     * Affiche seulement les 4 premiers et 6 derniers caractères.
     * Exemple : FR7630006000011234567890189 → FR76****890189
     * 
     * @param iban l'IBAN complet
     * @return l'IBAN masqué
     */
    protected String maskIban(String iban) {
        if (iban == null || iban.length() < 8) {
            return "****";
        }
        return iban.substring(0, 4) + "****" + iban.substring(iban.length() - 6);
    }

    /**
     * Gère uniformément les InterruptedException.
     * 
     * Restaure l'état d'interruption du thread (bonne pratique Java)
     * et retourne un résultat d'échec avec un message explicite.
     * 
     * @param channelName le nom du canal (pour le message d'erreur)
     * @return TransferResult d'échec
     */
    protected TransferResult handleInterruption(String channelName) {
        Thread.currentThread().interrupt();
        log.error("[{}] Thread interrompu pendant le traitement", channelName);
        return TransferResult.failure("Interruption du traitement " + channelName);
    }
}
