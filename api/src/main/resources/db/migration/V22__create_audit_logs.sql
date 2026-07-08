-- ============================================================
-- V22__create_audit_logs.sql
-- Table unique d'audit centralisant TOUS les événements
-- de la plateforme (admin + système).
--
-- Catégories (category) :
--   AUTH     → inscriptions, connexions, déconnexions
--   TRANSFER → création, succès, échec, remboursement de transferts
--   ADMIN    → actions des administrateurs
--   CONFIG   → modifications de configuration
--   SYSTEM   → erreurs système, jobs, maintenance
--
-- Types d'événements (event_type) :
--   AUTH       : REGISTER, LOGIN, LOGIN_FAILED, LOGOUT
--   TRANSFER   : TRANSFER_CREATED, TRANSFER_COMPLETED, TRANSFER_FAILED,
--                TRANSFER_REFUNDED
--   ADMIN      : USER_TOGGLE, BATCH_TOGGLE, FEE_UPDATE, MAINTENANCE
--   CONFIG     : CONFIG_CHANGE
--   SYSTEM     : ERROR, JOB_EXECUTED
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Catégorie principale (AUTH, TRANSFER, ADMIN, CONFIG, SYSTEM)
    category        VARCHAR(50)  NOT NULL,

    -- Type d'événement spécifique
    event_type      VARCHAR(50)  NOT NULL,

    -- Email de l'utilisateur/admin concerné
    user_email      VARCHAR(255) NULL,

    -- Identifiant de l'utilisateur/admin concerné
    user_id         BIGINT       NULL,

    -- Identifiant de la ressource (transactionId, etc.)
    resource_id     BIGINT       NULL,

    -- Type de ressource (TRANSACTION, USER, CONFIG, etc.)
    resource_type   VARCHAR(50)  NULL,

    -- Statut (SUCCESS, FAILED, PENDING)
    status          VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',

    -- Message descriptif
    message         TEXT         NULL,

    -- Détails JSON supplémentaires
    details         TEXT         NULL,

    -- Adresse IP
    ip_address      VARCHAR(45)  NULL,

    -- User-Agent
    user_agent      TEXT         NULL,

    -- Horodatage
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_audit_category (category),
    INDEX idx_audit_event_type (event_type),
    INDEX idx_audit_user_email (user_email),
    INDEX idx_audit_resource (resource_type, resource_id),
    INDEX idx_audit_created_at (created_at),
    INDEX idx_audit_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
