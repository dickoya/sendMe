-- ============================================================
-- Migration V13 : Support multi-devises avancé
-- Ajoute les colonnes nécessaires pour gérer GBP, USD, CAD,
-- CHF, NGN, GHS, ZAR, MAD, TND, XAF dans les transactions.
-- ============================================================

-- 1. Ajout de la colonne currency_sent_symbol (symbole monétaire)
--    pour un affichage plus riche dans l'UI
SET @exist_sent_symbol := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND COLUMN_NAME = 'currency_sent_symbol');
SET @sql_sent_symbol = IF(@exist_sent_symbol = 0,
    'ALTER TABLE transactions ADD COLUMN currency_sent_symbol VARCHAR(5) DEFAULT NULL',
    'SELECT ''currency_sent_symbol already exists''');
PREPARE stmt FROM @sql_sent_symbol;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ajout de la colonne currency_received_symbol
SET @exist_recv_symbol := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND COLUMN_NAME = 'currency_received_symbol');
SET @sql_recv_symbol = IF(@exist_recv_symbol = 0,
    'ALTER TABLE transactions ADD COLUMN currency_received_symbol VARCHAR(5) DEFAULT NULL',
    'SELECT ''currency_received_symbol already exists''');
PREPARE stmt FROM @sql_recv_symbol;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Ajout de la colonne sender_country_currency
--    pour tracer depuis quelle devise l'utilisateur a envoyé
SET @exist_sender_currency := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND COLUMN_NAME = 'sender_country_currency');
SET @sql_sender_currency = IF(@exist_sender_currency = 0,
    'ALTER TABLE transactions ADD COLUMN sender_country_currency VARCHAR(3) DEFAULT NULL',
    'SELECT ''sender_country_currency already exists''');
PREPARE stmt FROM @sql_sender_currency;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Ajout d'un index sur les colonnes de devises pour les recherches
--    fréquentes (filtrage par devise dans le dashboard admin)
SET @exist_idx_sent := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND INDEX_NAME = 'idx_transactions_currency_sent');
SET @sql_idx_sent = IF(@exist_idx_sent = 0,
    'CREATE INDEX idx_transactions_currency_sent ON transactions (currency_sent)',
    'SELECT ''idx_transactions_currency_sent already exists''');
PREPARE stmt FROM @sql_idx_sent;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_idx_recv := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND INDEX_NAME = 'idx_transactions_currency_received');
SET @sql_idx_recv = IF(@exist_idx_recv = 0,
    'CREATE INDEX idx_transactions_currency_received ON transactions (currency_received)',
    'SELECT ''idx_transactions_currency_received already exists''');
PREPARE stmt FROM @sql_idx_recv;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Mise à jour des transactions existantes avec les symboles
UPDATE transactions
SET currency_sent_symbol = CASE currency_sent
    WHEN 'EUR' THEN '€'
    WHEN 'USD' THEN '$'
    WHEN 'GBP' THEN '£'
    WHEN 'CAD' THEN 'C$'
    WHEN 'CHF' THEN 'CHF'
    ELSE currency_sent
END;

UPDATE transactions
SET currency_received_symbol = CASE currency_received
    WHEN 'XOF' THEN 'CFA'
    WHEN 'XAF' THEN 'CFA'
    WHEN 'GNF' THEN 'FG'
    WHEN 'NGN' THEN '₦'
    WHEN 'GHS' THEN '₵'
    WHEN 'ZAR' THEN 'R'
    WHEN 'MAD' THEN 'DH'
    WHEN 'TND' THEN 'DT'
    ELSE currency_received
END;
