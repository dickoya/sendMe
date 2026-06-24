package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.response.NotificationResponse;
import ibrahima.dickoya.sendme.entity.Notification;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.NotificationType;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.NotificationRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service gérant la création et la gestion des notifications.
 *
 * Chaque notification est :
 * 1. Sauvegardée en base de données (persistance)
 * 2. Envoyée en temps réel via SSE (instantanéité)
 *
 * Ce service est appelé par le TransferService à chaque
 * changement de statut d'une transaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

        /** Repository pour sauvegarder les notifications en base. */
        private final NotificationRepository notificationRepository;

        /** Repository pour récupérer l'utilisateur destinataire. */
        private final UserRepository userRepository;

        /**
         * Service SSE pour pousser les notifications en temps réel.
         * Si l'utilisateur n'est pas connecté, la notification
         * est quand même sauvegardée en base.
         */
        private final SseEmitterService sseEmitterService;

        /**
         * Crée une notification et l'envoie en temps réel.
         *
         * Cette méthode est le point central de création de notifications.
         * Elle sauvegarde en base ET pousse via SSE en une seule opération.
         *
         * @param user        l'utilisateur destinataire
         * @param message     le message à afficher
         * @param type        le type de notification
         * @param transaction la transaction liée (peut être null)
         * @return NotificationResponse la notification créée
         */
        @Transactional
        public NotificationResponse createAndSend(
                        User user,
                        String message,
                        NotificationType type,
                        Transaction transaction) {

                // 1. Construire et sauvegarder la notification en base
                Notification notification = Notification.builder()
                                .message(message)
                                .type(type)
                                .isRead(false) // non lue par défaut
                                .user(user)
                                .transaction(transaction)
                                .build();

                notification = notificationRepository.save(notification);
                log.info("[NOTIF] Notification créée : type={}, userId={}",
                                type, user.getId());

                // 2. Convertir en DTO
                NotificationResponse response = toResponse(notification);

                // 3. Pousser en temps réel via SSE (sans bloquer si l'utilisateur est
                // déconnecté)
                sseEmitterService.sendToUser(user.getId(), response, type.name());

                return response;
        }

        /**
         * Récupère les notifications de l'utilisateur connecté avec pagination.
         * Triées par date décroissante (plus récentes en premier).
         *
         * La pagination évite de charger toutes les notifications en mémoire,
         * ce qui améliore les performances pour les utilisateurs ayant
         * beaucoup de notifications.
         *
         * @param userEmail email de l'utilisateur connecté
         * @param pageable  paramètres de pagination (page, size, sort)
         * @return Page de notifications formatées en DTO
         */
        public Page<NotificationResponse> findAllByUser(String userEmail, Pageable pageable) {

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Utilisateur non trouvé : " + userEmail));

                return notificationRepository
                                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                                .map(this::toResponse);
        }

        /**
         * Marque une notification spécifique comme lue.
         *
         * @param notificationId identifiant de la notification
         * @param userEmail      email de l'utilisateur connecté
         */
        @Transactional
        public void markAsRead(Long notificationId, String userEmail) {

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Utilisateur non trouvé : " + userEmail));

                Notification notification = notificationRepository
                                .findById(notificationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Notification non trouvée : " + notificationId));

                // Vérifier que la notification appartient à l'utilisateur
                if (!notification.getUser().getId().equals(user.getId())) {
                        throw new ResourceNotFoundException("Accès refusé à cette notification");
                }

                // Marquer comme lue
                notification.setIsRead(true);
                notificationRepository.save(notification);
                log.info("[NOTIF] Notification {} marquée comme lue", notificationId);
        }

        /**
         * Marque toutes les notifications de l'utilisateur comme lues.
         * Utilisé par le bouton "Tout marquer comme lu" dans l'interface.
         *
         * @param userEmail email de l'utilisateur connecté
         */
        @Transactional
        public void markAllAsRead(String userEmail) {

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Utilisateur non trouvé : " + userEmail));

                // Récupérer toutes les notifications non lues
                List<Notification> unread = notificationRepository
                                .findByUserIdOrderByCreatedAtDesc(user.getId())
                                .stream()
                                .filter(n -> !n.getIsRead())
                                .collect(Collectors.toList());

                // Marquer toutes comme lues
                unread.forEach(n -> n.setIsRead(true));
                notificationRepository.saveAll(unread);

                log.info("[NOTIF] {} notifications marquées comme lues pour userId : {}",
                                unread.size(), user.getId());
        }

        /**
         * Compte le nombre de notifications non lues.
         * Utilisé pour afficher le badge rouge dans la navbar.
         *
         * @param userEmail email de l'utilisateur connecté
         * @return nombre de notifications non lues
         */
        public Long countUnread(String userEmail) {

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Utilisateur non trouvé : " + userEmail));

                return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        }

        /**
         * Convertit une entité Notification en DTO NotificationResponse.
         *
         * @param notification l'entité à convertir
         * @return NotificationResponse le DTO formaté
         */
        private NotificationResponse toResponse(Notification notification) {
                return NotificationResponse.builder()
                                .id(notification.getId())
                                .message(notification.getMessage())
                                .type(notification.getType())
                                .isRead(notification.getIsRead())
                                .transactionId(notification.getTransaction() != null
                                                ? notification.getTransaction().getId()
                                                : null)
                                .transactionReference(notification.getTransaction() != null
                                                ? notification.getTransaction().getReference()
                                                : null)
                                .createdAt(notification.getCreatedAt())
                                .build();
        }
}