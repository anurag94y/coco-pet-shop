CREATE TABLE inventory_transaction
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,

    shop_id          BIGINT      NOT NULL,
    product_id       BIGINT      NOT NULL,
    batch_id         BIGINT      NOT NULL,

    transaction_type VARCHAR(30) NOT NULL,
    quantity         INT         NOT NULL,

    reference_type   VARCHAR(30),
    reference_id     BIGINT,

    created_by       BIGINT      NOT NULL,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_transaction_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id),

    CONSTRAINT fk_inventory_transaction_product
        FOREIGN KEY (product_id) REFERENCES product (id),

    CONSTRAINT fk_inventory_transaction_batch
        FOREIGN KEY (batch_id) REFERENCES inventory_batch (id),

    CONSTRAINT fk_inventory_transaction_user
        FOREIGN KEY (created_by) REFERENCES user (id)
);