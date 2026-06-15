package ibrahima.dickoya.sendme.repository;

import ibrahima.dickoya.sendme.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité ExchangeRate.
 * Permet de stocker et consulter l'historique des taux de change
 * pour l'audit, la conformité et le dashboard admin.
 */
@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    /**
     * Récupère le dernier taux enregistré pour un couple de devises.
     * Utilisé pour le fallback si l'API est indisponible.
     */
    Optional<ExchangeRate> findFirstByBaseCurrencyAndTargetCurrencyOrderByFetchedAtDesc(
            String baseCurrency, String targetCurrency);

    /**
     * Récupère l'historique des taux pour un couple de devises
     * sur une période donnée.
     */
    List<ExchangeRate> findByBaseCurrencyAndTargetCurrencyAndFetchedAtBetweenOrderByFetchedAtDesc(
            String baseCurrency, String targetCurrency,
            LocalDateTime start, LocalDateTime end);

    /**
     * Récupère tous les taux récents (pour le dashboard admin).
     */
    List<ExchangeRate> findTop50ByOrderByFetchedAtDesc();
}
