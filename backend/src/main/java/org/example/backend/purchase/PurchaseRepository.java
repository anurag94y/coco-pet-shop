package org.example.backend.purchase;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByShopIdOrderByBillDateDesc(Long shopId);

    List<Purchase> findByDealerIdOrderByBillDateDesc(Long dealerId);

    boolean existsByShopIdAndDealerIdAndBillNumber(
            Long shopId,
            Long dealerId,
            String billNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Purchase p
        WHERE p.id = :purchaseId
        """)
    Optional<Purchase> findByIdForUpdate(
            @Param("purchaseId") Long purchaseId
    );
}