CREATE TABLE inventory_batch
(
    id                          BIGINT PRIMARY KEY AUTO_INCREMENT,

    shop_id                     BIGINT         NOT NULL,
    product_id                  BIGINT         NOT NULL,
    purchase_id                 BIGINT         NOT NULL,

    expiry_date                 DATE NULL,

    quantity                    INT            NOT NULL,
    purchase_price              DECIMAL(12, 2) NOT NULL,
    mrp                         DECIMAL(12, 2) NOT NULL,

    selling_discount_percentage DECIMAL(5, 2)  NOT NULL DEFAULT 0,

    created_at                  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_batch_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id),

    CONSTRAINT fk_inventory_batch_product
        FOREIGN KEY (product_id) REFERENCES product (id),

    CONSTRAINT fk_inventory_batch_purchase
        FOREIGN KEY (purchase_id) REFERENCES purchase (id)
);