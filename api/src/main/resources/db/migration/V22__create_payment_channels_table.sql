-- Table de gestion dynamique des canaux de paiement
-- Permet à l'admin d'activer/désactiver et d'ajouter des canaux
CREATE TABLE IF NOT EXISTS payment_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_type VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    icon VARCHAR(10) DEFAULT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insérer les 6 canaux existants comme actifs par défaut (ignorer si déjà présents)
INSERT IGNORE INTO payment_channels (channel_type, display_name, icon, is_active) VALUES
    ('BANK_TRANSFER',   'Virement bancaire', '🏦', TRUE),
    ('ORANGE_MONEY',    'Orange Money',      '🟠', TRUE),
    ('MTN_MOBILE_MONEY','MTN Mobile Money',  '🟡', TRUE),
    ('WAVE',            'Wave',              '🔵', TRUE),
    ('MOOV_MONEY',      'Moov Money',        '🟢', TRUE),
    ('AIRTEL_MONEY',    'Airtel Money',      '🔴', TRUE);
