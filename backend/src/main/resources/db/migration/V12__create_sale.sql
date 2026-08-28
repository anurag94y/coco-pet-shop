CREATE TABLE sale
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,

    shop_id         BIGINT         NOT NULL,
    customer_id     BIGINT         NOT NULL,

    bill_number     VARCHAR(100)   NOT NULL,
    bill_date       TIMESTAMP      NOT NULL,

    subtotal        DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount    DECIMAL(12, 2) NOT NULL,

    created_by      BIGINT         NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sale_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id),

    CONSTRAINT fk_sale_customer
        FOREIGN KEY (customer_id) REFERENCES customer (id),

    CONSTRAINT fk_sale_user
        FOREIGN KEY (created_by) REFERENCES user (id),

    CONSTRAINT uk_sale_bill_number
        UNIQUE (shop_id, bill_number)
);