package org.example.backend.inventory;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryBatchRepository
        extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findByShopIdAndProductId(Long shopId, Long productId);

    List<InventoryBatch> findByProductIdAndQuantityGreaterThan(
            Long productId,
            Integer quantity
    );

    Optional<InventoryBatch> findByPurchaseItemId(Long purchaseItemId);

    List<InventoryBatch>
    findByShopIdAndProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(
            Long shopId,
            Long productId,
            Integer quantity
    );

    List<InventoryBatch>
    findByShopIdAndQuantityGreaterThan(
            Long shopId,
            Integer quantity
    );

    List<InventoryBatch> findByShopIdAndExpiryDateBeforeAndQuantityGreaterThan(
            Long shopId,
            LocalDate expiryDate,
            Integer quantity
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT b
                FROM InventoryBatch b
                WHERE b.shop.id = :shopId
                  AND b.product.id = :productId
                  AND b.quantity > 0
                  AND (b.expiryDate IS NULL OR b.expiryDate >= :today)
                ORDER BY
                  CASE WHEN b.expiryDate IS NULL THEN 1 ELSE 0 END,
                  b.expiryDate ASC,
                  b.id ASC
            """)
    List<InventoryBatch> findAvailableBatchesForSale(
            @Param("shopId") Long shopId,
            @Param("productId") Long productId,
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b
        FROM InventoryBatch b
        WHERE b.id = :batchId
        """)
    Optional<InventoryBatch> findByIdForUpdate(
            @Param("batchId") Long batchId
    );
}