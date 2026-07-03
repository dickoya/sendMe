-- Ajout des colonnes pour la validation par email lors de l'inscription
-- Un utilisateur doit vérifier son email avant de pouvoir se connecter

ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN email_verification_token VARCHAR(255) UNIQUE,
    ADD COLUMN email_verification_token_expires_at TIMESTAMP;
