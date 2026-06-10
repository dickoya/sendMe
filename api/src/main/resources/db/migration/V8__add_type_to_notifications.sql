-- Ajout du type de notification pour distinguer
-- les différents événements (TRANSFER_COMPLETED, TRANSFER_FAILED, etc.)
ALTER TABLE notifications
    ADD COLUMN type VARCHAR(50) DEFAULT 'INFO';