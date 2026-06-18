-- ============================================================
-- V12 : Système de parrainage (referral) avec coupons de réduction
-- ============================================================

-- 1. Ajout du code de parrainage et du parrain à la table users
ALTER TABLE users
    ADD COLUMN referral_code VARCHAR(20) NOT NULL UNIQUE,
    ADD COLUMN referred_by_id BIGINT NULL,
    ADD INDEX idx_referral_code (referral_code);

ALTER TABLE users
    ADD CONSTRAINT fk_referred_by FOREIGN KEY (referred_by_id) REFERENCES users(id);

-- 2. Table des coupons de réduction générés par le parrainage
CREATE TABLE referral_coupons (
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
