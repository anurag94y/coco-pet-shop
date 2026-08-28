CREATE TABLE user
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_id       BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL,
    phone         VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(30)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id),

    CONSTRAINT uk_user_shop_email
        UNIQUE (shop_id, email)
);