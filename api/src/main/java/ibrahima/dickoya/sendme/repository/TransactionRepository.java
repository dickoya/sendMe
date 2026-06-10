package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.Transaction;
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
}