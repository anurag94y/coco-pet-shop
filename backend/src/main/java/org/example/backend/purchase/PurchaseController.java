package org.example.backend.purchase;

import jakarta.validation.Valid;
import org.example.backend.purchase.dto.*;
import org.example.backend.purchase.extractor.DealerBillExtractionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final DealerBillImportService dealerBillImportService;
    private final DealerBillExtractionService dealerBillExtractionService;
    private final DealerBillReviewService dealerBillReviewService;

    public PurchaseController(PurchaseService purchaseService,
                              DealerBillImportService dealerBillImportService,
                              DealerBillExtractionService dealerBillExtractionService,
                              DealerBillReviewService dealerBillReviewService) {
        this.purchaseService = purchaseService;
        this.dealerBillImportService = dealerBillImportService;
        this.dealerBillExtractionService = dealerBillExtractionService;
        this.dealerBillReviewService = dealerBillReviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse createPurchase(
            @Valid @RequestBody CreatePurchaseRequest request
    ) {
        return purchaseService.createPurchase(request);
    }

    @GetMapping("/{purchaseId}")
    public PurchaseResponse getPurchase(
            @PathVariable Long purchaseId
    ) {
        return purchaseService.getPurchase(purchaseId);
    }

    @GetMapping("/dealer/{dealerId}")
    public List<PurchaseResponse> getDealerPurchases(
            @PathVariable Long dealerId
    ) {
        return purchaseService.getDealerPurchases(dealerId);
    }

    @GetMapping("/shop/{shopId}")
    public List<PurchaseResponse> getShopPurchases(
            @PathVariable Long shopId
    ) {
        return purchaseService.getShopPurchases(shopId);
    }

    @PostMapping("/bill-import")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse importBill(
            @Valid @RequestBody ConfirmDealerBillImportRequest request
    ) {
        return dealerBillImportService.importBill(request);
    }

    @PostMapping("/bill-review")
    public DealerBillReviewResponse reviewBill(
            @RequestParam Long shopId,
            @RequestParam String storedPath
    ) {

        DealerBillParseResponse extraction =
                dealerBillExtractionService
                        .extract(storedPath);

        return dealerBillReviewService
                .buildReview(
                        shopId,
                        extraction
                );
    }
}