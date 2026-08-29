ALTER TABLE inventory_batch
    ADD COLUMN purchase_item_id BIGINT NULL;

ALTER TABLE inventory_batch
    ADD CONSTRAINT fk_inventory_batch_purchase_item
        FOREIGN KEY (purchase_item_id)
            REFERENCES purchase_item(id);

CREATE INDEX idx_inventory_batch_purchase_item
    ON inventory_batch(purchase_item_id);