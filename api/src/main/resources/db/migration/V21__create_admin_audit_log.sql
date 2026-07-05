-- ============================================================
-- V21__create_admin_audit_log.sql
-- Table d'audit pour tracer toutes les actions des administrateurs.
-- Chaque action sensible (suspension, réactivation, modification
-- des frais, envoi maintenance) est enregistrée avec :
--   - L'admin qui a effectué l'action
--   - Le type d'action
--   - La cible (utilisateur, configuration, etc.)
--   - Les détails (ancienne valeur, nouvelle valeur, raison)
--   - L'adresse IP et le timestamp
-- ============================================================

CREATE TABLE IF NOT EXISTS admin_audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Admin qui a effectué l'action
    admin_id        BIGINT       NOT NULL,
    admin_email     VARCHAR(255) NOT NULL,

    -- Type d'action (USER_SUSPENDED, USER_REACTIVATED, FEE_UPDATED,
    --                MAINTENANCE_SENT, BATCH_TOGGLE, etc.)
    action          VARCHAR(50)  NOT NULL,

    -- Type d'entité ciblée (USER, CONFIG, NOTIFICATION, etc.)
    entity_type     VARCHAR(50)  NOT NULL,

    -- Identifiant de l'entité ciblée (userId, etc.) — nullable
    entity_id       BIGINT       NULL,

    -- Détails JSON de l'action (ancienne valeur, nouvelle valeur, raison, etc.)
    details         TEXT         NULL,

    -- Adresse IP de l'admin au moment de l'action
    ip_address      VARCHAR(45)  NULL,

    -- Horodatage de l'action
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_admin_audit_action (action),
    INDEX idx_admin_audit_admin_id (admin_id),
    INDEX idx_admin_audit_created_at (created_at),
    INDEX idx_admin_audit_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
