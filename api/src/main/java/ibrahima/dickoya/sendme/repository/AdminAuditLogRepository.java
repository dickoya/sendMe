package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository pour les logs d'audit des actions administrateurs.
 * <p>
 * Supporte le filtrage par action, admin, et période.
 */
@Repository
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

        /**
         * Recherche paginée avec filtres optionnels.
         * Si un paramètre est null, le filtre correspondant est ignoré.
         */
        @Query("""
                        SELECT a FROM AdminAuditLog a
                        WHERE (:action IS NULL OR a.action = :action)
                          AND (:adminEmail IS NULL OR LOWER(a.adminEmail) LIKE LOWER(CONCAT('%', :adminEmail, '%')))
                          AND (:dateFrom IS NULL OR a.createdAt >= :dateFrom)
                          AND (:dateTo IS NULL OR a.createdAt <= :dateTo)
                        ORDER BY a.createdAt DESC
                        """)
        Page<AdminAuditLog> findFiltered(
                        @Param("action") String action,
                        @Param("adminEmail") String adminEmail,
                        @Param("dateFrom") LocalDateTime dateFrom,
                        @Param("dateTo") LocalDateTime dateTo,
                        Pageable pageable);

        /**
         * Liste des actions distinctes pour le filtre.
         */
        @Query("SELECT DISTINCT a.action FROM AdminAuditLog a ORDER BY a.action")
        java.util.List<String> findDistinctActions();
}
