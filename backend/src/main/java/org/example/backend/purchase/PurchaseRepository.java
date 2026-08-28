package org.example.backend.purchase;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByShopIdOrderByBillDateDesc(Long shopId);

    List<Purchase> findByDealerIdOrderByBillDateDesc(Long dealerId);
}