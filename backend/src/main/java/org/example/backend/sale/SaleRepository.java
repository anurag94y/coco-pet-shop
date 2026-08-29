package org.example.backend.sale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Page<Sale> findByShopIdOrderByBillDateDesc(
            Long shopId,
            Pageable pageable
    );

    Page<Sale> findByCustomerIdOrderByBillDateDesc(
            Long customerId,
            Pageable pageable
    );
}