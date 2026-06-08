CREATE TABLE notifications (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               message TEXT NOT NULL,
                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               user_id BIGINT NOT NULL,
                               transaction_id BIGINT,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id),
                               CONSTRAINT fk_notification_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);
