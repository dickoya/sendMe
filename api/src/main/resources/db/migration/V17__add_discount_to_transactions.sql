-- Migration V17 : Ajouter la colonne discount aux transactions
-- Pour stocker le montant de réduction appliqué (coupon de parrainage)

ALTER TABLE transactions
ADD COLUMN discount DECIMAL(15, 2) DEFAULT 0.00;

-- Mettre à jour les anciennes transactions avec 0 (aucune réduction)
UPDATE transactions
SET discount = 0.00
WHERE discount IS NULL;
