CREATE TABLE product
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id     BIGINT       NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    category    VARCHAR(100),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id)
);