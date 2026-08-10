package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.entity.ExchangeRate;
import ibrahima.dickoya.sendme.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour ExchangeRateService")
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        // Injecter l'appId et le RestTemplate mocké via ReflectionTestUtils
        ReflectionTestUtils.setField(exchangeRateService, "appId", "test-app-id");
    }

    @Nested
    @DisplayName("Tests de getRate")
    class GetRateTests {

        @Test
        @DisplayName("getRate - Doit retourner 1 si les devises sont identiques")
        void getRate_ShouldReturnOne_WhenSameCurrency() {
            // Act
            BigDecimal rate = exchangeRateService.getRate("EUR", "EUR");

            // Assert
            assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("getRate - Doit être insensible à la casse")
        void getRate_ShouldBeCaseInsensitive() {
            // Act
            BigDecimal rate = exchangeRateService.getRate("eur", "EUR");

            // Assert
            assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("getRate - Doit retourner 1:1 si l'API est indisponible et pas de fallback BDD")
        void getRate_ShouldReturnOne_WhenApiFailsAndNoFallback() {
            // Act - le cache est vide, fetchRates retournera null (API down)
            BigDecimal rate = exchangeRateService.getRate("EUR", "XOF");

            // Assert - fallback BDD null aussi → retour 1:1
            assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("getRate - Doit utiliser le fallback BDD si l'API est indisponible")
        void getRate_ShouldUseFallback_WhenApiFails() {
            // Arrange
            ExchangeRate lastRate = ExchangeRate.builder()
                    .marginRate(new BigDecimal("650.000000"))
                    .build();
            when(exchangeRateRepository
                    .findFirstByBaseCurrencyAndTargetCurrencyOrderByFetchedAtDesc("EUR", "XOF"))
                    .thenReturn(Optional.of(lastRate));

            // Act - le cache est vide, fetchRates retournera null
            BigDecimal rate = exchangeRateService.getRate("EUR", "XOF");

            // Assert
            assertThat(rate).isEqualByComparingTo(new BigDecimal("650.000000"));
        }
    }

    @Nested
    @DisplayName("Tests de getRawRate et getMarginRate")
    class RawAndMarginRateTests {

        @Test
        @DisplayName("getRawRate - Doit retourner null si le cache est vide et API down")
        void getRawRate_ShouldReturnNull_WhenNoData() {
            // Act
            BigDecimal rawRate = exchangeRateService.getRawRate("EUR", "XOF");

            // Assert
            assertThat(rawRate).isNull();
        }

        @Test
        @DisplayName("getMarginRate - Doit retourner null si le cache est vide et API down")
        void getMarginRate_ShouldReturnNull_WhenNoData() {
            // Act
            BigDecimal marginRate = exchangeRateService.getMarginRate("EUR", "XOF");

            // Assert
            assertThat(marginRate).isNull();
        }

        @Test
        @DisplayName("getRawRate - Doit retourner 1 si les devises sont identiques")
        void getRawRate_ShouldReturnOne_WhenSameCurrency() {
            // Act
            BigDecimal rate = exchangeRateService.getRawRate("EUR", "EUR");

            // Assert
            assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        }

        @Test
        @DisplayName("getMarginRate - Doit retourner 1 si les devises sont identiques")
        void getMarginRate_ShouldReturnOne_WhenSameCurrency() {
            // Act
            BigDecimal rate = exchangeRateService.getMarginRate("EUR", "EUR");

            // Assert
            assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
        }
    }

    @Nested
    @DisplayName("Tests de getMarginPercentage et getCacheTimestamp")
    class UtilityMethodTests {

        @Test
        @DisplayName("getMarginPercentage - Doit retourner 2.00")
        void getMarginPercentage_ShouldReturnTwoPercent() {
            // Act
            BigDecimal margin = exchangeRateService.getMarginPercentage();

            // Assert
            assertThat(margin).isEqualByComparingTo(new BigDecimal("2.00"));
        }

        @Test
        @DisplayName("getCacheTimestamp - Doit retourner 'Aucun cache disponible' si pas de cache")
        void getCacheTimestamp_ShouldReturnNoCache_WhenNotCached() {
            // Act
            String timestamp = exchangeRateService.getCacheTimestamp("EUR");

            // Assert
            assertThat(timestamp).isEqualTo("Aucun cache disponible");
        }
    }

}


