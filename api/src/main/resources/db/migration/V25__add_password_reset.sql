-- Migration V25: Ajout des colonnes de réinitialisation de mot de passe
-- Permet à un utilisateur de demander un lien de réinitialisation de mot de passe
-- (token unique + date d'expiration).

ALTER TABLE users
    ADD COLUMN reset_password_token VARCHAR(255) NULL,
    ADD COLUMN reset_password_token_expires_at DATETIME NULL,
    ADD UNIQUE INDEX idx_users_reset_password_token (reset_password_token);
