package org.example.backend.dealer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DealerAddressHistoryRepository
        extends JpaRepository<DealerAddressHistory, Long> {

    List<DealerAddressHistory> findByDealerIdOrderByValidFromDesc(
            Long dealerId
    );

    Optional<DealerAddressHistory>
    findFirstByDealerIdAndValidToIsNullOrderByValidFromDesc(
            Long dealerId
    );
}