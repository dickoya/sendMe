package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.request.SimulateRequest;
import ibrahima.dickoya.sendme.dto.response.SimulateResponse;
import ibrahima.dickoya.sendme.entity.ReferralCoupon;
import ibrahima.dickoya.sendme.enums.ChannelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour SimulatorService")
class SimulatorServiceTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private ReferralService referralService;

    @InjectMocks
    private SimulatorService simulatorService;

    private SimulateRequest.SimulateRequestBuilder requestBuilder;

    @BeforeEach
    void setUp() {
        requestBuilder = SimulateRequest.builder()
                .amount(new BigDecimal("200.00"))
                .currencyFrom("EUR")
                .currencyTo("XOF")
                .channel(ChannelType.ORANGE_MONEY);
    }

    @Nested
    @DisplayName("Tests de simulate sans userId")
    class SimulateWithoutUserTests {

        @Test
        @DisplayName("simulate - Doit calculer les frais à 1.5% du montant")
        void simulate_ShouldCalculateFees() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request);

            // Assert
            assertThat(response.getFees()).isEqualByComparingTo(new BigDecimal("3.00")); // 200 * 1.5%
            assertThat(response.getFeeRate()).isEqualByComparingTo(new BigDecimal("1.5"));
        }

        @Test
        @DisplayName("simulate - Doit calculer le total débité (montant + frais)")
        void simulate_ShouldCalculateTotalDebited() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request);

            // Assert
            assertThat(response.getTotalDebited()).isEqualByComparingTo(new BigDecimal("203.00")); // 200 + 3
        }

        @Test
        @DisplayName("simulate - Doit calculer le montant reçu avec le taux de change")
        void simulate_ShouldCalculateAmountReceived() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request);

            // Assert
            assertThat(response.getAmountReceived()).isEqualByComparingTo(new BigDecimal("131191.40")); // 200 * 655.957
        }

        @Test
        @DisplayName("simulate - Doit retourner un discount à 0 quand pas de userId")
        void simulate_ShouldReturnZeroDiscount_WhenNoUserId() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request);

            // Assert
            assertThat(response.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(referralService, never()).getValidCoupon(anyLong(), anyString());
        }

        @Test
        @DisplayName("simulate - Doit retourner les devises correctement")
        void simulate_ShouldReturnCorrectCurrencies() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request);

            // Assert
            assertThat(response.getCurrencyFrom()).isEqualTo("EUR");
            assertThat(response.getCurrencyTo()).isEqualTo("XOF");
            assertThat(response.getAmountSent()).isEqualByComparingTo(new BigDecimal("200.00"));
        }
    }

    @Nested
    @DisplayName("Tests de simulate avec userId")
    class SimulateWithUserTests {

        private final Long USER_ID = 1L;

        @Test
        @DisplayName("simulate - Doit appliquer la réduction quand un coupon valide existe")
        void simulate_ShouldApplyDiscount_WhenValidCouponExists() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            ReferralCoupon coupon = ReferralCoupon.builder()
                    .discountAmount(new BigDecimal("5.00"))
                    .build();
            when(referralService.getValidCoupon(USER_ID, "EUR"))
                    .thenReturn(Optional.of(coupon));

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request, USER_ID);

            // Assert
            assertThat(response.getDiscount()).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(response.getTotalDebited()).isEqualByComparingTo(new BigDecimal("198.00")); // 203 - 5
        }

        @Test
        @DisplayName("simulate - Ne doit pas appliquer de réduction si aucun coupon valide")
        void simulate_ShouldNotApplyDiscount_WhenNoValidCoupon() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));
            when(referralService.getValidCoupon(USER_ID, "EUR"))
                    .thenReturn(Optional.empty());

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request, USER_ID);

            // Assert
            assertThat(response.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getTotalDebited()).isEqualByComparingTo(new BigDecimal("203.00"));
        }

        @Test
        @DisplayName("simulate - Ne doit pas appliquer la réduction si applyDiscount est false")
        void simulate_ShouldNotApplyDiscount_WhenApplyDiscountIsFalse() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            SimulateRequest request = requestBuilder
                    .applyDiscount(false)
                    .build();

            // Act
            SimulateResponse response = simulatorService.simulate(request, USER_ID);

            // Assert
            assertThat(response.getDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(referralService, never()).getValidCoupon(anyLong(), anyString());
        }

        @Test
        @DisplayName("simulate - La réduction ne doit pas dépasser le total débité")
        void simulate_ShouldNotExceedTotalDebited_WhenDiscountIsLarger() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            // Coupon de 1000€ alors que le total débité n'est que de 203€
            ReferralCoupon coupon = ReferralCoupon.builder()
                    .discountAmount(new BigDecimal("1000.00"))
                    .build();
            when(referralService.getValidCoupon(USER_ID, "EUR"))
                    .thenReturn(Optional.of(coupon));

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request, USER_ID);

            // Assert
            assertThat(response.getDiscount()).isEqualByComparingTo(new BigDecimal("203.00")); // plafonné
            assertThat(response.getTotalDebited()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Tests de updateFeeRate et getCurrentFeeRate")
    class FeeRateTests {

        @Test
        @DisplayName("updateFeeRate - Doit mettre à jour le taux de commission")
        void updateFeeRate_ShouldUpdateFeeRate() {
            // Act
            simulatorService.updateFeeRate(new BigDecimal("2.0"));

            // Assert
            assertThat(simulatorService.getCurrentFeeRate())
                    .isEqualByComparingTo(new BigDecimal("0.02"));
        }

        @Test
        @DisplayName("updateFeeRate - Doit affecter les nouvelles simulations")
        void updateFeeRate_ShouldAffectNewSimulations() {
            // Arrange
            when(exchangeRateService.getRate("EUR", "XOF"))
                    .thenReturn(new BigDecimal("655.957000"));

            simulatorService.updateFeeRate(new BigDecimal("3.0"));

            SimulateRequest request = requestBuilder.build();

            // Act
            SimulateResponse response = simulatorService.simulate(request);

            // Assert
            assertThat(response.getFees()).isEqualByComparingTo(new BigDecimal("6.00")); // 200 * 3%
            assertThat(response.getFeeRate()).isEqualByComparingTo(new BigDecimal("3.0"));
        }

        @Test
        @DisplayName("getCurrentFeeRate - Doit retourner 1.5% par défaut")
        void getCurrentFeeRate_ShouldReturnDefault() {
            // Act
            BigDecimal rate = simulatorService.getCurrentFeeRate();

            // Assert
            assertThat(rate).isEqualByComparingTo(new BigDecimal("0.015"));
        }
    }
}
