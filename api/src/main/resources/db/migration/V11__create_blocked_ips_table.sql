CREATE TABLE IF NOT EXISTS blocked_ips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL,
    blocked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    blocked_until DATETIME NOT NULL,
    reason VARCHAR(255) DEFAULT NULL,
    INDEX idx_blocked_ips_ip (ip_address),
    INDEX idx_blocked_ips_blocked_until (blocked_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
