package org.example.backend.dealer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealerAddressHistoryRepository
        extends JpaRepository<DealerAddressHistory, Long> {

    List<DealerAddressHistory> findByDealerIdOrderByValidFromDesc(Long dealerId);
}