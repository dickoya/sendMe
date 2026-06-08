CREATE TABLE beneficiaries (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               first_name VARCHAR(100) NOT NULL,
                               last_name VARCHAR(100) NOT NULL,
                               channel VARCHAR(30) NOT NULL,
                               user_id BIGINT NOT NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               CONSTRAINT fk_beneficiary_user FOREIGN KEY (user_id) REFERENCES users(id)
);