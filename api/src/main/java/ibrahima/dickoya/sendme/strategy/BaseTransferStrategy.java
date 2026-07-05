package ibrahima.dickoya.sendme.strategy;

import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.BeneficiaryChannel;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.utils.DataMaskingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Classe abstraite de base pour toutes les stratégies de transfert.
 * 
 * Implémente le Template Method Pattern : la méthode
 * {@link #execute(Transaction, Beneficiary)}
 * définit le squelette commun de traitement, tandis que les sous-classes
 * fournissent les détails spécifiques via des méthodes abstraites/protégées.
 * 
 * Centralise le code commun partagé par toutes les implémentations :
 * - Génération de nombres aléatoires pour les simulations
 * - Délai de traitement simulé (non-bloquant via CompletableFuture)
 * - Masquage des données sensibles (IBAN, téléphone)
 * - Gestion uniforme des InterruptedException
 * - Squelette complet d'exécution (Template Method)
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

    // ─── Méthodes abstraites : chaque stratégie concrète doit les définir ───

    /**
     * Retourne le type de canal géré par cette stratégie.
     * Utilisé pour filtrer le canal correspondant dans la liste du bénéficiaire.
     */
    protected abstract ChannelType getChannelType();

    /**
     * Retourne le nom du canal pour les logs et les messages d'erreur.
     * Exemple : "ORANGE_MONEY", "BANK_TRANSFER"
     */
    protected abstract String getChannelName();

    /**
     * Retourne le préfixe de la référence externe générée.
     * Exemple : "OM-", "BANK-", "MTN-"
     */
    protected abstract String getReferencePrefix();

    /**
     * Retourne le délai minimum de simulation en millisecondes.
     */
    protected abstract int getDelayMinMs();

    /**
     * Retourne le délai maximum de simulation en millisecondes.
     */
    protected abstract int getDelayMaxMs();

    /**
     * Retourne le message d'échec simulé personnalisé.
     */
    protected abstract String getFailureMessage();

    /**
     * Retourne le message d'erreur technique personnalisé.
     */
    protected abstract String getErrorMessage();

    // ─── Template Method ───────────────────────────────────────────────────

    /**
     * Template Method qui définit le squelette d'exécution d'un transfert.
     * 
     * Les sous-classes fournissent les détails spécifiques via les méthodes
     * abstraites définies ci-dessus.
     *
     * @param transaction la transaction contenant le montant, la devise, etc.
     * @param beneficiary le bénéficiaire avec ses coordonnées de réception
     * @return TransferResult le résultat de l'opération (succès ou échec)
     */
    @Override
    public TransferResult execute(Transaction transaction, Beneficiary beneficiary) {
        String channelName = getChannelName();
        String contactInfo = extractContactInfo(beneficiary);

        log.info("[{}] Debut du transfert vers : {}", channelName, maskContactInfo(contactInfo));

        try {
            // Simuler le délai de traitement (non-bloquant)
            simulateProcessingDelay(getDelayMinMs(), getDelayMaxMs())
                    .get(5, TimeUnit.SECONDS);

            // Simuler un échec selon le taux configuré (uniquement en mode simulation)
            if (shouldSimulateFailure()) {
                log.warn("[{}] Transfert rejete : {} (simulation)", channelName, getFailureMessage());
                return TransferResult.failure(getFailureMessage());
            }

            // Générer une référence externe simulée
            String externalRef = getReferencePrefix()
                    + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

            log.info("[{}] Transfert reussi. Ref : {}", channelName, externalRef);
            return TransferResult.success(externalRef);

        } catch (InterruptedException e) {
            return handleInterruption(channelName);
        } catch (Exception e) {
            log.error("[{}] Erreur lors du transfert : {}", channelName, e.getMessage());
            return TransferResult.failure(getErrorMessage() + " : " + e.getMessage());
        }
    }

    /**
     * Extrait les informations de contact du bénéficiaire selon le type de canal.
     * Pour les canaux Mobile Money → numéro de téléphone.
     * Pour les canaux bancaires → IBAN.
     */
    private String extractContactInfo(Beneficiary beneficiary) {
        return beneficiary.getChannels().stream()
                .filter(ch -> ch.getChannel() == getChannelType())
                .findFirst()
                .map(this::getChannelValue)
                .orElse("N/A");
    }

    /**
     * Récupère la valeur du canal (téléphone ou IBAN) selon son type.
     */
    private String getChannelValue(BeneficiaryChannel channel) {
        if (channel.getChannel() == ChannelType.BANK_TRANSFER) {
            return channel.getIban() != null ? channel.getIban() : "N/A";
        }
        return channel.getPhoneNumber() != null ? channel.getPhoneNumber() : "N/A";
    }

    /**
     * Masque les informations de contact selon le type de canal.
     */
    private String maskContactInfo(String contactInfo) {
        if (contactInfo == null || contactInfo.equals("N/A")) {
            return contactInfo;
        }
        // Si ça ressemble à un IBAN (commence par 2 lettres + 2 chiffres)
        if (contactInfo.matches("^[A-Z]{2}\\d{2}.*")) {
            return maskIban(contactInfo);
        }
        // Sinon c'est un numéro de téléphone
        return maskPhone(contactInfo);
    }

    // ─── Méthodes de simulation ────────────────────────────────────────────

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
        },
                CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS));
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
