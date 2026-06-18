-- ============================================================
-- Migration V13 : Support multi-devises avancé
-- Ajoute les colonnes nécessaires pour gérer GBP, USD, CAD,
-- CHF, NGN, GHS, ZAR, MAD, TND, XAF dans les transactions.
-- ============================================================

-- 1. Ajout de la colonne currency_sent_symbol (symbole monétaire)
--    pour un affichage plus riche dans l'UI
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS currency_sent_symbol VARCHAR(5) DEFAULT NULL;

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS currency_received_symbol VARCHAR(5) DEFAULT NULL;

-- 2. Ajout de la colonne sender_country_currency
--    pour tracer depuis quelle devise l'utilisateur a envoyé
ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS sender_country_currency VARCHAR(3) DEFAULT NULL;

-- 3. Ajout d'un index sur les colonnes de devises pour les recherches
--    fréquentes (filtrage par devise dans le dashboard admin)
CREATE INDEX IF NOT EXISTS idx_transactions_currency_sent
    ON transactions (currency_sent);

CREATE INDEX IF NOT EXISTS idx_transactions_currency_received
    ON transactions (currency_received);

-- 4. Table de configuration des devises supportées
--    (miroir de CurrencyConfig côté Java)
CREATE TABLE IF NOT EXISTS supported_currencies (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(3)  NOT NULL UNIQUE,
    symbol      VARCHAR(5)  NOT NULL,
    decimals    INT         NOT NULL DEFAULT 2,
    country_name VARCHAR(100) NOT NULL,
    currency_type VARCHAR(10) NOT NULL CHECK (currency_type IN ('SEND', 'RECEIVE')),
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- 5. Insertion des devises supportées
INSERT INTO supported_currencies (code, symbol, decimals, country_name, currency_type) VALUES
    -- Devises d'envoi
    ('EUR', '€', 2, 'France', 'SEND'),
    ('USD', '$', 2, 'États-Unis', 'SEND'),
    ('GBP', '£', 2, 'Royaume-Uni', 'SEND'),
    ('CAD', 'C$', 2, 'Canada', 'SEND'),
    ('CHF', 'CHF', 2, 'Suisse', 'SEND'),
    -- Devises de réception
    ('XOF', 'CFA', 0, 'UEMOA (Bénin, Burkina, Côte d''Ivoire...)', 'RECEIVE'),
    ('XAF', 'CFA', 0, 'CEMAC (Cameroun, Congo, Gabon...)', 'RECEIVE'),
    ('GNF', 'FG', 0, 'Guinée', 'RECEIVE'),
    ('NGN', '₦', 2, 'Nigeria', 'RECEIVE'),
    ('GHS', '₵', 2, 'Ghana', 'RECEIVE'),
    ('ZAR', 'R', 2, 'Afrique du Sud', 'RECEIVE'),
    ('MAD', 'DH', 2, 'Maroc', 'RECEIVE'),
    ('TND', 'DT', 3, 'Tunisie', 'RECEIVE')
ON CONFLICT (code) DO NOTHING;

-- 6. Mise à jour des transactions existantes avec les symboles
UPDATE transactions
SET currency_sent_symbol = CASE currency_sent
    WHEN 'EUR' THEN '€'
    WHEN 'USD' THEN '$'
    WHEN 'GBP' THEN '£'
    WHEN 'CAD' THEN 'C$'
    WHEN 'CHF' THEN 'CHF'
    ELSE currency_sent
END;

UPDATE transactions
SET currency_received_symbol = CASE currency_received
    WHEN 'XOF' THEN 'CFA'
    WHEN 'XAF' THEN 'CFA'
    WHEN 'GNF' THEN 'FG'
    WHEN 'NGN' THEN '₦'
    WHEN 'GHS' THEN '₵'
    WHEN 'ZAR' THEN 'R'
    WHEN 'MAD' THEN 'DH'
    WHEN 'TND' THEN 'DT'
    ELSE currency_received
END;
