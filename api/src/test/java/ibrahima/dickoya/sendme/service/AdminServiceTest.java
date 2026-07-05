package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.FeeConfigRequest;
import ibrahima.dickoya.sendme.dto.response.DashboardResponse;
import ibrahima.dickoya.sendme.dto.response.TransactionResponse;
import ibrahima.dickoya.sendme.dto.response.UserResponse;
import ibrahima.dickoya.sendme.dto.response.VolumeDataPoint;
import ibrahima.dickoya.sendme.dto.response.SuccessRateDataPoint;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.Role;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.TransactionRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour AdminService")
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SseEmitterService sseEmitterService;

    @Mock
    private SimulatorService simulatorService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Jean")
                .lastName("Dupont")
                .email("jean@sendme.com")
                .phoneNumber("+33600000000")
                .role(Role.ROLE_USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        transaction = Transaction.builder()
                .id(100L)
                .reference("SENDME-REF-001")
                .amountSent(new BigDecimal("200.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("130000.00"))
                .currencyReceived("XOF")
                .exchangeRate(new BigDecimal("650.000000"))
                .fees(new BigDecimal("3.00"))
                .status(TransactionStatus.COMPLETED)
                .channel(ChannelType.ORANGE_MONEY)
                .stripePaymentIntentId("pi_test_123")
                .stripePaymentStatus("succeeded")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Tests de getDashboard")
    class GetDashboardTests {

        @Test
        @DisplayName("getDashboard - Doit retourner toutes les statistiques")
        void getDashboard_ShouldReturnAllStats() {
            // Arrange
            when(userRepository.count()).thenReturn(100L);
            when(userRepository.countByIsActiveTrue()).thenReturn(80L);
            when(transactionRepository.count()).thenReturn(500L);
            when(transactionRepository.countByStatus(TransactionStatus.COMPLETED)).thenReturn(400L);
            when(transactionRepository.countByStatus(TransactionStatus.FAILED)).thenReturn(50L);
            when(transactionRepository.countByStatus(TransactionStatus.REFUNDED)).thenReturn(10L);
            when(transactionRepository.countByStatus(TransactionStatus.PENDING)).thenReturn(30L);
            when(transactionRepository.countByStatus(TransactionStatus.PROCESSING)).thenReturn(10L);
            when(transactionRepository.sumAmountSentByStatus(TransactionStatus.COMPLETED)).thenReturn(50000.0);
            when(transactionRepository.sumFeesByStatus(TransactionStatus.COMPLETED)).thenReturn(1500.0);
            when(transactionRepository.sumAmountSentAfterDate(any(LocalDateTime.class),
                    eq(TransactionStatus.COMPLETED)))
                    .thenReturn(2000.0);
            when(transactionRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(25L);
            when(transactionRepository.countByChannel()).thenReturn(List.of(
                    new Object[] { "ORANGE_MONEY", 300L },
                    new Object[] { "BANK_TRANSFER", 200L }));
            when(sseEmitterService.getActiveConnectionsCount()).thenReturn(5);

            // Act
            DashboardResponse response = adminService.getDashboard();

            // Assert
            assertThat(response.getTotalUsers()).isEqualTo(100L);
            assertThat(response.getActiveUsers()).isEqualTo(80L);
            assertThat(response.getInactiveUsers()).isEqualTo(20L);
            assertThat(response.getTotalTransactions()).isEqualTo(500L);
            assertThat(response.getCompletedTransactions()).isEqualTo(400L);
            assertThat(response.getFailedTransactions()).isEqualTo(50L);
            assertThat(response.getRefundedTransactions()).isEqualTo(10L);
            assertThat(response.getPendingTransactions()).isEqualTo(40L);
            assertThat(response.getSuccessRate()).isEqualTo(80.0);
            assertThat(response.getTotalVolume()).isEqualTo(50000.0);
            assertThat(response.getTotalFees()).isEqualTo(1500.0);
            assertThat(response.getTodayVolume()).isEqualTo(2000.0);
            assertThat(response.getTodayTransactions()).isEqualTo(25L);
            assertThat(response.getTransactionsByChannel())
                    .containsEntry("ORANGE_MONEY", 300L)
                    .containsEntry("BANK_TRANSFER", 200L);
            assertThat(response.getActiveSSEConnections()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Tests de getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("getAllUsers - Doit retourner la liste paginée des utilisateurs")
        void getAllUsers_ShouldReturnPagedUsers() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> page = new PageImpl<>(List.of(user));
            when(userRepository.findAll(pageable)).thenReturn(page);
            when(transactionRepository.countBySenderId(1L)).thenReturn(5L);
            when(transactionRepository.sumAmountSentBySenderIdAndStatus(1L, TransactionStatus.COMPLETED))
                    .thenReturn(1000.0);

            // Act
            Page<UserResponse> result = adminService.getAllUsers(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("jean@sendme.com");
            assertThat(result.getContent().get(0).getTotalTransactions()).isEqualTo(5L);
            assertThat(result.getContent().get(0).getTotalAmountSent()).isEqualTo(1000.0);
        }
    }

    @Nested
    @DisplayName("Tests de getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("getUserById - Doit retourner l'utilisateur")
        void getUserById_ShouldReturnUser() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(transactionRepository.countBySenderId(1L)).thenReturn(3L);
            when(transactionRepository.sumAmountSentBySenderIdAndStatus(1L, TransactionStatus.COMPLETED))
                    .thenReturn(500.0);

            // Act
            UserResponse result = adminService.getUserById(1L);

            // Assert
            assertThat(result.getEmail()).isEqualTo("jean@sendme.com");
            assertThat(result.getFirstName()).isEqualTo("Jean");
        }

        @Test
        @DisplayName("getUserById - Doit lever ResourceNotFoundException si l'utilisateur n'existe pas")
        void getUserById_ShouldThrow_WhenUserNotFound() {
            // Arrange
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> adminService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Utilisateur non trouvé");
        }
    }

    @Nested
    @DisplayName("Tests de toggleUserStatus")
    class ToggleUserStatusTests {

        @Test
        @DisplayName("toggleUserStatus - Doit désactiver un compte actif")
        void toggleUserStatus_ShouldDeactivateActiveAccount() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(transactionRepository.countBySenderId(1L)).thenReturn(0L);
            when(transactionRepository.sumAmountSentBySenderIdAndStatus(1L, TransactionStatus.COMPLETED))
                    .thenReturn(0.0);

            // Act
            UserResponse result = adminService.toggleUserStatus(1L);

            // Assert
            assertThat(result.getIsActive()).isFalse();
            verify(emailService).sendAccountDisabledEmail(user);
        }

        @Test
        @DisplayName("toggleUserStatus - Doit réactiver un compte inactif")
        void toggleUserStatus_ShouldReactivateInactiveAccount() {
            // Arrange
            user.setIsActive(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(transactionRepository.countBySenderId(1L)).thenReturn(0L);
            when(transactionRepository.sumAmountSentBySenderIdAndStatus(1L, TransactionStatus.COMPLETED))
                    .thenReturn(0.0);

            // Act
            UserResponse result = adminService.toggleUserStatus(1L);

            // Assert
            assertThat(result.getIsActive()).isTrue();
            verify(emailService).sendAccountReactivatedEmail(user);
        }
    }

    @Nested
    @DisplayName("Tests de getAllTransactions")
    class GetAllTransactionsTests {

        @Test
        @DisplayName("getAllTransactions - Doit retourner les transactions paginées")
        void getAllTransactions_ShouldReturnPagedTransactions() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> page = new PageImpl<>(List.of(transaction));
            when(transactionRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);

            // Act
            Page<TransactionResponse> result = adminService.getAllTransactions(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getReference()).isEqualTo("SENDME-REF-001");
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Tests de getTransactionsByStatus")
    class GetTransactionsByStatusTests {

        @Test
        @DisplayName("getTransactionsByStatus - Doit filtrer par statut")
        void getTransactionsByStatus_ShouldFilterByStatus() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> page = new PageImpl<>(List.of(transaction));
            when(transactionRepository.findByStatusOrderByCreatedAtDesc(TransactionStatus.COMPLETED, pageable))
                    .thenReturn(page);

            // Act
            Page<TransactionResponse> result = adminService.getTransactionsByStatus(
                    TransactionStatus.COMPLETED, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("Tests de updateFeeRate")
    class UpdateFeeRateTests {

        @Test
        @DisplayName("updateFeeRate - Doit mettre à jour le taux et notifier les utilisateurs")
        void updateFeeRate_ShouldUpdateAndNotify() {
            // Arrange
            FeeConfigRequest request = FeeConfigRequest.builder()
                    .feeRate(new BigDecimal("3.50"))
                    .reason("Ajustement annuel")
                    .build();

            when(simulatorService.getCurrentFeeRate()).thenReturn(new BigDecimal("0.02")); // 2%
            when(userRepository.findByIsActiveTrue()).thenReturn(List.of(user));

            // Act
            adminService.updateFeeRate(request);

            // Assert
            verify(simulatorService).updateFeeRate(new BigDecimal("3.50"));
            verify(emailService).sendFeeUpdateEmail(eq(user), any(), eq(new BigDecimal("3.50")), anyString());
        }
    }

    @Nested
    @DisplayName("Tests de sendMaintenanceNotification")
    class SendMaintenanceNotificationTests {

        @Test
        @DisplayName("sendMaintenanceNotification - Doit notifier tous les utilisateurs actifs")
        void sendMaintenanceNotification_ShouldNotifyActiveUsers() {
            // Arrange
            when(userRepository.findByIsActiveTrue()).thenReturn(List.of(user));

            // Act
            adminService.sendMaintenanceNotification("15 juin 2025 à 02:00", "2 heures");

            // Assert
            verify(emailService).sendMaintenanceEmail(user, "15 juin 2025 à 02:00", "2 heures");
        }
    }

    @Nested
    @DisplayName("Tests de batchToggleUsers")
    class BatchToggleUsersTests {

        @Test
        @DisplayName("batchToggleUsers - Doit suspendre plusieurs utilisateurs")
        void batchToggleUsers_ShouldSuspendUsers() {
            // Arrange
            User user2 = User.builder().id(2L).email("user2@sendme.com").isActive(true).build();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

            // Act
            int count = adminService.batchToggleUsers(List.of(1L, 2L), true);

            // Assert
            assertThat(count).isEqualTo(2);
            assertThat(user.getIsActive()).isFalse();
            assertThat(user2.getIsActive()).isFalse();
            verify(emailService, times(2)).sendAccountDisabledEmail(any(User.class));
        }

        @Test
        @DisplayName("batchToggleUsers - Doit ignorer les IDs inexistants")
        void batchToggleUsers_ShouldSkipMissingIds() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            int count = adminService.batchToggleUsers(List.of(1L, 999L), true);

            // Assert
            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Tests de getVolumeByDay")
    class GetVolumeByDayTests {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("getVolumeByDay - Doit retourner les données de volume")
        void getVolumeByDay_ShouldReturnVolumeData() {
            // Arrange
            List<Object[]> mockData = Collections.singletonList(
                    new Object[] { Date.valueOf(LocalDate.now()), 1000.0, 5L });
            when(transactionRepository.getVolumeByDay(any(LocalDateTime.class)))
                    .thenReturn(mockData);

            // Act
            List<VolumeDataPoint> result = adminService.getVolumeByDay(7);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVolume()).isEqualTo(1000.0);
            assertThat(result.get(0).getTransactionCount()).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("Tests de getSuccessRateByDay")
    class GetSuccessRateByDayTests {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("getSuccessRateByDay - Doit retourner les taux de succès")
        void getSuccessRateByDay_ShouldReturnSuccessRateData() {
            // Arrange
            List<Object[]> mockData = Collections.singletonList(
                    new Object[] { Date.valueOf(LocalDate.now()), 10L, 8L, 2L });
            when(transactionRepository.getSuccessRateByDay(any(LocalDateTime.class)))
                    .thenReturn(mockData);

            // Act
            List<SuccessRateDataPoint> result = adminService.getSuccessRateByDay(7);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTotal()).isEqualTo(10L);
            assertThat(result.get(0).getCompleted()).isEqualTo(8L);
            assertThat(result.get(0).getFailed()).isEqualTo(2L);
            assertThat(result.get(0).getSuccessRate()).isEqualTo(80.0);
        }
    }
}
