-- ============================================================
-- Migration V16 : Nettoyage des tables et colonnes obsolètes
--
-- Supprime :
--   - bank_details           (remplacée par beneficiary_channels en V15)
--   - mobile_money_details   (remplacée par beneficiary_channels en V15)
--   - supported_currencies   (créée en V13 mais jamais utilisée dans le code)
--
-- Supprime les colonnes inutilisées de transactions :
--   - currency_sent_symbol
--   - currency_received_symbol
--   - sender_country_currency
-- ============================================================

-- 1. Supprimer les tables remplacées par beneficiary_channels
DROP TABLE IF EXISTS bank_details;
DROP TABLE IF EXISTS mobile_money_details;

-- 2. Supprimer la table supported_currencies (jamais utilisée dans le code)
DROP TABLE IF EXISTS supported_currencies;

-- 3. Supprimer les colonnes inutilisées de transactions
ALTER TABLE transactions
    DROP COLUMN IF EXISTS currency_sent_symbol,
    DROP COLUMN IF EXISTS currency_received_symbol,
    DROP COLUMN IF EXISTS sender_country_currency;
