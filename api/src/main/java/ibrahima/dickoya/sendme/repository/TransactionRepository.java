package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);
    Optional<Transaction> findByReference(String reference);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Optional<Transaction> findByStripePaymentIntentId(String stripePaymentIntentId);
    /**
     * Récupère les transactions filtrées par statut pour un utilisateur.
     * Utilisé par l'endpoint de l'historique avec filtre.
     *
     * @param senderId identifiant de l'expéditeur
     * @param status   statut à filtrer
     * @param pageable paramètres de pagination
     * @return Page de transactions filtrées
     */
    Page<Transaction> findBySenderIdAndStatusOrderByCreatedAtDesc(
            Long senderId,
            TransactionStatus status,
            Pageable pageable);

    /**
     * Retrouve une transaction par sa référence unique.
     * Utilisé pour la génération du reçu PDF.
     *
     * @param reference la référence (ex: TXN-20250609-0001)
     * @return Optional<Transaction>
     */

}