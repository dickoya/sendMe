package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Récupère toutes les notifications d'un utilisateur triées par date
     * décroissante.
     * Version sans pagination (pour markAllAsRead).
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Récupère les notifications d'un utilisateur avec pagination.
     * Version paginée pour améliorer les performances.
     */
    // @EntityGraph : charge la transaction liée pour éviter la
    // LazyInitializationException hors transaction (open-in-view=false).
    @EntityGraph(attributePaths = "transaction")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Compte le nombre de notifications non lues d'un utilisateur.
     */
    Long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Marque toutes les notifications non lues d'un utilisateur comme lues.
     * Utilise une requête UPDATE SQL directe pour améliorer les performances.
     * 
     * @param userId identifiant de l'utilisateur
     * @return nombre de notifications modifiées
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
