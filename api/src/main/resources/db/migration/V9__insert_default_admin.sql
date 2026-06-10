-- Insertion de l'administrateur par défaut
-- Mot de passe : Admin@sendme2025
-- Hash BCrypt généré avec BCryptPasswordEncoder (force 10)
-- IMPORTANT : Changer ce mot de passe en production !

INSERT INTO users (
    first_name,
    last_name,
    email,
    password_hash,
    phone_number,
    role,
    is_active,
    created_at,
    updated_at
) VALUES (
             'Admin',
             'SendMe',
             'admin@sendme.com',
             '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
             '+33 1 00 00 00 00',
             'ROLE_ADMIN',
             TRUE,
             NOW(),
             NOW()
         );