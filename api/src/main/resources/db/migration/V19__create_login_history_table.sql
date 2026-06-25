-- Migration V19: Création de la table login_history pour l'historique des connexions
-- Cette table stocke toutes les tentatives de connexion (réussies ou échouées)

CREATE TABLE login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NOT NULL COMMENT 'Adresse IP (support IPv4 et IPv6)',
    user_agent TEXT COMMENT 'User-Agent du navigateur',
    success BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'TRUE si connexion réussie, FALSE si échec',
    login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Date et heure de la tentative de connexion',
    
    -- Index pour optimiser les requêtes
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time DESC),
    INDEX idx_user_login_time (user_id, login_time DESC),
    
    -- Clé étrangère vers la table users
    CONSTRAINT fk_login_history_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Historique des connexions utilisateurs (authentification)';
