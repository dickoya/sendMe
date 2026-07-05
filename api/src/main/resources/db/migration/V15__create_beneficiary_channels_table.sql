-- V15__create_beneficiary_channels_table.sql
-- Remplace les tables bank_details et mobile_money_details par une table unique
-- permettant à un bénéficiaire d'avoir plusieurs canaux de réception.
--
-- Chaque canal peut être :
--   - BANK_TRANSFER : nécessite bank_name, iban, bic
--   - Mobile Money  : nécessite phone_number, country_code
--
-- country_code détermine la devise de réception (ex: GIN → GNF, SEN → XOF, FRA → EUR)

CREATE TABLE IF NOT EXISTS beneficiary_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    beneficiary_id BIGINT NOT NULL,
    channel ENUM('BANK_TRANSFER', 'ORANGE_MONEY', 'MTN_MOBILE_MONEY', 'WAVE', 'MOOV_MONEY', 'AIRTEL_MONEY') NOT NULL,
    phone_number VARCHAR(20) COMMENT 'Numéro de téléphone pour les canaux Mobile Money',
    country_code VARCHAR(3) COMMENT 'Code pays ISO 3166-1 alpha-3 (ex: GIN, SEN, CIV, FRA)',
    bank_name VARCHAR(150) COMMENT 'Nom de la banque pour BANK_TRANSFER',
    iban VARCHAR(34) COMMENT 'IBAN pour BANK_TRANSFER',
    bic VARCHAR(11) COMMENT 'BIC/SWIFT pour BANK_TRANSFER',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Canal de réception par défaut',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_channel_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id) ON DELETE CASCADE
);

-- Index pour retrouver rapidement les canaux d'un bénéficiaire (si pas déjà présent)
SET @exist_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'beneficiary_channels' AND INDEX_NAME = 'idx_beneficiary_channels_beneficiary_id');
SET @sql_idx = IF(@exist_idx = 0,
    'CREATE INDEX idx_beneficiary_channels_beneficiary_id ON beneficiary_channels(beneficiary_id)',
    'SELECT ''idx_beneficiary_channels_beneficiary_id already exists''');
PREPARE stmt FROM @sql_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Migrer les données existantes de bank_details vers beneficiary_channels (si la table source existe)
SET @exist_bank_details := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bank_details');
SET @sql_migrate_bank = IF(@exist_bank_details > 0,
    'INSERT INTO beneficiary_channels (beneficiary_id, channel, bank_name, iban, bic, is_primary, created_at, updated_at) SELECT bd.beneficiary_id, ''BANK_TRANSFER'', bd.bank_name, bd.iban, bd.bic, TRUE, b.created_at, b.updated_at FROM bank_details bd JOIN beneficiaries b ON b.id = bd.beneficiary_id WHERE b.channel = ''BANK_TRANSFER''',
    'SELECT ''bank_details table does not exist, skipping migration''');
PREPARE stmt FROM @sql_migrate_bank;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Migrer les données existantes de mobile_money_details vers beneficiary_channels (si la table source existe)
SET @exist_mmd := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mobile_money_details');
SET @sql_migrate_mmd = IF(@exist_mmd > 0,
    'INSERT INTO beneficiary_channels (beneficiary_id, channel, phone_number, is_primary, created_at, updated_at) SELECT mmd.beneficiary_id, b.channel, mmd.phone_number, TRUE, b.created_at, b.updated_at FROM mobile_money_details mmd JOIN beneficiaries b ON b.id = mmd.beneficiary_id WHERE b.channel != ''BANK_TRANSFER''',
    'SELECT ''mobile_money_details table does not exist, skipping migration''');
PREPARE stmt FROM @sql_migrate_mmd;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
