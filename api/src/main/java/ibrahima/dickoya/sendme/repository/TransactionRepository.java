package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
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
        * Compte les transactions par statut.
        * Utilisé pour les statistiques du tableau de bord.
        *
        * @param status le statut à compter
        * @return nombre de transactions avec ce statut
        */
       Long countByStatus(TransactionStatus status);

       /**
        * Calcule la somme des montants envoyés pour un statut donné.
        * Utilisé pour calculer le volume total des transactions COMPLETED.
        *
        * @param status le statut (généralement COMPLETED)
        * @return somme des amountSent ou null si aucune transaction
        */
       @Query("SELECT SUM(t.amountSent) FROM Transaction t WHERE t.status = :status")
       Double sumAmountSentByStatus(@Param("status") TransactionStatus status);

       /**
        * Calcule la somme des frais collectés pour un statut donné.
        *
        * @param status le statut
        * @return somme des fees ou null si aucune transaction
        */
       @Query("SELECT SUM(t.fees) FROM Transaction t WHERE t.status = :status")
       Double sumFeesByStatus(@Param("status") TransactionStatus status);

       /**
        * Calcule le volume transféré depuis une date donnée.
        * Utilisé pour les statistiques quotidiennes.
        *
        * @param date   date de début
        * @param status statut des transactions à inclure
        * @return volume total ou null
        */
       @Query("SELECT SUM(t.amountSent) FROM Transaction t " +
                     "WHERE t.createdAt >= :date AND t.status = :status")
       Double sumAmountSentAfterDate(
                     @Param("date") LocalDateTime date,
                     @Param("status") TransactionStatus status);

       /**
        * Compte les transactions créées après une date donnée.
        * Utilisé pour les statistiques quotidiennes.
        *
        * @param date date de début
        * @return nombre de transactions
        */
       Long countByCreatedAtAfter(LocalDateTime date);

       /**
        * Retourne la répartition des transactions par canal.
        * Utilisé pour le graphique du tableau de bord admin.
        *
        * Retourne une liste de tableaux [nomCanal, nombreTransactions].
        *
        * @return liste des résultats groupés par canal
        */
       @Query("SELECT t.channel, COUNT(t) FROM Transaction t GROUP BY t.channel")
       List<Object[]> countByChannel();

       /**
        * Compte les transactions d'un expéditeur spécifique.
        * Utilisé pour les statistiques par utilisateur.
        *
        * @param senderId identifiant de l'expéditeur
        * @return nombre de transactions
        */
       Long countBySenderId(Long senderId);

       /**
        * Calcule le montant total envoyé par un expéditeur.
        *
        * @param senderId identifiant de l'expéditeur
        * @param status   statut des transactions à inclure
        * @return montant total ou null
        */
       @Query("SELECT SUM(t.amountSent) FROM Transaction t " +
                     "WHERE t.sender.id = :senderId AND t.status = :status")
       Double sumAmountSentBySenderIdAndStatus(
                     @Param("senderId") Long senderId,
                     @Param("status") TransactionStatus status);

       /**
        * Récupère toutes les transactions triées par date décroissante.
        * Version paginée pour l'admin.
        *
        * @param pageable paramètres de pagination
        * @return Page de transactions
        */
       Page<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

       /**
        * Récupère les transactions filtrées par statut.
        *
        * @param status   statut à filtrer
        * @param pageable paramètres de pagination
        * @return Page de transactions filtrées
        */
       Page<Transaction> findByStatusOrderByCreatedAtDesc(
                     TransactionStatus status, Pageable pageable);

       /**
        * Retourne la valeur numérique maximale du suffixe des références.
        * Utilisé pour initialiser le compteur de références au redémarrage.
        * Exemple : pour TXN-20250618-0042, extrait 42 et retourne le max.
        * 
        * Compatible MySQL et H2 : utilise SUBSTRING avec LOCATE au lieu de
        * SUBSTRING_INDEX qui est spécifique à MySQL.
        * Utilise INTEGER au lieu de UNSIGNED (non supporté par H2).
        */
       @Query(value = "SELECT MAX(CAST(SUBSTRING(t.reference, LOCATE('-', t.reference, LOCATE('-', t.reference) + 1) + 1) AS INTEGER)) FROM transactions t", nativeQuery = true)
       Long findMaxReferenceSequence();

       // ── Agrégations pour le dashboard admin ────────────────────────────

       /**
        * Volume total par jour pour les X derniers jours.
        * Retourne une liste de tableaux [date, volume, count].
        */
       @Query("""
                     SELECT FUNCTION('DATE', t.createdAt) as date,
                            SUM(t.amountSent) as volume,
                            COUNT(t) as cnt
                     FROM Transaction t
                     WHERE t.status = 'COMPLETED'
                       AND t.createdAt >= :since
                     GROUP BY FUNCTION('DATE', t.createdAt)
                     ORDER BY FUNCTION('DATE', t.createdAt) ASC
                     """)
       List<Object[]> getVolumeByDay(@Param("since") LocalDateTime since);

       /**
        * Taux de succès par jour pour les X derniers jours.
        * Retourne une liste de tableaux [date, total, completed, failed].
        */
       @Query("""
                     SELECT FUNCTION('DATE', t.createdAt) as date,
                            COUNT(t) as total,
                            SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
                            SUM(CASE WHEN t.status = 'FAILED' THEN 1 ELSE 0 END) as failed
                     FROM Transaction t
                     WHERE t.createdAt >= :since
                     GROUP BY FUNCTION('DATE', t.createdAt)
                     ORDER BY FUNCTION('DATE', t.createdAt) ASC
                     """)
       List<Object[]> getSuccessRateByDay(@Param("since") LocalDateTime since);

}
