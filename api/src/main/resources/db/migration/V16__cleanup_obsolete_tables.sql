-- ============================================================
-- Migration V16 : Nettoyage des tables et colonnes obsolètes
--
-- Supprime :
--   - bank_details           (remplacée par beneficiary_channels en V15)
--   - mobile_money_details   (remplacée par beneficiary_channels en V15)
--
-- Supprime les colonnes inutilisées de transactions :
--   - currency_sent_symbol
--   - currency_received_symbol
--   - sender_country_currency
-- ============================================================

-- 1. Supprimer les tables remplacées par beneficiary_channels
DROP TABLE IF EXISTS bank_details;
DROP TABLE IF EXISTS mobile_money_details;

-- 2. Supprimer les colonnes inutilisées de transactions
SET @exist_sent_symbol := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND COLUMN_NAME = 'currency_sent_symbol');
SET @sql_drop_sent = IF(@exist_sent_symbol > 0,
    'ALTER TABLE transactions DROP COLUMN currency_sent_symbol',
    'SELECT ''currency_sent_symbol already removed''');
PREPARE stmt FROM @sql_drop_sent;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_recv_symbol := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND COLUMN_NAME = 'currency_received_symbol');
SET @sql_drop_recv = IF(@exist_recv_symbol > 0,
    'ALTER TABLE transactions DROP COLUMN currency_received_symbol',
    'SELECT ''currency_received_symbol already removed''');
PREPARE stmt FROM @sql_drop_recv;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_sender_currency := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND COLUMN_NAME = 'sender_country_currency');
SET @sql_drop_sender = IF(@exist_sender_currency > 0,
    'ALTER TABLE transactions DROP COLUMN sender_country_currency',
    'SELECT ''sender_country_currency already removed''');
PREPARE stmt FROM @sql_drop_sender;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
