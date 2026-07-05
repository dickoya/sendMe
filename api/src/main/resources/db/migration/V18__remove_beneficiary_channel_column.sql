-- Migration V18 : Supprimer la colonne channel redondante de la table beneficiaries
-- La colonne channel était conservée pour compatibilité mais est maintenant obsolète
-- car chaque bénéficiaire peut avoir plusieurs canaux via beneficiary_channels

-- Supprimer la colonne channel qui est devenue redondante (si elle existe encore)
SET @exist_channel := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'beneficiaries' AND COLUMN_NAME = 'channel');
SET @sql_drop_channel = IF(@exist_channel > 0,
    'ALTER TABLE beneficiaries DROP COLUMN channel',
    'SELECT ''channel column already removed''');
PREPARE stmt FROM @sql_drop_channel;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
