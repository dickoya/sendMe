package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.utils.DataMaskingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Classe abstraite de base pour toutes les stratégies de transfert.
 * 
 * Centralise le code commun partagé par toutes les implémentations :
 * - Génération de nombres aléatoires pour les simulations
 * - Délai de traitement simulé (non-bloquant via CompletableFuture)
 * - Masquage des données sensibles (IBAN, téléphone)
 * - Gestion uniforme des InterruptedException
 * 
 * MODE SIMULATION :
 * Les stratégies peuvent fonctionner en mode simulation (pour le développement)
 * ou en mode production (appels API réels). Le mode est configurable via :
 * - strategy.simulation-mode=true/false (défaut: false)
 * - strategy.failure-rate=5 (pourcentage d'échecs simulés, défaut: 0)
 */
@Slf4j
public abstract class BaseTransferStrategy implements TransferStrategy {

    /**
     * Générateur de nombres aléatoires partagé par toutes les stratégies.
     * Utilisé pour simuler les délais et les échecs aléatoires.
     */
    protected final Random random = new Random();

    /**
     * Active ou désactive le mode simulation.
     * En production (false), les appels API réels sont utilisés.
     * En simulation (true), des délais et échecs aléatoires sont simulés.
     */
    @Value("${strategy.simulation-mode:false}")
    private boolean simulationMode;

    /**
     * Taux d'échec simulé en pourcentage (0-100).
     * Utilisé uniquement en mode simulation.
     * 0 = jamais d'échec simulé, 5 = 5% d'échecs, 100 = toujours en échec.
     */
    @Value("${strategy.failure-rate:0}")
    private int failureRate;

    /**
     * Simule un délai de traitement réseau/API de manière non-bloquante.
     * 
     * Utilise CompletableFuture.delayedExecutor() au lieu de Thread.sleep()
     * pour ne pas bloquer le thread du pool Tomcat.
     * 
     * En mode production, cette méthode est remplacée par de vrais appels HTTP
     * asynchrones (WebClient, RestTemplate async, ou CompletableFuture).
     * 
     * @param minMs délai minimum en millisecondes
     * @param maxMs délai maximum en millisecondes
     * @return CompletableFuture qui se complète après le délai
     */
    protected CompletableFuture<Void> simulateProcessingDelay(int minMs, int maxMs) {
        if (!simulationMode) {
            // En production : pas de délai simulé, retour immédiat
            return CompletableFuture.completedFuture(null);
        }
        int delay = minMs + random.nextInt(maxMs - minMs);
        log.debug("[SIMULATION] Delai de traitement simule : {} ms", delay);
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, CompletableFuture.delayedExecutor(0, TimeUnit.MILLISECONDS));
    }

    /**
     * Vérifie si un échec doit être simulé selon le taux configuré.
     * 
     * @return true si un échec doit être simulé
     */
    protected boolean shouldSimulateFailure() {
        if (!simulationMode) {
            return false; // Pas d'échec simulé en production
        }
        if (failureRate <= 0) {
            return false; // Taux à 0 = jamais d'échec
        }
        return random.nextInt(100) < failureRate;
    }

    /**
     * Vérifie si le mode simulation est actif.
     * 
     * @return true si le mode simulation est activé
     */
    protected boolean isSimulationMode() {
        return simulationMode;
    }

    /**
     * Masque un numéro de téléphone pour la sécurité dans les logs.
     * Délègue à DataMaskingUtils pour centraliser la logique.
     * 
     * @param phone le numéro de téléphone complet
     * @return le numéro masqué
     */
    protected String maskPhone(String phone) {
        return DataMaskingUtils.maskPhone(phone);
    }

    /**
     * Masque un IBAN pour la sécurité dans les logs.
     * Délègue à DataMaskingUtils pour centraliser la logique.
     * 
     * @param iban l'IBAN complet
     * @return l'IBAN masqué
     */
    protected String maskIban(String iban) {
        return DataMaskingUtils.maskIban(iban);
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
