package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.config.TestH2Config;
import ibrahima.dickoya.sendme.dto.request.RegisterRequest;
import ibrahima.dickoya.sendme.dto.response.AuthResponse;
import ibrahima.dickoya.sendme.entity.ReferralCoupon;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.repository.ReferralCouponRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test d'intégration du service de parrainage avec base H2.
 *
 * Couvre le flux complet :
 * 1. Inscription avec code de parrainage → lien filleul/parrain
 * 2. Premier transfert du filleul → génération des coupons
 * 3. Consultation et utilisation des coupons
 *
 * Les dépendances externes (EmailService) sont mockées.
 * La base de données H2 est utilisée en mode mémoire.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestH2Config.class)
class ReferralServiceIntegrationTest {

    @Autowired
    private ReferralService referralService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReferralCouponRepository couponRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private HttpServletRequest httpServletRequest;

    private User sponsor;
    private User referredUser;
    private String sponsorReferralCode;

    @BeforeEach
    void setUp() {
        // Mock HTTP pour éviter les NullPointerException dans AuthService.login
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Test-Agent");

        // Créer le parrain (sponsor)
        RegisterRequest sponsorRequest = RegisterRequest.builder()
                .firstName("Marie")
                .lastName("Sponsor")
                .email("marie.sponsor@referral-test.com")
                .password("Password123!")
                .build();
        AuthResponse sponsorResponse = authService.register(sponsorRequest);
        sponsorReferralCode = sponsorResponse.getReferralCode();

        // Récupérer l'entité User du parrain
        sponsor = userRepository.findByEmail("marie.sponsor@referral-test.com").orElseThrow();

        // Créer le filleul avec le code de parrainage
        RegisterRequest referredRequest = RegisterRequest.builder()
                .firstName("Paul")
                .lastName("Filleul")
                .email("paul.filleul@referral-test.com")
                .password("Password123!")
                .referralCode(sponsorReferralCode)
                .build();
        authService.register(referredRequest);

        // Récupérer l'entité User du filleul
        referredUser = userRepository.findByEmail("paul.filleul@referral-test.com").orElseThrow();
    }

    @Test
    @DisplayName("Inscription avec code de parrainage → lien filleul/parrain créé")
    void testReferralLinkCreated() {
        // Vérifier que le filleul est lié au parrain
        assertThat(referredUser.getReferredBy()).isNotNull();
        assertThat(referredUser.getReferredBy().getId()).isEqualTo(sponsor.getId());

        // Vérifier que le parrain n'est pas lié à un autre parrain
        assertThat(sponsor.getReferredBy()).isNull();
    }

