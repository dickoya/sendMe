package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.dto.response.NotificationResponse;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.service.NotificationService;
import ibrahima.dickoya.sendme.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST gérant les endpoints des notifications.
 *
 * Routes disponibles :
 * GET  /api/notifications/stream     → flux SSE temps réel
 * GET  /api/notifications            → liste historique
 * GET  /api/notifications/unread-count → badge de notifications
 * PUT  /api/notifications/{id}/read  → marquer une comme lue
 * PUT  /api/notifications/read-all   → tout marquer comme lu
 *
 * Le flux SSE utilise MediaType.TEXT_EVENT_STREAM_VALUE
 * qui est le Content-Type standard pour SSE.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;
    private final UserRepository userRepository;

    /**
     * Ouvre un flux SSE pour recevoir les notifications en temps réel.
     *
     * Le frontend appelle cet endpoint une seule fois au démarrage
     * et reste connecté. Spring envoie les événements au fil de l'eau.
     *
     * Côté frontend (React) :
     * const eventSource = new EventSource('/api/notifications/stream', {
     *     headers: { Authorization: `Bearer ${token}` }
     * });
     * eventSource.addEventListener('TRANSFER_COMPLETED', (e) => {
     *     const notif = JSON.parse(e.data);
     *     showToast(notif.message);
     * });
     *
     * @param userDetails utilisateur connecté extrait du JWT
     * @return SseEmitter la connexion SSE persistante
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @AuthenticationPrincipal UserDetails userDetails) {

        // Récupérer l'ID de l'utilisateur pour indexer son émetteur SSE
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        return sseEmitterService.createEmitter(user.getId());
    }

    /**
     * Retourne la liste complète des notifications de l'utilisateur.
     * Triées par date décroissante.
     *
     * @param userDetails utilisateur connecté
     * @return 200 OK avec la liste des notifications
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<NotificationResponse> notifications =
                notificationService.findAllByUser(userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Liste des notifications", notifications));
    }

    /**
     * Retourne le nombre de notifications non lues.
     * Utilisé pour afficher le badge rouge dans la navbar.
     *
     * @param userDetails utilisateur connecté
     * @return 200 OK avec le nombre de non lues
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> countUnread(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long count = notificationService.countUnread(userDetails.getUsername());

        return ResponseEntity.ok(
                ApiResponse.success("Nombre de non lues",
                        Map.of("unreadCount", count)));
    }

    /**
     * Marque une notification spécifique comme lue.
     *
     * @param id          identifiant de la notification
     * @param userDetails utilisateur connecté
     * @return 200 OK
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Notification marquée comme lue", null));
    }

    /**
     * Marque toutes les notifications de l'utilisateur comme lues.
     *
     * @param userDetails utilisateur connecté
     * @return 200 OK
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Toutes les notifications lues", null));
    }
}