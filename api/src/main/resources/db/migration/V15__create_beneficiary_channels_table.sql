-- V15__create_beneficiary_channels_table.sql
-- Remplace les tables bank_details et mobile_money_details par une table unique
-- permettant à un bénéficiaire d'avoir plusieurs canaux de réception.
--
-- Chaque canal peut être :
--   - BANK_TRANSFER : nécessite bank_name, iban, bic
--   - Mobile Money  : nécessite phone_number, country_code
--
-- country_code détermine la devise de réception (ex: GIN → GNF, SEN → XOF, FRA → EUR)

CREATE TABLE beneficiary_channels (
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

-- Index pour retrouver rapidement les canaux d'un bénéficiaire
CREATE INDEX idx_beneficiary_channels_beneficiary_id ON beneficiary_channels(beneficiary_id);

-- Migrer les données existantes de bank_details vers beneficiary_channels
INSERT INTO beneficiary_channels (beneficiary_id, channel, bank_name, iban, bic, is_primary, created_at, updated_at)
SELECT
    bd.beneficiary_id,
    'BANK_TRANSFER',
    bd.bank_name,
    bd.iban,
    bd.bic,
    TRUE,
    b.created_at,
    b.updated_at
FROM bank_details bd
JOIN beneficiaries b ON b.id = bd.beneficiary_id
WHERE b.channel = 'BANK_TRANSFER';

-- Migrer les données existantes de mobile_money_details vers beneficiary_channels
INSERT INTO beneficiary_channels (beneficiary_id, channel, phone_number, is_primary, created_at, updated_at)
SELECT
    mmd.beneficiary_id,
    b.channel,
    mmd.phone_number,
    TRUE,
    b.created_at,
    b.updated_at
FROM mobile_money_details mmd
JOIN beneficiaries b ON b.id = mmd.beneficiary_id
WHERE b.channel != 'BANK_TRANSFER';
