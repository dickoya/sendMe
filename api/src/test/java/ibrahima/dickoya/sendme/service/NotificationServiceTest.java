package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.response.NotificationResponse;
import ibrahima.dickoya.sendme.entity.Notification;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.NotificationType;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.NotificationRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour NotificationService")
class NotificationServiceTest {

        @Mock
        private NotificationRepository notificationRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private SseEmitterService sseEmitterService;

        @InjectMocks
        private NotificationService notificationService;

        private User user;
        private Transaction transaction;
        private final String USER_EMAIL = "ibrahima@sendme.com";
        private final Long USER_ID = 1L;

        @BeforeEach
        void setUp() {
                user = User.builder()
                                .id(USER_ID)
                                .email(USER_EMAIL)
                                .build();

                transaction = Transaction.builder()
                                .id(100L)
                                .reference("SENDME-ABC-123")
                                .build();
        }

        @Nested
        @DisplayName("Tests de createAndSend")
        class CreateAndSendTests {

                @Test
                @DisplayName("createAndSend - Doit créer et sauvegarder une notification")
                void createAndSend_ShouldCreateAndSaveNotification() {
                        // Arrange
                        Notification savedNotification = Notification.builder()
                                        .id(1L)
                                        .message("Transfert réussi")
                                        .type(NotificationType.TRANSFER_COMPLETED)
                                        .isRead(false)
                                        .user(user)
                                        .transaction(transaction)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(notificationRepository.save(any(Notification.class)))
                                        .thenReturn(savedNotification);

                        // Act
                        NotificationResponse response = notificationService.createAndSend(
                                        user, "Transfert réussi", NotificationType.TRANSFER_COMPLETED, transaction);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getMessage()).isEqualTo("Transfert réussi");
                        assertThat(response.getType()).isEqualTo(NotificationType.TRANSFER_COMPLETED);
                        assertThat(response.getIsRead()).isFalse();
                        assertThat(response.getTransactionId()).isEqualTo(100L);
                        assertThat(response.getTransactionReference()).isEqualTo("SENDME-ABC-123");
                }

                @Test
                @DisplayName("createAndSend - Doit envoyer la notification via SSE")
                void createAndSend_ShouldSendViaSSE() {
                        // Arrange
                        Notification savedNotification = Notification.builder()
                                        .id(1L)
                                        .message("Transfert réussi")
                                        .type(NotificationType.TRANSFER_COMPLETED)
                                        .isRead(false)
                                        .user(user)
                                        .transaction(transaction)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(notificationRepository.save(any(Notification.class)))
                                        .thenReturn(savedNotification);

                        // Act
                        notificationService.createAndSend(
                                        user, "Transfert réussi", NotificationType.TRANSFER_COMPLETED, transaction);

                        // Assert
                        verify(sseEmitterService).sendToUser(
                                        eq(USER_ID), any(NotificationResponse.class), eq("TRANSFER_COMPLETED"));
                }

                @Test
                @DisplayName("createAndSend - Doit fonctionner sans transaction associée")
                void createAndSend_ShouldWorkWithoutTransaction() {
                        // Arrange
                        Notification savedNotification = Notification.builder()
                                        .id(2L)
                                        .message("Bienvenue sur SendMe")
                                        .type(NotificationType.INFO)
                                        .isRead(false)
                                        .user(user)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(notificationRepository.save(any(Notification.class)))
                                        .thenReturn(savedNotification);

                        // Act
                        NotificationResponse response = notificationService.createAndSend(
                                        user, "Bienvenue sur SendMe", NotificationType.INFO, null);

                        // Assert
                        assertThat(response.getTransactionId()).isNull();
                        assertThat(response.getTransactionReference()).isNull();
                }

