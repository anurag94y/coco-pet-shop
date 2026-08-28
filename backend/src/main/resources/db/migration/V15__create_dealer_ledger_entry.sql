CREATE TABLE dealer_ledger_entry
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,

    shop_id          BIGINT         NOT NULL,
    dealer_id        BIGINT         NOT NULL,

    purchase_id      BIGINT NULL,

    transaction_type VARCHAR(30)    NOT NULL,
    amount           DECIMAL(12, 2) NOT NULL,

    description      VARCHAR(500),

    created_by       BIGINT         NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dealer_ledger_shop
        FOREIGN KEY (shop_id) REFERENCES shop (id),

    CONSTRAINT fk_dealer_ledger_dealer
        FOREIGN KEY (dealer_id) REFERENCES dealer (id),

    CONSTRAINT fk_dealer_ledger_purchase
        FOREIGN KEY (purchase_id) REFERENCES purchase (id),

    CONSTRAINT fk_dealer_ledger_user
        FOREIGN KEY (created_by) REFERENCES user (id)
);