CREATE TABLE product_image
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id    BIGINT       NOT NULL,
    image_path    VARCHAR(500) NOT NULL,
    display_order INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_image_product
        FOREIGN KEY (product_id) REFERENCES product (id)
);