                @Test
                @DisplayName("createAndSend - Doit créer une notification non lue par défaut")
                void createAndSend_ShouldCreateUnreadNotification() {
                        // Arrange
                        Notification savedNotification = Notification.builder()
                                        .id(3L)
                                        .message("Notification test")
                                        .type(NotificationType.INFO)
                                        .isRead(false)
                                        .user(user)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        when(notificationRepository.save(any(Notification.class)))
                                        .thenReturn(savedNotification);

                        // Act
                        NotificationResponse response = notificationService.createAndSend(
                                        user, "Notification test", NotificationType.INFO, null);

                        // Assert
                        assertThat(response.getIsRead()).isFalse();
                }
        }

        @Nested
        @DisplayName("Tests de findAllByUser")
        class FindAllByUserTests {

                @Test
                @DisplayName("findAllByUser - Doit retourner les notifications paginées")
                void findAllByUser_ShouldReturnPagedNotifications() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

                        Notification notif = Notification.builder()
                                        .id(1L)
                                        .message("Test")
                                        .type(NotificationType.INFO)
                                        .isRead(false)
                                        .user(user)
                                        .createdAt(LocalDateTime.now())
                                        .build();

                        Pageable pageable = PageRequest.of(0, 10);
                        Page<Notification> page = new PageImpl<>(List.of(notif));
                        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(USER_ID, pageable))
                                        .thenReturn(page);

                        // Act
                        Page<NotificationResponse> result = notificationService.findAllByUser(USER_EMAIL, pageable);

                        // Assert
                        assertThat(result.getContent()).hasSize(1);
                        assertThat(result.getContent().get(0).getMessage()).isEqualTo("Test");
                }

                @Test
                @DisplayName("findAllByUser - Doit lever ResourceNotFoundException si l'utilisateur n'existe pas")
                void findAllByUser_ShouldThrow_WhenUserNotFound() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> notificationService.findAllByUser(USER_EMAIL, PageRequest.of(0, 10)))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Utilisateur non trouvé");
                }
        }

        @Nested
        @DisplayName("Tests de markAsRead")
        class MarkAsReadTests {

                private final Long NOTIFICATION_ID = 1L;

                @Test
                @DisplayName("markAsRead - Doit marquer une notification comme lue")
                void markAsRead_ShouldMarkNotificationAsRead() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

                        Notification notification = Notification.builder()
                                        .id(NOTIFICATION_ID)
                                        .message("Test")
                                        .type(NotificationType.INFO)
                                        .isRead(false)
                                        .user(user)
                                        .build();

                        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));

                        // Act
                        notificationService.markAsRead(NOTIFICATION_ID, USER_EMAIL);

                        // Assert
                        assertThat(notification.getIsRead()).isTrue();
                        verify(notificationRepository).save(notification);
                }

                @Test
                @DisplayName("markAsRead - Doit lever ResourceNotFoundException si la notification n'existe pas")
                void markAsRead_ShouldThrow_WhenNotificationNotFound() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> notificationService.markAsRead(NOTIFICATION_ID, USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Notification non trouvée");
                }

                @Test
                @DisplayName("markAsRead - Doit lever ResourceNotFoundException si la notification appartient à un autre utilisateur")
                void markAsRead_ShouldThrow_WhenNotificationBelongsToAnotherUser() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

                        User otherUser = User.builder().id(999L).email("autre@email.com").build();
                        Notification notification = Notification.builder()
                                        .id(NOTIFICATION_ID)
                                        .message("Test")
                                        .type(NotificationType.INFO)
                                        .isRead(false)
                                        .user(otherUser)
                                        .build();

                        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));

                        // Act & Assert
                        assertThatThrownBy(() -> notificationService.markAsRead(NOTIFICATION_ID, USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Accès refusé");
                }
        }

        @Nested
        @DisplayName("Tests de markAllAsRead")
        class MarkAllAsReadTests {

                @Test
                @DisplayName("markAllAsRead - Doit marquer toutes les notifications comme lues")
                void markAllAsRead_ShouldMarkAllAsRead() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(notificationRepository.markAllAsReadByUserId(USER_ID)).thenReturn(5);

                        // Act
                        notificationService.markAllAsRead(USER_EMAIL);

                        // Assert
                        verify(notificationRepository).markAllAsReadByUserId(USER_ID);
                }

                @Test
                @DisplayName("markAllAsRead - Doit lever ResourceNotFoundException si l'utilisateur n'existe pas")
                void markAllAsRead_ShouldThrow_WhenUserNotFound() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> notificationService.markAllAsRead(USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Utilisateur non trouvé");
                }
        }

        @Nested
        @DisplayName("Tests de countUnread")
        class CountUnreadTests {

                @Test
                @DisplayName("countUnread - Doit retourner le nombre de notifications non lues")
                void countUnread_ShouldReturnUnreadCount() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(notificationRepository.countByUserIdAndIsReadFalse(USER_ID)).thenReturn(3L);

                        // Act
                        Long count = notificationService.countUnread(USER_EMAIL);

                        // Assert
                        assertThat(count).isEqualTo(3L);
                }

                @Test
                @DisplayName("countUnread - Doit retourner 0 si aucune notification non lue")
                void countUnread_ShouldReturnZero_WhenNoUnread() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
                        when(notificationRepository.countByUserIdAndIsReadFalse(USER_ID)).thenReturn(0L);

                        // Act
                        Long count = notificationService.countUnread(USER_EMAIL);

                        // Assert
                        assertThat(count).isZero();
                }

                @Test
                @DisplayName("countUnread - Doit lever ResourceNotFoundException si l'utilisateur n'existe pas")
                void countUnread_ShouldThrow_WhenUserNotFound() {
                        // Arrange
                        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> notificationService.countUnread(USER_EMAIL))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Utilisateur non trouvé");
                }
        }
}
