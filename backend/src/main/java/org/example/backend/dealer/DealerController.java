package org.example.backend.dealer;

import jakarta.validation.Valid;
import org.example.backend.dealer.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DealerController {

    private final DealerService dealerService;
    private final DealerPaymentService dealerPaymentService;

    public DealerController(DealerService dealerService, DealerPaymentService dealerPaymentService) {
        this.dealerService = dealerService;
        this.dealerPaymentService = dealerPaymentService;
    }

    @PostMapping("/dealers")
    @ResponseStatus(HttpStatus.CREATED)
    public DealerResponse createDealer(
            @Valid @RequestBody CreateDealerRequest request
    ) {
        return dealerService.createDealer(request);
    }

    @GetMapping("/dealers/{dealerId}")
    public DealerResponse getDealer(
            @PathVariable Long dealerId
    ) {
        return dealerService.getDealer(dealerId);
    }

    @GetMapping("/shops/{shopId}/dealers")
    public List<DealerResponse> getDealers(
            @PathVariable Long shopId
    ) {
        return dealerService.getDealers(shopId);
    }

    @PutMapping("/dealers/{dealerId}")
    public DealerResponse updateDealer(
            @PathVariable Long dealerId,
            @Valid @RequestBody UpdateDealerRequest request
    ) {
        return dealerService.updateDealer(dealerId, request);
    }

    @GetMapping("/dealers/{dealerId}/address-history")
    public List<DealerAddressHistoryResponse> getAddressHistory(
            @PathVariable Long dealerId
    ) {
        return dealerService.getAddressHistory(dealerId);
    }

    @GetMapping("/dealers/{dealerId}/ledger")
    public List<DealerLedgerResponse> getLedger(
            @PathVariable Long dealerId
    ) {
        return dealerPaymentService.getDealerLedger(dealerId);
    }

    @GetMapping("/dealers/{dealerId}/outstanding")
    public DealerOutstandingResponse getOutstanding(
            @PathVariable Long dealerId
    ) {
        return dealerPaymentService.getOutstandingBalance(dealerId);
    }
}