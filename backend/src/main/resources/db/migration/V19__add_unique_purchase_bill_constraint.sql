ALTER TABLE purchase
    ADD CONSTRAINT uk_purchase_shop_dealer_bill
        UNIQUE (shop_id, dealer_id, bill_number);