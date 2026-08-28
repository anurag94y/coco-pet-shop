package org.example.backend.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryBatchRepository
        extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findByShopIdAndProductId(Long shopId, Long productId);

    List<InventoryBatch> findByProductIdAndQuantityGreaterThan(
            Long productId,
            Integer quantity
    );
}