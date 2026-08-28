CREATE TABLE payment
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,

    sale_id               BIGINT         NOT NULL,

    amount                DECIMAL(12, 2) NOT NULL,
    payment_method        VARCHAR(30)    NOT NULL,
    payment_status        VARCHAR(30)    NOT NULL,

    transaction_reference VARCHAR(100),

    created_at            TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_sale
        FOREIGN KEY (sale_id) REFERENCES sale (id)
);