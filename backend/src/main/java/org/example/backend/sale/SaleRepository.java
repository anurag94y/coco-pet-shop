package org.example.backend.sale;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCustomerIdOrderByBillDateDesc(Long customerId);

    List<Sale> findByShopIdOrderByBillDateDesc(Long shopId);

    Optional<Sale> findByShopIdAndBillNumber(
            Long shopId,
            String billNumber
    );
}