    @Test
    @DisplayName("Inscription avec code → coupon REFERRED créé pour le filleul")
    void testReferredCouponCreated() {
        // Vérifier que le filleul a un coupon REFERRED valide
        List<ReferralCoupon> referredCoupons = couponRepository.findValidCouponsByUserId(referredUser.getId());

        assertThat(referredCoupons).isNotEmpty();
        Optional<ReferralCoupon> referredCoupon = referredCoupons.stream()
                .filter(c -> "REFERRED".equals(c.getCouponType()))
                .findFirst();

        assertThat(referredCoupon).isPresent();
        assertThat(referredCoupon.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(referredCoupon.get().getCurrency()).isEqualTo("EUR");
        assertThat(referredCoupon.get().getIsUsed()).isFalse();
        assertThat(referredCoupon.get().getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Premier transfert du filleul → coupon SPONSOR créé pour le parrain")
    void testSponsorCouponCreatedAfterFirstTransfer() {
        // Simuler un premier transfert du filleul
        Transaction transaction = Transaction.builder()
                .reference("TXN-TEST-0001")
                .amountSent(new BigDecimal("100.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("65595.70"))
                .currencyReceived("XOF")
                .exchangeRate(new BigDecimal("655.957"))
                .fees(new BigDecimal("1.50"))
                .status(TransactionStatus.COMPLETED)
                .channel(ChannelType.ORANGE_MONEY)
                .sender(referredUser)
                .build();

        referralService.rewardFirstTransfer(referredUser, transaction);

        // Vérifier que le parrain a un coupon SPONSOR
        List<ReferralCoupon> sponsorCoupons = couponRepository.findValidCouponsByUserId(sponsor.getId());

        assertThat(sponsorCoupons).isNotEmpty();
        Optional<ReferralCoupon> sponsorCoupon = sponsorCoupons.stream()
                .filter(c -> "SPONSOR".equals(c.getCouponType()))
                .findFirst();

        assertThat(sponsorCoupon).isPresent();
        assertThat(sponsorCoupon.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(sponsorCoupon.get().getCurrency()).isEqualTo("EUR");
        assertThat(sponsorCoupon.get().getIsUsed()).isFalse();
        assertThat(sponsorCoupon.get().getReferredUser().getId()).isEqualTo(referredUser.getId());
    }

    @Test
    @DisplayName("Récupération des coupons valides pour un utilisateur")
    void testGetValidCoupons() {
        // Le filleul doit avoir 1 coupon REFERRED valide
        List<ReferralCoupon> referredCoupons = referralService.getValidCoupons(referredUser.getId());
        assertThat(referredCoupons).hasSize(1);
        assertThat(referredCoupons.get(0).getCouponType()).isEqualTo("REFERRED");

        // Le parrain n'a pas encore de coupon (pas de premier transfert)
        List<ReferralCoupon> sponsorCoupons = referralService.getValidCoupons(sponsor.getId());
        assertThat(sponsorCoupons).isEmpty();
    }

    @Test
    @DisplayName("Récupération du coupon valide par devise")
    void testGetValidCouponByCurrency() {
        // Le filleul a un coupon en EUR
        Optional<ReferralCoupon> coupon = referralService.getValidCoupon(referredUser.getId(), "EUR");
        assertThat(coupon).isPresent();
        assertThat(coupon.get().getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));

        // Pas de coupon en USD
        Optional<ReferralCoupon> noCoupon = referralService.getValidCoupon(referredUser.getId(), "USD");
        assertThat(noCoupon).isEmpty();
    }

    @Test
    @DisplayName("Utilisation d'un coupon → marqué comme utilisé")
    void testUseCoupon() {
        // Récupérer le coupon du filleul
        Optional<ReferralCoupon> coupon = referralService.getValidCoupon(referredUser.getId(), "EUR");
        assertThat(coupon).isPresent();

        // Créer une transaction pour lier au coupon
        Transaction transaction = Transaction.builder()
                .reference("TXN-TEST-0002")
                .amountSent(new BigDecimal("50.00"))
                .currencySent("EUR")
                .amountReceived(new BigDecimal("32797.85"))
                .currencyReceived("XOF")
                .exchangeRate(new BigDecimal("655.957"))
                .fees(new BigDecimal("0.75"))
                .status(TransactionStatus.COMPLETED)
                .channel(ChannelType.ORANGE_MONEY)
                .sender(referredUser)
                .build();

        // Utiliser le coupon
        referralService.useCoupon(coupon.get().getId(), transaction);

        // Vérifier que le coupon est marqué comme utilisé
        ReferralCoupon usedCoupon = couponRepository.findById(coupon.get().getId()).orElseThrow();
        assertThat(usedCoupon.getIsUsed()).isTrue();
        assertThat(usedCoupon.getUsedAt()).isNotNull();
        assertThat(usedCoupon.getTransaction()).isNotNull();
        assertThat(usedCoupon.getTransaction().getId()).isEqualTo(transaction.getId());

        // Vérifier que le coupon n'est plus dans les valides
        List<ReferralCoupon> validCoupons = referralService.getValidCoupons(referredUser.getId());
        assertThat(validCoupons).isEmpty();
    }

    @Test
    @DisplayName("Montant de réduction disponible pour un utilisateur et une devise")
    void testGetDiscountAmount() {
        // Le filleul a 5€ de réduction en EUR
        BigDecimal discount = referralService.getDiscountAmount(referredUser.getId(), "EUR");
        assertThat(discount).isEqualByComparingTo(new BigDecimal("5.00"));

        // Pas de réduction en USD
        BigDecimal noDiscount = referralService.getDiscountAmount(referredUser.getId(), "USD");
        assertThat(noDiscount).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Vérification de code de parrainage valide")
    void testIsValidReferralCode() {
        // Le code du parrain est valide
        assertThat(referralService.isValidReferralCode(sponsorReferralCode)).isTrue();

        // Un code inexistant est invalide
        assertThat(referralService.isValidReferralCode("SENDME-INVALIDCODE")).isFalse();

        // Un code null est invalide
        assertThat(referralService.isValidReferralCode(null)).isFalse();

        // Un code vide est invalide
        assertThat(referralService.isValidReferralCode("")).isFalse();
    }

    @Test
    @DisplayName("Informations de parrainage complètes")
    void testGetReferralInfo() {
        var referralInfo = referralService.getReferralInfo(sponsor);

        assertThat(referralInfo).isNotNull();
        assertThat(referralInfo.getReferralCode()).isEqualTo(sponsorReferralCode);
        assertThat(referralInfo.getShareLink()).contains(sponsorReferralCode);
        assertThat(referralInfo.getTotalReferred()).isEqualTo(0); // Pas encore de premier transfert
        assertThat(referralInfo.getAvailableCoupons()).isEqualTo(0); // Pas de coupon pour le parrain
    }

    @Test
    @DisplayName("Liste de tous les coupons d'un utilisateur")
    void testGetUserCoupons() {
        // Le filleul doit avoir 1 coupon
        var coupons = referralService.getUserCoupons(referredUser.getId());
        assertThat(coupons).hasSize(1);
        assertThat(coupons.get(0).getCouponType()).isEqualTo("REFERRED");
        assertThat(coupons.get(0).getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(coupons.get(0).isUsed()).isFalse();
    }
}
