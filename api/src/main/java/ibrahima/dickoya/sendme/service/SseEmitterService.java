package ibrahima.dickoya.sendme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseEmitterService {

    private final Map<Long, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> {
            removeEmitter(userId, emitter);
            log.info("[SSE] Connexion SSE fermee pour userId : {}", userId);
        });

        emitter.onTimeout(() -> {
            removeEmitter(userId, emitter);
            log.info("[SSE] Timeout SSE pour userId : {}", userId);
        });

        emitter.onError(e -> {
            removeEmitter(userId, emitter);
            log.warn("[SSE] Erreur SSE pour userId : {} - {}", userId, e.getMessage());
        });

        emitters
                .computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet())
                .add(emitter);
        log.info("[SSE] Nouvelle connexion SSE pour userId : {}", userId);

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("Connexion SSE etablie"));
        } catch (IOException | IllegalStateException e) {
            removeEmitter(userId, emitter);
            log.warn("[SSE] Impossible d'envoyer l'evenement initial : {}", e.getMessage());
        }

        return emitter;
    }

    public void sendToUser(Long userId, Object data, String eventName) {
        Set<SseEmitter> userEmitters = emitters.get(userId);

        if (userEmitters == null || userEmitters.isEmpty()) {
            log.debug("[SSE] Aucune connexion active pour userId : {}", userId);
            return;
        }

        userEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                log.info("[SSE] Evenement '{}' envoye a userId : {}", eventName, userId);
            } catch (IOException | IllegalStateException e) {
                log.warn("[SSE] Echec envoi pour userId : {} - connexion cassee ou obsolete", userId);
                removeEmitter(userId, emitter);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        });
    }

    public int getActiveConnectionsCount() {
        return emitters.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        Set<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }

        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId, userEmitters);
        }
    }
}
