package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByReferralCode(String referralCode);

    Boolean existsByReferralCode(String referralCode);

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

    /**
     * Recherche un utilisateur par son token de vérification email.
     * Utilisé lors de la validation de l'email après inscription.
     *
     * @param emailVerificationToken le token de vérification unique
     * @return Optional<User>
     */
    Optional<User> findByEmailVerificationToken(String emailVerificationToken);

    // Recherche et filtres pour l'admin

    /**
     * Recherche paginée d'utilisateurs avec filtres combinés.
     * Tous les paramètres sont optionnels (null = pas de filtre).
     *
     * @param search   recherche textuelle sur nom, prénom, email (LIKE)
     * @param isActive filtre par statut (null = tous)
     * @param role     filtre par rôle (null = tous)
     * @param dateFrom date d'inscription minimum (null = pas de limite)
     * @param dateTo   date d'inscription maximum (null = pas de limite)
     * @param pageable paramètres de pagination et tri
     * @return page d'utilisateurs filtrée
     */
    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL
                   OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:isActive IS NULL OR u.isActive = :isActive)
              AND (:role IS NULL OR u.role = :role)
              AND (:dateFrom IS NULL OR u.createdAt >= :dateFrom)
              AND (:dateTo IS NULL OR u.createdAt <= :dateTo)
            """)
    Page<User> findFiltered(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            @Param("role") Role role,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
}
