package org.example.backend.dealer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    List<Dealer> findByShopId(Long shopId);

    Optional<Dealer> findByShopIdAndNameIgnoreCase(
            Long shopId,
            String name
    );
}