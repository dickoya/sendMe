package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Compte le nombre de notifications non lues d'un utilisateur.
     */
    Long countByUserIdAndIsReadFalse(Long userId);
}
