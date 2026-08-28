package org.example.backend.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long> {

    List<InventoryTransaction> findByBatchIdOrderByCreatedAtDesc(Long batchId);

    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);
}