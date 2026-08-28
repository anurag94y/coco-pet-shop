CREATE TABLE sale_item
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,

    sale_id             BIGINT         NOT NULL,
    product_id          BIGINT         NOT NULL,
    inventory_batch_id  BIGINT         NOT NULL,

    quantity            INT            NOT NULL,

    mrp                 DECIMAL(12, 2) NOT NULL,
    discount_percentage DECIMAL(5, 2)  NOT NULL,

    unit_price          DECIMAL(12, 2) NOT NULL,
    total_price         DECIMAL(12, 2) NOT NULL,

    CONSTRAINT fk_sale_item_sale
        FOREIGN KEY (sale_id) REFERENCES sale (id),

    CONSTRAINT fk_sale_item_product
        FOREIGN KEY (product_id) REFERENCES product (id),

    CONSTRAINT fk_sale_item_batch
        FOREIGN KEY (inventory_batch_id) REFERENCES inventory_batch (id)
);