package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.response.ReferralCodeResponse;
import ibrahima.dickoya.sendme.dto.response.ReferralCouponResponse;
import ibrahima.dickoya.sendme.entity.ReferralCoupon;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.ReferralCouponRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour ReferralService")
class ReferralServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReferralCouponRepository couponRepository;

    @InjectMocks
    private ReferralService referralService;

    private User user;
    private User sponsor;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("filleul@sendme.com")
                .firstName("Filleul")
                .lastName("Test")
                .referralCode("SENDME-USER1234567")
                .isActive(true)
                .build();

        sponsor = User.builder()
                .id(2L)
                .email("parrain@sendme.com")
                .firstName("Parrain")
                .lastName("Test")
                .referralCode("SENDME-SPONSOR1234")
                .isActive(true)
                .build();

        transaction = Transaction.builder()
                .id(100L)
                .reference("SENDME-REF-001")
                .currencySent("EUR")
                .build();
    }

    @Nested
    @DisplayName("Tests de generateReferralCode")
    class GenerateReferralCodeTests {

        @Test
        @DisplayName("generateReferralCode - Doit générer un code commençant par SENDME-")
        void generateReferralCode_ShouldStartWithSendMePrefix() {
            // Arrange
            when(userRepository.existsByReferralCode(anyString())).thenReturn(false);

            // Act
            String code = referralService.generateReferralCode();

            // Assert
            assertThat(code).startsWith("SENDME-");
            assertThat(code).hasSize(19); // "SENDME-" (7) + 12 chars
        }

        @Test
        @DisplayName("generateReferralCode - Doit générer un code unique même après collision")
        void generateReferralCode_ShouldRetryOnCollision() {
            // Arrange
            when(userRepository.existsByReferralCode(anyString()))
                    .thenReturn(true) // première collision
                    .thenReturn(false); // deuxième tentative réussie

            // Act
            String code = referralService.generateReferralCode();

            // Assert
            assertThat(code).startsWith("SENDME-");
            verify(userRepository, times(2)).existsByReferralCode(anyString());
        }
    }

    @Nested
    @DisplayName("Tests de isValidReferralCode")
    class IsValidReferralCodeTests {

        @Test
        @DisplayName("isValidReferralCode - Doit retourner true pour un code valide")
        void isValidReferralCode_ShouldReturnTrue_WhenValid() {
            // Arrange
            when(userRepository.findByReferralCode("SENDME-USER1234567"))
                    .thenReturn(Optional.of(user));

            // Act
            boolean result = referralService.isValidReferralCode("SENDME-USER1234567");

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isValidReferralCode - Doit retourner false pour un code null")
        void isValidReferralCode_ShouldReturnFalse_WhenNull() {
            // Act
            boolean result = referralService.isValidReferralCode(null);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isValidReferralCode - Doit retourner false pour un code vide")
        void isValidReferralCode_ShouldReturnFalse_WhenBlank() {
            // Act
            boolean result = referralService.isValidReferralCode("   ");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isValidReferralCode - Doit retourner false pour un code inexistant")
        void isValidReferralCode_ShouldReturnFalse_WhenNotFound() {
            // Arrange
            when(userRepository.findByReferralCode("INEXISTANT")).thenReturn(Optional.empty());

            // Act
            boolean result = referralService.isValidReferralCode("INEXISTANT");

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("isValidReferralCode - Doit retourner false si l'utilisateur est inactif")
        void isValidReferralCode_ShouldReturnFalse_WhenUserInactive() {
            // Arrange
            user.setIsActive(false);
            when(userRepository.findByReferralCode("SENDME-INACTIF")).thenReturn(Optional.of(user));

            // Act
            boolean result = referralService.isValidReferralCode("SENDME-INACTIF");

            // Assert
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests de applyReferralCode")
    class ApplyReferralCodeTests {

        @Test
        @DisplayName("applyReferralCode - Doit lier le filleul au parrain et créer un coupon")
        void applyReferralCode_ShouldLinkAndCreateCoupon() {
            // Arrange
            when(userRepository.findByReferralCode("SENDME-SPONSOR1234"))
                    .thenReturn(Optional.of(sponsor));

            ReferralCoupon savedCoupon = ReferralCoupon.builder()
                    .id(1L)
                    .user(user)
                    .referredUser(user)
                    .couponType("REFERRED")
                    .discountAmount(new BigDecimal("5.00"))
                    .currency("EUR")
                    .isUsed(false)
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .build();

            when(couponRepository.save(any(ReferralCoupon.class))).thenReturn(savedCoupon);

            // Act
            referralService.applyReferralCode(user, "SENDME-SPONSOR1234");

            // Assert
            assertThat(user.getReferredBy()).isEqualTo(sponsor);
            verify(userRepository).save(user);
            verify(couponRepository).save(any(ReferralCoupon.class));
        }

        @Test
        @DisplayName("applyReferralCode - Ne doit rien faire si le code est null")
        void applyReferralCode_ShouldDoNothing_WhenCodeIsNull() {
            // Act
            referralService.applyReferralCode(user, null);

            // Assert
            verify(userRepository, never()).findByReferralCode(anyString());
            verify(userRepository, never()).save(any());
            verify(couponRepository, never()).save(any());
        }

        @Test
        @DisplayName("applyReferralCode - Ne doit rien faire si le code est vide")
        void applyReferralCode_ShouldDoNothing_WhenCodeIsBlank() {
            // Act
            referralService.applyReferralCode(user, "   ");

            // Assert
            verify(userRepository, never()).findByReferralCode(anyString());
        }

        @Test
        @DisplayName("applyReferralCode - Ne doit rien faire si le code est invalide")
        void applyReferralCode_ShouldDoNothing_WhenCodeInvalid() {
            // Arrange
            when(userRepository.findByReferralCode("INVALIDE")).thenReturn(Optional.empty());

            // Act
            referralService.applyReferralCode(user, "INVALIDE");

            // Assert
            assertThat(user.getReferredBy()).isNull();
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("applyReferralCode - Ne doit pas permettre l'auto-parrainage")
        void applyReferralCode_ShouldPreventSelfReferral() {
            // Arrange
            when(userRepository.findByReferralCode("SENDME-USER1234567"))
                    .thenReturn(Optional.of(user)); // même utilisateur

            // Act
            referralService.applyReferralCode(user, "SENDME-USER1234567");

            // Assert
            assertThat(user.getReferredBy()).isNull();
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests de rewardFirstTransfer")
    class RewardFirstTransferTests {

        @Test
        @DisplayName("rewardFirstTransfer - Doit créer un coupon SPONSOR pour le parrain")
        void rewardFirstTransfer_ShouldCreateSponsorCoupon() {
            // Arrange
            user.setReferredBy(sponsor);
            when(couponRepository.countByUserIdAndCouponType(sponsor.getId(), "SPONSOR"))
                    .thenReturn(0L);

            // Act
            referralService.rewardFirstTransfer(user, transaction);

            // Assert
            verify(couponRepository).save(argThat(coupon -> coupon.getCouponType().equals("SPONSOR") &&
                    coupon.getUser().equals(sponsor) &&
                    coupon.getReferredUser().equals(user) &&
                    coupon.getDiscountAmount().compareTo(new BigDecimal("5.00")) == 0 &&
                    !coupon.getIsUsed()));
        }

        @Test
        @DisplayName("rewardFirstTransfer - Ne doit rien faire si l'utilisateur n'a pas de parrain")
        void rewardFirstTransfer_ShouldDoNothing_WhenNoSponsor() {
            // Arrange - user n'a pas de referredBy
            // Act
            referralService.rewardFirstTransfer(user, transaction);

            // Assert
            verify(couponRepository, never()).save(any());
        }

        @Test
        @DisplayName("rewardFirstTransfer - Ne doit pas créer de doublon si le parrain a déjà son coupon")
        void rewardFirstTransfer_ShouldNotDuplicate_WhenSponsorAlreadyHasCoupon() {
            // Arrange
            user.setReferredBy(sponsor);
            when(couponRepository.countByUserIdAndCouponType(sponsor.getId(), "SPONSOR"))
                    .thenReturn(1L); // déjà un coupon existant

            // Act
            referralService.rewardFirstTransfer(user, transaction);

            // Assert
            verify(couponRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests de getValidCoupon et getValidCoupons")
    class GetValidCouponsTests {

        @Test
        @DisplayName("getValidCoupon - Doit retourner un coupon valide")
        void getValidCoupon_ShouldReturnValidCoupon() {
            // Arrange
            ReferralCoupon coupon = ReferralCoupon.builder().id(1L).build();
            when(couponRepository.findFirstValidByUserIdAndCurrency(1L, "EUR"))
                    .thenReturn(Optional.of(coupon));

            // Act
            Optional<ReferralCoupon> result = referralService.getValidCoupon(1L, "EUR");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getValidCoupons - Doit retourner la liste des coupons valides")
        void getValidCoupons_ShouldReturnValidCoupons() {
            // Arrange
            ReferralCoupon coupon1 = ReferralCoupon.builder().id(1L).build();
            ReferralCoupon coupon2 = ReferralCoupon.builder().id(2L).build();
            when(couponRepository.findValidCouponsByUserId(1L))
                    .thenReturn(List.of(coupon1, coupon2));

            // Act
            List<ReferralCoupon> result = referralService.getValidCoupons(1L);

            // Assert
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Tests de useCoupon")
    class UseCouponTests {

        @Test
        @DisplayName("useCoupon - Doit marquer le coupon comme utilisé")
        void useCoupon_ShouldMarkCouponAsUsed() {
            // Arrange
            ReferralCoupon coupon = ReferralCoupon.builder()
                    .id(1L)
                    .isUsed(false)
                    .build();

            when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

            // Act
            referralService.useCoupon(1L, transaction);

            // Assert
            assertThat(coupon.getIsUsed()).isTrue();
            assertThat(coupon.getUsedAt()).isNotNull();
            assertThat(coupon.getTransaction()).isEqualTo(transaction);
            verify(couponRepository).save(coupon);
        }

        @Test
        @DisplayName("useCoupon - Ne doit rien faire si le coupon est déjà utilisé")
        void useCoupon_ShouldDoNothing_WhenAlreadyUsed() {
            // Arrange
            ReferralCoupon coupon = ReferralCoupon.builder()
                    .id(1L)
                    .isUsed(true)
                    .build();

            when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

            // Act
            referralService.useCoupon(1L, transaction);

            // Assert
            verify(couponRepository, never()).save(any());
        }

        @Test
        @DisplayName("useCoupon - Doit lever une exception si le coupon n'existe pas")
        void useCoupon_ShouldThrow_WhenCouponNotFound() {
            // Arrange
            when(couponRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> referralService.useCoupon(999L, transaction))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Coupon introuvable");
        }
    }

    @Nested
    @DisplayName("Tests de getReferralInfo")
    class GetReferralInfoTests {

        @Test
        @DisplayName("getReferralInfo - Doit retourner les infos de parrainage")
        void getReferralInfo_ShouldReturnReferralInfo() {
            // Arrange
            user.setReferralCode("SENDME-ABC123");
            when(couponRepository.countByUserIdAndCouponType(1L, "SPONSOR")).thenReturn(3L);
            when(couponRepository.countByUserIdAndIsUsedTrue(1L)).thenReturn(5L);
            when(couponRepository.findValidCouponsByUserId(1L)).thenReturn(List.of(
                    ReferralCoupon.builder().id(1L).build()));

            // Act
            ReferralCodeResponse response = referralService.getReferralInfo(user);

            // Assert
            assertThat(response.getReferralCode()).isEqualTo("SENDME-ABC123");
            assertThat(response.getShareLink()).contains("sendme.app/register?ref=");
            assertThat(response.getTotalReferred()).isEqualTo(3L);
            assertThat(response.getTotalCouponsEarned()).isEqualTo(5L);
            assertThat(response.getAvailableCoupons()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Tests de getDiscountAmount")
    class GetDiscountAmountTests {

        @Test
        @DisplayName("getDiscountAmount - Doit retourner le montant de réduction")
        void getDiscountAmount_ShouldReturnDiscount() {
            // Arrange
            ReferralCoupon coupon = ReferralCoupon.builder()
                    .discountAmount(new BigDecimal("5.00"))
                    .build();
            when(couponRepository.findFirstValidByUserIdAndCurrency(1L, "EUR"))
                    .thenReturn(Optional.of(coupon));

            // Act
            BigDecimal discount = referralService.getDiscountAmount(1L, "EUR");

            // Assert
            assertThat(discount).isEqualByComparingTo(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("getDiscountAmount - Doit retourner 0 si aucun coupon valide")
        void getDiscountAmount_ShouldReturnZero_WhenNoCoupon() {
            // Arrange
            when(couponRepository.findFirstValidByUserIdAndCurrency(1L, "EUR"))
                    .thenReturn(Optional.empty());

            // Act
            BigDecimal discount = referralService.getDiscountAmount(1L, "EUR");

            // Assert
            assertThat(discount).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Tests de getUserCoupons")
    class GetUserCouponsTests {

        @Test
        @DisplayName("getUserCoupons - Doit retourner la liste des coupons")
        void getUserCoupons_ShouldReturnCoupons() {
            // Arrange
            ReferralCoupon coupon = ReferralCoupon.builder()
                    .id(1L)
                    .couponType("REFERRED")
                    .discountAmount(new BigDecimal("5.00"))
                    .currency("EUR")
                    .isUsed(false)
                    .expiresAt(LocalDateTime.now().plusDays(30))
                    .referredUser(user)
                    .build();

            when(couponRepository.findAllByUserId(1L)).thenReturn(List.of(coupon));

            // Act
            List<ReferralCouponResponse> result = referralService.getUserCoupons(1L);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCouponType()).isEqualTo("REFERRED");
            assertThat(result.get(0).getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
            assertThat(result.get(0).getReferredUserName()).isEqualTo("Filleul Test");
        }
    }
}
