package ibrahima.dickoya.sendme.controller;

import ibrahima.dickoya.sendme.dto.response.ApiResponse;
import ibrahima.dickoya.sendme.dto.response.NotificationResponse;
import ibrahima.dickoya.sendme.repository.UserRepository;
import ibrahima.dickoya.sendme.service.NotificationService;
import ibrahima.dickoya.sendme.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = """
                        Gestion des notifications en temps réel via SSE (Server-Sent Events).

                        **Connexion SSE :**
                ```javascript
                        const eventSource = new EventSource(
                            'http://localhost:8080/api/notifications/stream',
                            { headers: { Authorization: `Bearer ${token}` } }
                        );
                        eventSource.addEventListener('TRANSFER_COMPLETED', (e) => {
                            const notif = JSON.parse(e.data);
                            showToast(notif.message);
                        });
                ```

                        **Événements disponibles :**
                        - `TRANSFER_INITIATED` — transfert créé
                        - `TRANSFER_COMPLETED` — transfert réussi
                        - `TRANSFER_FAILED` — transfert échoué
                        - `TRANSFER_REFUNDED` — remboursement effectué
                        - `PAYMENT_CONFIRMED` — paiement Stripe confirmé
                        """)
public class NotificationController {

        private final NotificationService notificationService;
        private final SseEmitterService sseEmitterService;
        private final UserRepository userRepository;

        @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        @Operation(summary = "Flux SSE temps réel", description = """
                        Ouvre une connexion SSE persistante pour recevoir les notifications
                        en temps réel. La connexion reste ouverte indéfiniment.

                        **À appeler une seule fois** au démarrage du frontend.
                        Les événements arrivent automatiquement sans polling.
                        """)
        public SseEmitter stream(
                        @AuthenticationPrincipal UserDetails userDetails) {
                var user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow();
                return sseEmitterService.createEmitter(user.getId());
        }

        @GetMapping
        @Operation(summary = "Historique des notifications", description = "Retourne toutes les notifications de l'utilisateur, "
                        +
                        "triées par date décroissante.")
        public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getAll(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @AuthenticationPrincipal UserDetails userDetails) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                var notifications = notificationService
                                .findAllByUser(userDetails.getUsername(), pageable);
                return ResponseEntity.ok(
                                ApiResponse.success("Liste des notifications", notifications));
        }

        @GetMapping("/unread-count")
        @Operation(summary = "Nombre de notifications non lues", description = "Retourne le nombre de notifications non lues. "
                        +
                        "Utilisé pour afficher le badge rouge dans la navbar.")
        public ResponseEntity<ApiResponse<Map<String, Long>>> countUnread(
                        @AuthenticationPrincipal UserDetails userDetails) {
                Long count = notificationService.countUnread(userDetails.getUsername());
                return ResponseEntity.ok(
                                ApiResponse.success("Notifications non lues",
                                                Map.of("unreadCount", count)));
        }

        @PutMapping("/{id}/read")
        @Operation(summary = "Marquer une notification comme lue", description = "Marque une notification spécifique comme lue. "
                        +
                        "Le badge rouge diminue d'une unité.")
        public ResponseEntity<ApiResponse<Void>> markAsRead(
                        @Parameter(description = "Identifiant de la notification", required = true) @PathVariable Long id,
                        @AuthenticationPrincipal UserDetails userDetails) {
                notificationService.markAsRead(id, userDetails.getUsername());
                return ResponseEntity.ok(
                                ApiResponse.success("Notification marquee comme lue", null));
        }

        @PutMapping("/read-all")
        @Operation(summary = "Tout marquer comme lu", description = "Marque toutes les notifications comme lues. " +
                        "Le badge rouge disparaît complètement.")
        public ResponseEntity<ApiResponse<Void>> markAllAsRead(
                        @AuthenticationPrincipal UserDetails userDetails) {
                notificationService.markAllAsRead(userDetails.getUsername());
                return ResponseEntity.ok(
                                ApiResponse.success("Toutes les notifications lues", null));
        }
}