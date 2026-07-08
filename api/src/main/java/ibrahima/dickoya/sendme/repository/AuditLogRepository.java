package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository pour les logs d'audit centralisés.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Tous les logs triés par date décroissante. */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Filtre par catégorie. */
    Page<AuditLog> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);

    /** Filtre par type d'événement. */
    Page<AuditLog> findByEventTypeOrderByCreatedAtDesc(String eventType, Pageable pageable);

    /** Filtre par email utilisateur. */
    Page<AuditLog> findByUserEmailOrderByCreatedAtDesc(String userEmail, Pageable pageable);

    /** Filtre par statut. */
    Page<AuditLog> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    /** Filtre combiné avancé. */
    @Query("SELECT l FROM AuditLog l WHERE " +
            "(:category IS NULL OR l.category = :category) AND " +
            "(:eventType IS NULL OR l.eventType = :eventType) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:userEmail IS NULL OR l.userEmail LIKE %:userEmail%) AND " +
            "(:from IS NULL OR l.createdAt >= :from) AND " +
            "(:to IS NULL OR l.createdAt <= :to) " +
            "ORDER BY l.createdAt DESC")
    Page<AuditLog> findFiltered(
            @Param("category") String category,
            @Param("eventType") String eventType,
            @Param("status") String status,
            @Param("userEmail") String userEmail,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    /** Compte les événements par catégorie. */
    @Query("SELECT l.category, COUNT(l) FROM AuditLog l GROUP BY l.category")
    java.util.List<Object[]> countByCategory();

    /** Compte les événements par type. */
    @Query("SELECT l.eventType, COUNT(l) FROM AuditLog l GROUP BY l.eventType")
    java.util.List<Object[]> countByEventType();

    /** Compte les événements par statut. */
    @Query("SELECT l.status, COUNT(l) FROM AuditLog l GROUP BY l.status")
    java.util.List<Object[]> countByStatus();
}
