-- ============================================================
-- V24__drop_admin_audit_log.sql
-- Supprime la table admin_audit_log devenue obsolète.
--
-- Le système d'audit de la plateforme est centralisé dans la
-- table unique `audit_logs` (créée en V22) qui couvre aussi
-- bien les événements administrateurs que les événements système.
-- ============================================================

DROP TABLE IF EXISTS admin_audit_log;
