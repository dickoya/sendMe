package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.response.LiveExchangeRateResponse;
import ibrahima.dickoya.sendme.dto.response.TransferEvolutionResponse;
import ibrahima.dickoya.sendme.dto.response.UserDashboardStatsResponse;
import ibrahima.dickoya.sendme.entity.Beneficiary;
import ibrahima.dickoya.sendme.entity.ExchangeRate;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.repository.ExchangeRateRepository;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour DashboardService")
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private DashboardService dashboardService;

    private User user;
    private Transaction transaction;
    private Beneficiary beneficiary;
    private final String USER_EMAIL = "ibrahima@sendme.com";

    @BeforeEach
    void setUp() {
        beneficiary = Beneficiary.builder()
                .id(10L)
                .firstName("Mariama")
                .lastName("Diallo")
                .build();

        user = User.builder()
                .id(1L)
                .email(USER_EMAIL)
                .build();

        transaction = Transaction.builder()
                .id(100L)
                .amountSent(new BigDecimal("200.00"))
                .fees(new BigDecimal("3.00"))
                .status(TransactionStatus.COMPLETED)
                .beneficiary(beneficiary)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Tests de getStats")
    class GetStatsTests {

        @Test
        @DisplayName("getStats - Doit retourner les statistiques utilisateur")
        void getStats_ShouldReturnUserStats() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(transactionRepository.countBySenderId(1L)).thenReturn(10L);
            when(transactionRepository.countByStatus(TransactionStatus.COMPLETED)).thenReturn(8L);
            when(transactionRepository.sumAmountSentBySenderIdAndStatus(1L, TransactionStatus.COMPLETED))
                    .thenReturn(2000.0);
            when(transactionRepository.findBySenderIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(transaction)));

            // Act
            UserDashboardStatsResponse stats = dashboardService.getStats(USER_EMAIL);

            // Assert
            assertThat(stats.getTotalSent()).isEqualTo(2000.0);
            assertThat(stats.getTotalTransactions()).isEqualTo(10L);
            assertThat(stats.getCompletedTransactions()).isEqualTo(8L);
            assertThat(stats.getTotalFees()).isEqualTo(3.0);
            assertThat(stats.getTotalReceived()).isEqualTo(1997.0);
            assertThat(stats.getSavings()).isEqualTo(1.5);
            assertThat(stats.getFavoriteBeneficiary()).isNotNull();
            assertThat(stats.getFavoriteBeneficiary().getFirstName()).isEqualTo("Mariama");
            assertThat(stats.getFavoriteBeneficiary().getCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getStats - Doit lever une exception si l'utilisateur n'existe pas")
        void getStats_ShouldThrow_WhenUserNotFound() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> dashboardService.getStats(USER_EMAIL))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Utilisateur non trouvé");
        }

        @Test
        @DisplayName("getStats - Doit retourner null pour le bénéficiaire favori si aucun")
        void getStats_ShouldReturnNullFavorite_WhenNoBeneficiary() {
            // Arrange
            Transaction txWithoutBeneficiary = Transaction.builder()
                    .id(101L)
                    .amountSent(new BigDecimal("100.00"))
                    .fees(new BigDecimal("2.00"))
                    .status(TransactionStatus.COMPLETED)
                    .beneficiary(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(transactionRepository.countBySenderId(1L)).thenReturn(1L);
            when(transactionRepository.countByStatus(TransactionStatus.COMPLETED)).thenReturn(1L);
            when(transactionRepository.sumAmountSentBySenderIdAndStatus(1L, TransactionStatus.COMPLETED))
                    .thenReturn(100.0);
            when(transactionRepository.findBySenderIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(txWithoutBeneficiary)));

            // Act
            UserDashboardStatsResponse stats = dashboardService.getStats(USER_EMAIL);

            // Assert
            assertThat(stats.getFavoriteBeneficiary()).isNull();
        }
    }

    @Nested
    @DisplayName("Tests de getTransferEvolution")
    class GetTransferEvolutionTests {

        @Test
        @DisplayName("getTransferEvolution - Doit retourner l'évolution mensuelle et annuelle")
        void getTransferEvolution_ShouldReturnMonthlyAndYearly() {
            // Arrange
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(transactionRepository.findBySenderIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(transaction)));

            // Act
            TransferEvolutionResponse evolution = dashboardService.getTransferEvolution(USER_EMAIL);

            // Assert
            assertThat(evolution.getMonthly()).isNotEmpty();
            assertThat(evolution.getYearly()).isNotEmpty();
            assertThat(evolution.getMonthly().get(0).getCount()).isEqualTo(1L);
            assertThat(evolution.getMonthly().get(0).getTotalAmount()).isEqualTo(200.0);
        }
    }

    @Nested
    @DisplayName("Tests de getLiveExchangeRates")
    class GetLiveExchangeRatesTests {

        @Test
        @DisplayName("getLiveExchangeRates - Doit retourner les taux en direct")
        void getLiveExchangeRates_ShouldReturnRates() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF")).thenReturn(new BigDecimal("650.000000"));
            when(exchangeRateService.getRawRate("EUR", "XOF")).thenReturn(new BigDecimal("655.000000"));
            when(exchangeRateService.getCacheTimestamp("EUR")).thenReturn("2025-07-05T20:00:00");

            // Act
            List<LiveExchangeRateResponse> rates = dashboardService.getLiveExchangeRates();

            // Assert
            assertThat(rates).isNotEmpty();
            LiveExchangeRateResponse eurXof = rates.stream()
                    .filter(r -> "EUR".equals(r.getFrom()) && "XOF".equals(r.getTo()))
                    .findFirst().orElse(null);
            assertThat(eurXof).isNotNull();
            assertThat(eurXof.getRate()).isEqualTo(650.0);
            assertThat(eurXof.getRawRate()).isEqualTo(655.0);
            assertThat(eurXof.getTimestamp()).isEqualTo("2025-07-05T20:00:00");
        }
    }

    @Nested
    @DisplayName("Tests de getExchangeRateHistory")
    class GetExchangeRateHistoryTests {

        @Test
        @DisplayName("getExchangeRateHistory - Doit retourner l'historique des taux")
        void getExchangeRateHistory_ShouldReturnHistory() {
            // Arrange
            ExchangeRate rate = ExchangeRate.builder()
                    .baseCurrency("EUR")
                    .targetCurrency("XOF")
                    .marginRate(new BigDecimal("650.000000"))
                    .fetchedAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(exchangeRateRepository
                    .findByBaseCurrencyAndTargetCurrencyAndFetchedAtBetweenOrderByFetchedAtDesc(
                            eq("EUR"), eq("XOF"), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(List.of(rate));

            // Act
            List<LiveExchangeRateResponse.ExchangeRateHistoryPoint> history = dashboardService
                    .getExchangeRateHistory("EUR", "XOF");

            // Assert
            assertThat(history).hasSize(1);
            assertThat(history.get(0).getRate()).isEqualTo(650.0);
        }
    }
}
