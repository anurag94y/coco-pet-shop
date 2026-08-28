CREATE TABLE purchase
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id         BIGINT         NOT NULL,
    dealer_id       BIGINT         NOT NULL,
    bill_number     VARCHAR(100),
    bill_date       DATE           NOT NULL,
    bill_image_path VARCHAR(500),
    total_amount    DECIMAL(12, 2) NOT NULL,
    created_by      BIGINT         NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_purchase_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id),

    CONSTRAINT fk_purchase_dealer
        FOREIGN KEY (dealer_id) REFERENCES dealer (id),

    CONSTRAINT fk_purchase_user
        FOREIGN KEY (created_by) REFERENCES user (id)
);