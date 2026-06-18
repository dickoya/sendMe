package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /**
     * Récupère l'historique de connexion d'un utilisateur,
     * trié par date décroissante (le plus récent en premier).
     */
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    /**
     * Récupère les N dernières tentatives de connexion d'un utilisateur.
     */
    List<LoginHistory> findTop10ByUserIdOrderByLoginTimeDesc(Long userId);
}
