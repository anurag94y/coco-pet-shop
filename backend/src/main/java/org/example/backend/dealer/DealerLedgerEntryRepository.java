package org.example.backend.dealer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DealerLedgerEntryRepository
        extends JpaRepository<DealerLedgerEntry, Long> {

    List<DealerLedgerEntry> findByDealerIdOrderByCreatedAtDesc(Long dealerId);

    List<DealerLedgerEntry> findByPurchaseIdOrderByCreatedAtDesc(Long purchaseId);
}