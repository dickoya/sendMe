package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.dto.response.ReferralCodeResponse;
import ibrahima.dickoya.sendme.dto.response.ReferralCouponResponse;
import ibrahima.dickoya.sendme.entity.ReferralCoupon;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.exception.ResourceNotFoundException;
import ibrahima.dickoya.sendme.repository.ReferralCouponRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service gérant le système de parrainage SendMe.
 * <p>
 * Fonctionnement :
 * <ul>
 * <li>Chaque utilisateur reçoit un code de parrainage unique à
 * l'inscription</li>
 * <li>Quand un filleul s'inscrit avec un code, on lie les deux comptes</li>
 * <li>Au premier transfert du filleul, des coupons de réduction sont générés :
 * - 5€ pour le filleul (utilisable sur son prochain transfert)
 * - 5€ pour le parrain (utilisable sur son prochain transfert)</li>
 * <li>Les coupons expirent après 30 jours</li>
 * <li>L'utilisateur peut choisir d'appliquer ou non un coupon lors de la
 * simulation</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private static final BigDecimal REFERRAL_DISCOUNT = new BigDecimal("5.00");
    private static final int COUPON_EXPIRY_DAYS = 30;
    private static final int REFERRAL_CODE_LENGTH = 12;

    private final UserRepository userRepository;
    private final ReferralCouponRepository couponRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    // ── Génération du code de parrainage ──────────────────────────────────

    /**
     * Génère un code de parrainage unique et non-prédictible pour un utilisateur.
     * <p>
     * Sécurité :
     * <ul>
     * <li>Utilisation de {@link SecureRandom} (CSPRNG) — pas de
     * {@link java.util.Random}</li>
     * <li>12 caractères aléatoires → 36¹² ≈ 4,7 × 10¹⁸ combinaisons</li>
     * <li>Préfixe fixe "SENDME-" pour la lisibilité</li>
     * <li>Vérification d'unicité en base avant validation</li>
     * </ul>
     * Format : SENDME-XXXXXXXXXXXX (ex: SENDME-A7K9M2X4P1QR)
     */
    public String generateReferralCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code;
        do {
            code = new StringBuilder("SENDME-");
            for (int i = 0; i < REFERRAL_CODE_LENGTH; i++) {
                code.append(chars.charAt(secureRandom.nextInt(chars.length())));
            }
        } while (userRepository.existsByReferralCode(code.toString()));
        return code.toString();
    }

    /**
     * Vérifie si un code de parrainage existe et est valide.
     * Utile pour valider le code côté frontend avant la soumission du formulaire
     * d'inscription.
     *
     * @param referralCode le code à vérifier
     * @return true si le code existe et correspond à un utilisateur actif
     */
    @Transactional(readOnly = true)
    public boolean isValidReferralCode(String referralCode) {
        if (referralCode == null || referralCode.isBlank()) {
            return false;
        }
        return userRepository.findByReferralCode(referralCode.trim())
                .map(User::getIsActive)
                .orElse(false);
    }

    // ── Application du code de parrainage à l'inscription ─────────────────

    /**
     * Lie un nouvel utilisateur à son parrain via le code de parrainage.
     * Appelé après la création du compte.
     * <p>
     * Crée immédiatement un coupon de réduction pour le filleul (REFERRED)
     * afin qu'il puisse l'utiliser dès son premier transfert.
     * Le coupon du parrain (SPONSOR) sera créé lors du premier transfert
     * du filleul via {@link #rewardFirstTransfer(User, Transaction)}.
     *
     * @param newUser      l'utilisateur qui vient de s'inscrire
     * @param referralCode le code de parrainage saisi (peut être null)
     */
    @Transactional
    public void applyReferralCode(User newUser, String referralCode) {
        if (referralCode == null || referralCode.isBlank()) {
            return;
        }

        Optional<User> sponsorOpt = userRepository.findByReferralCode(referralCode.trim());
        if (sponsorOpt.isEmpty()) {
            log.warn("[PARRAINAGE] Code invalide saisi par {} : {}", newUser.getEmail(), referralCode);
            return;
        }

        User sponsor = sponsorOpt.get();

        // Anti-fraude : un utilisateur ne peut pas se parrainer lui-même
        if (sponsor.getId().equals(newUser.getId())) {
            log.warn("[PARRAINAGE] Tentative d'auto-parrainage de {}", newUser.getEmail());
            return;
        }

        // Lier le filleul au parrain
        newUser.setReferredBy(sponsor);
        userRepository.save(newUser);

        // Créer immédiatement le coupon de réduction pour le filleul
        // afin qu'il puisse l'utiliser dès son premier transfert
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(COUPON_EXPIRY_DAYS);
        ReferralCoupon referredCoupon = ReferralCoupon.builder()
                .user(newUser)
                .referredUser(newUser)
                .couponType("REFERRED")
                .discountAmount(REFERRAL_DISCOUNT)
                .currency("EUR")
                .isUsed(false)
                .expiresAt(expiresAt)
                .build();
        couponRepository.save(referredCoupon);

        log.info("[PARRAINAGE] {} a été parrainé par {} (code: {}) — coupon filleul de {} EUR créé",
                newUser.getEmail(), sponsor.getEmail(), referralCode, REFERRAL_DISCOUNT);
    }

    // ── Génération des coupons au premier transfert du filleul ────────────

    /**
     * Vérifie si l'utilisateur est un filleul et génère les coupons
     * de réduction pour son premier transfert réussi.
     * <p>
     * Appelé par TransferService après un transfert COMPLETED.
     * <p>
     * Depuis la création du coupon filleul à l'inscription, cette méthode
     * ne crée plus que le coupon du parrain (SPONSOR) si ce n'est pas déjà fait.
     *
     * @param user        l'expéditeur du transfert
     * @param transaction la transaction complétée
     */
    @Transactional
    public void rewardFirstTransfer(User user, Transaction transaction) {
        // Vérifier que l'utilisateur a été parrainé
        if (user.getReferredBy() == null) {
            return;
        }

        User sponsor = user.getReferredBy();
        String currency = transaction.getCurrencySent() != null ? transaction.getCurrencySent() : "EUR";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(COUPON_EXPIRY_DAYS);

        // Vérifier si le parrain a déjà reçu son coupon pour ce filleul
        long existingSponsorCoupons = couponRepository.countByUserIdAndCouponType(sponsor.getId(), "SPONSOR");
        if (existingSponsorCoupons > 0) {
            log.debug("[PARRAINAGE] {} a déjà reçu son coupon parrain, ignoré", sponsor.getEmail());
            return;
        }

        // Coupon pour le parrain (SPONSOR) — réduction sur son prochain transfert
        ReferralCoupon sponsorCoupon = ReferralCoupon.builder()
                .user(sponsor)
                .referredUser(user)
                .couponType("SPONSOR")
                .discountAmount(REFERRAL_DISCOUNT)
                .currency(currency)
                .isUsed(false)
                .expiresAt(expiresAt)
                .build();
        couponRepository.save(sponsorCoupon);

        log.info("[PARRAINAGE] Coupon parrain généré pour {} — {} {}",
                sponsor.getEmail(), REFERRAL_DISCOUNT, currency);
    }

    // ── Récupération des coupons disponibles ──────────────────────────────

    /**
     * Récupère le coupon valide le plus ancien pour un utilisateur et une devise.
     *
     * @param userId   l'ID de l'utilisateur
     * @param currency la devise du transfert
     * @return un coupon valide s'il existe
     */
    @Transactional(readOnly = true)
    public Optional<ReferralCoupon> getValidCoupon(Long userId, String currency) {
        return couponRepository.findFirstValidByUserIdAndCurrency(userId, currency);
    }

    /**
     * Récupère tous les coupons valides d'un utilisateur.
     */
    @Transactional(readOnly = true)
    public List<ReferralCoupon> getValidCoupons(Long userId) {
        return couponRepository.findValidCouponsByUserId(userId);
    }

    // ── Utilisation d'un coupon ───────────────────────────────────────────

    /**
     * Marque un coupon comme utilisé et le lie à une transaction.
     *
     * @param couponId    l'ID du coupon
     * @param transaction la transaction associée
     */
    @Transactional
    public void useCoupon(Long couponId, Transaction transaction) {
        ReferralCoupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon introuvable : " + couponId));

        if (coupon.getIsUsed()) {
            log.warn("[PARRAINAGE] Tentative d'utilisation d'un coupon déjà utilisé : {}", couponId);
            return;
        }

        coupon.setIsUsed(true);
        coupon.setUsedAt(LocalDateTime.now());
        coupon.setTransaction(transaction);
        couponRepository.save(coupon);

        log.info("[PARRAINAGE] Coupon {} utilisé sur la transaction {}",
                couponId, transaction.getReference());
    }

    // ── Statistiques de parrainage ────────────────────────────────────────

    /**
     * Récupère les informations de parrainage d'un utilisateur.
     */
    @Transactional(readOnly = true)
    public ReferralCodeResponse getReferralInfo(User user) {
        long totalReferred = couponRepository.countByUserIdAndCouponType(user.getId(), "SPONSOR");
        long totalCoupons = couponRepository.countByUserIdAndIsUsedTrue(user.getId());
        List<ReferralCoupon> validCoupons = couponRepository.findValidCouponsByUserId(user.getId());

        String shareLink = "https://sendme.app/register?ref=" + user.getReferralCode();

        return ReferralCodeResponse.builder()
                .referralCode(user.getReferralCode())
                .shareLink(shareLink)
                .totalReferred(totalReferred)
                .totalCouponsEarned(totalCoupons)
                .availableCoupons(validCoupons.size())
                .build();
    }

    /**
     * Récupère la liste de tous les coupons d'un utilisateur
     * (valides, utilisés et expirés), triés par date de création décroissante.
     */
    @Transactional(readOnly = true)
    public List<ReferralCouponResponse> getUserCoupons(Long userId) {
        List<ReferralCoupon> coupons = couponRepository.findAllByUserId(userId);
        return coupons.stream()
                .map(this::toCouponResponse)
                .toList();
    }

    // ── Montant de la réduction ───────────────────────────────────────────

    /**
     * Calcule le montant de réduction applicable pour un utilisateur et une devise.
     * Retourne 0 si aucun coupon valide n'est disponible.
     */
    @Transactional(readOnly = true)
    public BigDecimal getDiscountAmount(Long userId, String currency) {
        return couponRepository.findFirstValidByUserIdAndCurrency(userId, currency)
                .map(ReferralCoupon::getDiscountAmount)
                .orElse(BigDecimal.ZERO);
    }

    // ── Mappers ───────────────────────────────────────────────────────────

    private ReferralCouponResponse toCouponResponse(ReferralCoupon coupon) {
        String referredName = coupon.getReferredUser().getFirstName()
                + " " + coupon.getReferredUser().getLastName();

        return ReferralCouponResponse.builder()
                .id(coupon.getId())
                .couponType(coupon.getCouponType())
                .discountAmount(coupon.getDiscountAmount())
                .currency(coupon.getCurrency())
                .isUsed(coupon.getIsUsed())
                .expiresAt(coupon.getExpiresAt())
                .usedAt(coupon.getUsedAt())
                .referredUserName(referredName)
                .transactionId(coupon.getTransaction() != null ? coupon.getTransaction().getId() : null)
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
