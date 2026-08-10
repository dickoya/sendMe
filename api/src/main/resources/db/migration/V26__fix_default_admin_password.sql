-- Correction du mot de passe de l'administrateur par défaut
-- Le hash de la migration V9 ne correspondait pas au mot de passe annoncé (Admin@sendme2025).
-- Nouveau hash BCrypt (force 10) généré pour "Admin@sendme2025".
-- IMPORTANT : Changer ce mot de passe en production !

UPDATE users
SET password_hash = '$2a$10$QZrCuEbID5ogxpCgexCaDOiF3/aaTWFcU2PY5P/WW5oxheJOaaX0y'
WHERE email = 'admin@sendme.com';
