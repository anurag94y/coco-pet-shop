package org.example.backend.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShopId(Long shopId);

    List<Product> findTop10ByShopIdAndNameContainingIgnoreCase(
            Long shopId,
            String name
    );

    Optional<Product> findByShopIdAndNameIgnoreCase(
            Long shopId,
            String name
    );
}