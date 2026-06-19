package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.ReferralCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReferralCouponRepository extends JpaRepository<ReferralCoupon, Long> {

        /**
         * Trouve tous les coupons valides (non utilisés et non expirés) d'un
         * utilisateur.
         */
        @Query("""
                        SELECT c FROM ReferralCoupon c
                        WHERE c.user.id = :userId
                          AND c.isUsed = false
                          AND c.expiresAt > CURRENT_TIMESTAMP
                        ORDER BY c.createdAt DESC
                        """)
        List<ReferralCoupon> findValidCouponsByUserId(@Param("userId") Long userId);

        /**
         * Trouve un coupon valide pour un utilisateur et une devise donnée.
         */
        @Query("""
                        SELECT c FROM ReferralCoupon c
                        WHERE c.user.id = :userId
                          AND c.currency = :currency
                          AND c.isUsed = false
                          AND c.expiresAt > CURRENT_TIMESTAMP
                        ORDER BY c.createdAt ASC
                        """)
        Optional<ReferralCoupon> findFirstValidByUserIdAndCurrency(
                        @Param("userId") Long userId,
                        @Param("currency") String currency);

        /**
         * Compte le nombre de coupons déjà utilisés par un utilisateur.
         */
        long countByUserIdAndIsUsedTrue(Long userId);

        /**
         * Compte le nombre de filleuls (coupons SPONSOR générés).
         */
        long countByUserIdAndCouponType(Long userId, String couponType);

        /**
         * Trouve tous les coupons d'un utilisateur (valides, utilisés et expirés),
         * triés par date de création décroissante.
         */
        @Query("""
                        SELECT c FROM ReferralCoupon c
                        WHERE c.user.id = :userId
                        ORDER BY c.createdAt DESC
                        """)
        List<ReferralCoupon> findAllByUserId(@Param("userId") Long userId);
}
