-- Ajout des champs Stripe à la table transactions
-- Ces champs permettent de tracer chaque paiement dans le dashboard Stripe
-- et de gérer les remboursements en cas d'échec du transfert.

ALTER TABLE transactions
    ADD COLUMN stripe_payment_intent_id VARCHAR(100),
    ADD COLUMN stripe_payment_status VARCHAR(50);

-- Index pour retrouver rapidement une transaction par son PaymentIntent Stripe
CREATE INDEX idx_stripe_payment_intent
    ON transactions(stripe_payment_intent_id);