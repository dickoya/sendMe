-- Migration V17 : Ajouter la colonne discount aux transactions
-- Pour stocker le montant de réduction appliqué (coupon de parrainage)

-- Ajouter la colonne discount si elle n'existe pas déjà
SET @exist_discount := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'transactions' AND COLUMN_NAME = 'discount');
SET @sql_discount = IF(@exist_discount = 0,
    'ALTER TABLE transactions ADD COLUMN discount DECIMAL(15, 2) DEFAULT 0.00',
    'SELECT ''discount column already exists''');
PREPARE stmt FROM @sql_discount;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Mettre à jour les anciennes transactions avec 0 (aucune réduction)
UPDATE transactions
SET discount = 0.00
WHERE discount IS NULL;
