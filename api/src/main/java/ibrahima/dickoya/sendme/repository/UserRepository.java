package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    /**
     * Compte les utilisateurs dont le compte est actif.
     * Utilisé pour les statistiques du tableau de bord admin.
     *
     * @return nombre d'utilisateurs actifs
     */
    Long countByIsActiveTrue();

    /**
     * Recherche un utilisateur par email avec son rôle.
     * Utilisé par l'admin pour vérifier les droits.
     *
     * @param email email de l'utilisateur
     * @return Optional<User>
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Récupère tous les utilisateurs avec un compte actif.
     * Utilisé pour envoyer les notifications de masse
     * (maintenance, changement de tarifs) uniquement aux
     * comptes actifs — inutile de notifier les comptes désactivés.
     *
     * @return liste des utilisateurs actifs
     */
    List<User> findByIsActiveTrue();
}
