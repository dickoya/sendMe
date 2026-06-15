-- ============================================================
-- V10 : Création de la table exchange_rates
-- Stocke l'historique des taux de change récupérés depuis
-- l'API OpenExchangeRates pour audit et conformité.
-- ============================================================

CREATE TABLE IF NOT EXISTS exchange_rates (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_currency VARCHAR(3)   NOT NULL COMMENT 'Devise de base (ex: EUR, USD)',
    target_currency VARCHAR(3) NOT NULL COMMENT 'Devise cible (ex: XOF, GNF)',
    raw_rate    DECIMAL(18, 10) NOT NULL COMMENT 'Taux brut récupéré depuis l API',
    margin_rate DECIMAL(18, 10) NOT NULL COMMENT 'Taux avec marge interne appliquée',
    margin_pct  DECIMAL(5, 2)  NOT NULL COMMENT 'Pourcentage de marge appliqué',
    fetched_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de récupération',
    api_status  VARCHAR(20)    NOT NULL DEFAULT 'SUCCESS' COMMENT 'Statut de l appel API (SUCCESS / FALLBACK)',
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_exchange_rates_base_target (base_currency, target_currency),
    INDEX idx_exchange_rates_fetched_at (fetched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Historique des taux de change pour audit et conformité';
