CREATE TABLE purchase_item
(
    id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
    purchase_id                BIGINT         NOT NULL,
    product_id                 BIGINT         NOT NULL,

    quantity                   INT            NOT NULL,

    mrp                        DECIMAL(12, 2) NOT NULL,
    dealer_discount_percentage DECIMAL(5, 2)  NOT NULL DEFAULT 0,
    purchase_price             DECIMAL(12, 2) NOT NULL,

    CONSTRAINT fk_purchase_item_purchase
        FOREIGN KEY (purchase_id) REFERENCES purchase (id),

    CONSTRAINT fk_purchase_item_product
        FOREIGN KEY (product_id) REFERENCES product (id)
);