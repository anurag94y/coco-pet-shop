CREATE INDEX idx_product_name
ON product(shop_id, name);

CREATE INDEX idx_product_image_product
ON product_image(product_id);

CREATE INDEX idx_inventory_product
ON inventory_batch(product_id);

CREATE INDEX idx_inventory_product_expiry
ON inventory_batch(product_id, expiry_date);

CREATE INDEX idx_inventory_transaction_batch
ON inventory_transaction(batch_id);

CREATE INDEX idx_customer_phone
ON customer(shop_id, phone);

CREATE INDEX idx_dealer_phone
ON dealer(shop_id, phone);