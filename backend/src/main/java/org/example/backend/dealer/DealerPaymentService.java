package org.example.backend.dealer;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.dealer.dto.DealerLedgerResponse;
import org.example.backend.dealer.dto.DealerOutstandingResponse;
import org.example.backend.dealer.dto.DealerPaymentRequest;
import org.example.backend.dealer.dto.DealerPaymentResponse;
import org.example.backend.purchase.Purchase;
import org.example.backend.purchase.PurchaseRepository;
import org.example.backend.user.AppUser;
import org.example.backend.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DealerPaymentService {

    private final DealerRepository dealerRepository;
    private final PurchaseRepository purchaseRepository;
    private final DealerLedgerEntryRepository dealerLedgerEntryRepository;
    private final AppUserRepository appUserRepository;

    public DealerPaymentService(
            DealerRepository dealerRepository,
            PurchaseRepository purchaseRepository,
            DealerLedgerEntryRepository dealerLedgerEntryRepository,
            AppUserRepository appUserRepository
    ) {
        this.dealerRepository = dealerRepository;
        this.purchaseRepository = purchaseRepository;
        this.dealerLedgerEntryRepository = dealerLedgerEntryRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public DealerPaymentResponse payDealer(
            DealerPaymentRequest request
    ) {

        Dealer dealer =
                dealerRepository.findById(request.dealerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Dealer not found: "
                                                + request.dealerId()
                                )
                        );

        Purchase purchase =
                purchaseRepository.findByIdForUpdate(request.purchaseId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Purchase not found: "
                                                + request.purchaseId()
                                )
                        );

        AppUser user =
                appUserRepository.findById(request.createdBy())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found: "
                                                + request.createdBy()
                                )
                        );

        if (!purchase.getDealer().getId()
                .equals(dealer.getId())) {

            throw new IllegalArgumentException(
                    "Purchase does not belong to dealer"
            );
        }

        if (!dealer.getShop().getId()
                .equals(user.getShop().getId())) {

            throw new IllegalArgumentException(
                    "User does not belong to dealer shop"
            );
        }

        DealerLedgerEntry entry =
                new DealerLedgerEntry();

        entry.setShop(dealer.getShop());
        entry.setDealer(dealer);
        entry.setPurchase(purchase);
        entry.setTransactionType("PAYMENT");
        entry.setAmount(
                request.amount().negate()
        );
        entry.setDescription(
                request.description()
        );
        entry.setCreatedBy(user);

        DealerLedgerEntry saved =
                dealerLedgerEntryRepository.save(entry);

        BigDecimal outstanding =
                getOutstandingForPurchase(
                        purchase.getId()
                );

        if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Purchase is already fully paid"
            );
        }

        if (request.amount()
                .compareTo(outstanding) != 0) {

            throw new IllegalArgumentException(
                    "Full payment required. Outstanding amount: "
                            + outstanding
            );
        }

        return new DealerPaymentResponse(
                saved.getId(),
                dealer.getId(),
                purchase.getId(),
                request.amount(),
                saved.getTransactionType(),
                saved.getCreatedAt()
        );
    }

//    @Transactional(readOnly = true)
//    public BigDecimal getOutstandingBalance(
//            Long dealerId
//    ) {
//
//        if (!dealerRepository.existsById(dealerId)) {
//            throw new ResourceNotFoundException(
//                    "Dealer not found: " + dealerId
//            );
//        }
//
//        return dealerLedgerEntryRepository
//                .findByDealerIdOrderByCreatedAtDesc(dealerId)
//                .stream()
//                .map(DealerLedgerEntry::getAmount)
//                .reduce(
//                        BigDecimal.ZERO,
//                        BigDecimal::add
//                );
//    }

    @Transactional(readOnly = true)
    private BigDecimal getOutstandingForPurchase(
            Long purchaseId
    ) {

        return dealerLedgerEntryRepository
                .findByPurchaseIdOrderByCreatedAtDesc(
                        purchaseId
                )
                .stream()
                .map(DealerLedgerEntry::getAmount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    @Transactional(readOnly = true)
    public List<DealerLedgerResponse> getDealerLedger(
            Long dealerId
    ) {

        if (!dealerRepository.existsById(dealerId)) {
            throw new ResourceNotFoundException(
                    "Dealer not found: " + dealerId
            );
        }

        return dealerLedgerEntryRepository
                .findByDealerIdOrderByCreatedAtDesc(dealerId)
                .stream()
                .map(entry ->
                        new DealerLedgerResponse(
                                entry.getId(),
                                entry.getDealer().getId(),
                                entry.getPurchase() != null
                                        ? entry.getPurchase().getId()
                                        : null,
                                entry.getTransactionType(),
                                entry.getAmount(),
                                entry.getDescription(),
                                entry.getCreatedAt()
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public DealerOutstandingResponse getOutstandingBalance(
            Long dealerId
    ) {

        Dealer dealer =
                dealerRepository.findById(dealerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Dealer not found: " + dealerId
                                )
                        );

        BigDecimal outstanding =
                dealerLedgerEntryRepository
                        .findByDealerIdOrderByCreatedAtDesc(dealerId)
                        .stream()
                        .map(DealerLedgerEntry::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new DealerOutstandingResponse(
                dealer.getId(),
                dealer.getName(),
                outstanding
        );
    }
}