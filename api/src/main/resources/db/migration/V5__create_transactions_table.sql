CREATE TABLE transactions (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              reference VARCHAR(30) NOT NULL UNIQUE,
                              amount_sent DECIMAL(15,2) NOT NULL,
                              currency_sent VARCHAR(5) NOT NULL,
                              amount_received DECIMAL(15,2),
                              currency_received VARCHAR(5),
                              exchange_rate DECIMAL(10,6),
                              fees DECIMAL(10,2),
                              status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                              channel ENUM('BANK_TRANSFER', 'ORANGE_MONEY', 'MTN_MOBILE_MONEY', 'WAVE', 'MOOV_MONEY', 'AIRTEL_MONEY') NOT NULL,
                              idempotency_key VARCHAR(100) UNIQUE,
                              sender_id BIGINT NOT NULL,
                              beneficiary_id BIGINT NOT NULL,
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              CONSTRAINT fk_transaction_sender FOREIGN KEY (sender_id) REFERENCES users(id),
                              CONSTRAINT fk_transaction_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id)
);