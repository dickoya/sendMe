CREATE TABLE bank_details (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              bank_name VARCHAR(150) NOT NULL,
                              iban VARCHAR(34) NOT NULL,
                              bic VARCHAR(11) NOT NULL,
                              beneficiary_id BIGINT NOT NULL UNIQUE,
                              CONSTRAINT fk_bank_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id)
);