-- Ajout des colonnes pour la validation par email lors de l'inscription
-- Un utilisateur doit vérifier son email avant de pouvoir se connecter

-- Ajouter email_verified si elle n'existe pas déjà
SET @exist_email_verified := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email_verified');
SET @sql1 = IF(@exist_email_verified = 0,
    'ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT ''email_verified column already exists''');
PREPARE stmt FROM @sql1;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ajouter email_verification_token si elle n'existe pas déjà
SET @exist_token := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email_verification_token');
SET @sql2 = IF(@exist_token = 0,
    'ALTER TABLE users ADD COLUMN email_verification_token VARCHAR(255) UNIQUE',
    'SELECT ''email_verification_token column already exists''');
PREPARE stmt FROM @sql2;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ajouter email_verification_token_expires_at si elle n'existe pas déjà
SET @exist_expires := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email_verification_token_expires_at');
SET @sql3 = IF(@exist_expires = 0,
    'ALTER TABLE users ADD COLUMN email_verification_token_expires_at TIMESTAMP',
    'SELECT ''email_verification_token_expires_at column already exists''');
PREPARE stmt FROM @sql3;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
