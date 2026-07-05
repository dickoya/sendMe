package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.PaymentChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion dynamique des canaux de paiement.
 */
@Repository
public interface PaymentChannelRepository extends JpaRepository<PaymentChannel, Long> {

    /** Récupère uniquement les canaux actifs (pour les utilisateurs). */
    List<PaymentChannel> findByIsActiveTrue();

    /** Vérifie si un type de canal existe déjà. */
    boolean existsByChannelType(String channelType);
}
