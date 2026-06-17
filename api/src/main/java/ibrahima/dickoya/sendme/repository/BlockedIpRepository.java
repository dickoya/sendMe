package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.BlockedIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BlockedIpRepository extends JpaRepository<BlockedIp, Long> {

    /**
     * Vérifie si une IP est actuellement bloquée (blocked_until > maintenant).
     */
    @Query("SELECT b FROM BlockedIp b WHERE b.ipAddress = :ip AND b.blockedUntil > :now")
    Optional<BlockedIp> findActiveBlockByIp(@Param("ip") String ipAddress, @Param("now") LocalDateTime now);

    /**
     * Supprime les blocages expirés pour nettoyer la table.
     */
    @Modifying
    @Query("DELETE FROM BlockedIp b WHERE b.blockedUntil <= :now")
    void deleteExpiredBlocks(@Param("now") LocalDateTime now);

    /**
     * Compte le nombre de blocages pour une IP sur une période donnée.
     */
    @Query("SELECT COUNT(b) FROM BlockedIp b WHERE b.ipAddress = :ip AND b.blockedAt > :since")
    long countBlocksSince(@Param("ip") String ipAddress, @Param("since") LocalDateTime since);
}
