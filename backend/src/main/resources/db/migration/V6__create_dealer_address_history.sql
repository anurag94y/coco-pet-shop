CREATE TABLE dealer_address_history
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    dealer_id  BIGINT    NOT NULL,
    address    TEXT      NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_to   TIMESTAMP NULL,

    CONSTRAINT fk_dealer_address_dealer
        FOREIGN KEY (dealer_id) REFERENCES dealer (id)
);