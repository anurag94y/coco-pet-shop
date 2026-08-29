package org.example.backend.purchase;

import jakarta.validation.Valid;
import org.example.backend.purchase.dto.ConfirmDealerBillImportRequest;
import org.example.backend.purchase.dto.CreatePurchaseRequest;
import org.example.backend.purchase.dto.PurchaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final DealerBillImportService dealerBillImportService;

    public PurchaseController(PurchaseService purchaseService,
                              DealerBillImportService dealerBillImportService) {
        this.purchaseService = purchaseService;
        this.dealerBillImportService = dealerBillImportService;
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
}