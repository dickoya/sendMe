package ibrahima.dickoya.sendme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitterService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {
        // 1. Créer le nouvel émetteur immédiatement avec une durée illimitée
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // 2. Configurer les callbacks de nettoyage AVANT de l'exposer dans la map
        emitter.onCompletion(() -> {
            // Sécurité : on ne supprime que si c'est bien notre instance qui est encore présente
            emitters.remove(userId, emitter);
            log.info("[SSE] Connexion SSE fermée pour userId : {}", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId, emitter);
            log.info("[SSE] Timeout SSE pour userId : {}", userId);
        });

        emitter.onError(e -> {
            emitters.remove(userId, emitter);
            log.warn("[SSE] Erreur SSE pour userId : {} — {}", userId, e.getMessage());
        });

        // 3. Gérer proprement le remplacement de l'ancien émetteur s'il existe
        SseEmitter existingEmitter = emitters.put(userId, emitter);
        log.info("[SSE] Nouvelle connexion SSE pour userId : {}", userId);

        if (existingEmitter != null) {
            try {
                existingEmitter.complete();
            } catch (Exception e) {
                // On ignore si l'ancien émetteur était déjà mal en point
            }
        }

        // 4. Envoyer l'événement initial (sécurisé contre le IllegalStateException)
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("Connexion SSE établie"));
        } catch (IOException | IllegalStateException e) {
            // Si le IllegalStateException survient, c'est qu'une autre requête concurrente
            // a déjà fermé cet émetteur. On logge tranquillement sans faire planter l'application.
            log.warn("[SSE] Impossible d'envoyer l'événement initial (connexion déjà remplacée ou fermée) : {}", e.getMessage());
        }

        return emitter;
    }

    public void sendToUser(Long userId, Object data, String eventName) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.debug("[SSE] Aucune connexion active pour userId : {}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            log.info("[SSE] Événement '{}' envoyé à userId : {}", eventName, userId);

        } catch (IOException | IllegalStateException e) {
            log.warn("[SSE] Échec envoi pour userId : {} — connexion cassée ou obsolète", userId);
            emitters.remove(userId, emitter);
            try {
                emitter.completeWithError(e);
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    public int getActiveConnectionsCount() {
        return emitters.size();
    }
}