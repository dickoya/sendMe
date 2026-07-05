-- ============================================================
-- V12 : Système de parrainage (referral) avec coupons de réduction
-- ============================================================

-- 1. Ajout du code de parrainage et du parrain à la table users (si colonnes n'existent pas déjà)
SET @exist_referral_code := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'referral_code');
SET @sql_referral_code = IF(@exist_referral_code = 0,
    'ALTER TABLE users ADD COLUMN referral_code VARCHAR(20) NOT NULL UNIQUE',
    'SELECT ''referral_code already exists''');
PREPARE stmt FROM @sql_referral_code;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exist_referred_by := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'referred_by_id');
SET @sql_referred_by = IF(@exist_referred_by = 0,
    'ALTER TABLE users ADD COLUMN referred_by_id BIGINT NULL',
    'SELECT ''referred_by_id already exists''');
PREPARE stmt FROM @sql_referred_by;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Index (si pas déjà présent)
SET @exist_idx := (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND INDEX_NAME = 'idx_referral_code');
SET @sql_idx = IF(@exist_idx = 0,
    'CREATE INDEX idx_referral_code ON users(referral_code)',
    'SELECT ''idx_referral_code already exists''');
PREPARE stmt FROM @sql_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- FK (si pas déjà présente)
SET @exist_fk := (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND CONSTRAINT_NAME = 'fk_referred_by');
SET @sql_fk = IF(@exist_fk = 0,
    'ALTER TABLE users ADD CONSTRAINT fk_referred_by FOREIGN KEY (referred_by_id) REFERENCES users(id)',
    'SELECT ''fk_referred_by already exists''');
PREPARE stmt FROM @sql_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Table des coupons de réduction générés par le parrainage
CREATE TABLE IF NOT EXISTS referral_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    referred_user_id BIGINT NOT NULL,
    coupon_type VARCHAR(10) NOT NULL COMMENT 'SPONSOR = pour le parrain, REFERRED = pour le filleul',
    discount_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    transaction_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_coupon_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_coupon_referred FOREIGN KEY (referred_user_id) REFERENCES users(id),
    CONSTRAINT fk_coupon_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    INDEX idx_coupon_user_used (user_id, is_used)
);
