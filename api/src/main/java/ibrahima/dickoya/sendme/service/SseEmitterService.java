package ibrahima.dickoya.sendme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service gérant les connexions SSE (Server-Sent Events).
 *
 * SSE est une technologie qui permet au serveur d'envoyer
 * des données au client en temps réel, via une connexion HTTP
 * persistante unidirectionnelle (serveur → client).
 *
 * Avantages par rapport aux WebSockets pour ce cas d'usage :
 * - Plus simple à implémenter côté Spring Boot
 * - Reconnexion automatique côté browser
 * - Fonctionne sur HTTP/1.1 standard
 * - Suffisant car on n'a besoin que de serveur → client
 *
 * Fonctionnement :
 * 1. Le frontend ouvre une connexion SSE vers /api/notifications/stream
 * 2. Ce service enregistre l'émetteur avec l'userId comme clé
 * 3. Quand une notification est créée, on retrouve l'émetteur
 *    et on pousse la notification en temps réel
 */
@Service
@Slf4j
public class SseEmitterService {

    /**
     * Map thread-safe stockant les émetteurs SSE actifs.
     * Clé   : userId (Long) — identifiant de l'utilisateur connecté
     * Valeur : SseEmitter — la connexion SSE active
     *
     * ConcurrentHashMap est utilisé car plusieurs threads peuvent
     * accéder à cette map simultanément (requêtes concurrentes).
     */
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Crée et enregistre un nouvel émetteur SSE pour un utilisateur.
     *
     * Appelé quand le frontend ouvre la connexion SSE.
     * Durée de vie : Long.MAX_VALUE (connexion maintenue indéfiniment).
     *
     * @param userId identifiant de l'utilisateur connecté
     * @return SseEmitter la connexion SSE à retourner au frontend
     */
    public SseEmitter createEmitter(Long userId) {

        // Supprimer l'ancien émetteur si l'utilisateur se reconnecte
        SseEmitter existingEmitter = emitters.get(userId);
        if (existingEmitter != null) {
            existingEmitter.complete();
            emitters.remove(userId);
        }

        // Créer un nouvel émetteur avec durée illimitée
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Enregistrer l'émetteur dans la map
        emitters.put(userId, emitter);
        log.info("[SSE] Nouvelle connexion SSE pour userId : {}", userId);

        // Nettoyage automatique quand la connexion se ferme
        emitter.onCompletion(() -> {
            emitters.remove(userId);
            log.info("[SSE] Connexion SSE fermée pour userId : {}", userId);
        });

        // Nettoyage en cas de timeout
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            log.info("[SSE] Timeout SSE pour userId : {}", userId);
        });

        // Nettoyage en cas d'erreur
        emitter.onError(e -> {
            emitters.remove(userId);
            log.warn("[SSE] Erreur SSE pour userId : {} — {}", userId, e.getMessage());
        });

        // Envoyer un événement initial pour confirmer la connexion
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("Connexion SSE établie"));
        } catch (IOException e) {
            log.warn("[SSE] Impossible d'envoyer l'événement initial : {}", e.getMessage());
        }

        return emitter;
    }

    /**
     * Envoie une notification en temps réel à un utilisateur spécifique.
     *
     * Appelé par le NotificationService après chaque création de notification.
     * Si l'utilisateur n'est pas connecté (pas d'émetteur actif),
     * la notification est quand même sauvegardée en base — elle sera
     * visible à la prochaine connexion.
     *
     * @param userId   identifiant de l'utilisateur destinataire
     * @param data     les données à envoyer (NotificationResponse)
     * @param eventName nom de l'événement SSE (ex: "TRANSFER_COMPLETED")
     */
    public void sendToUser(Long userId, Object data, String eventName) {

        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            // L'utilisateur n'est pas connecté — notification stockée en BDD uniquement
            log.debug("[SSE] Aucune connexion active pour userId : {}", userId);
            return;
        }

        try {
            // Envoyer l'événement SSE avec son nom et ses données
            emitter.send(SseEmitter.event()
                    .name(eventName)   // nom de l'événement (côté frontend : addEventListener)
                    .data(data));      // données JSON sérialisées automatiquement
            log.info("[SSE] Événement '{}' envoyé à userId : {}", eventName, userId);

        } catch (IOException e) {
            // La connexion est cassée — nettoyer
            log.warn("[SSE] Échec envoi pour userId : {} — connexion cassée", userId);
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
    }

    /**
     * Retourne le nombre de connexions SSE actives.
     * Utile pour le monitoring et le tableau de bord admin.
     *
     * @return nombre d'utilisateurs connectés en SSE
     */
    public int getActiveConnectionsCount() {
        return emitters.size();
    }
}