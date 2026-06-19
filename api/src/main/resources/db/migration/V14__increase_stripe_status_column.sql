-- ============================================================
-- V14 : Augmentation de la taille de stripe_payment_status
-- ============================================================
-- La colonne stripe_payment_status était VARCHAR(50), trop petite
-- pour stocker les messages d'erreur Stripe complets (ex: carte refusée
-- avec code d'erreur et request-id).
-- Passage à VARCHAR(500) pour couvrir tous les cas.
-- ============================================================

ALTER TABLE transactions
    MODIFY COLUMN stripe_payment_status VARCHAR(500);
