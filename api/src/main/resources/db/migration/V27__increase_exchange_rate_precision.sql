-- ============================================================
-- V27 : Élargir la précision de la colonne exchange_rate
-- Problème : DECIMAL(10,6) = 10 chiffres dont 6 décimaux -> max 4
-- chiffres entiers (9999.999999). Les taux EUR -> GNF (~10 000)
-- dépassent cette limite et provoquent l'erreur MySQL 1264
-- "Out of range value for column 'exchange_rate'".
-- Fix : on passe à DECIMAL(15,6) -> max 999 999 999.999999,
-- en conservant la même échelle (6) => opération en place,
-- sans impact sur les données existantes (< 9999).
-- ============================================================
ALTER TABLE transactions
    MODIFY COLUMN exchange_rate DECIMAL(18,6) DEFAULT NULL;
