CREATE TABLE mobile_money_details (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      phone_number VARCHAR(20) NOT NULL,
                                      beneficiary_id BIGINT NOT NULL UNIQUE,
                                      CONSTRAINT fk_mobile_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id)
);