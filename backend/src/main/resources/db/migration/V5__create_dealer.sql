CREATE TABLE dealer
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id    BIGINT       NOT NULL,
    name       VARCHAR(150) NOT NULL,
    phone      VARCHAR(20),
    address    TEXT,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dealer_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id)
);