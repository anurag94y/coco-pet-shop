package org.example.backend.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseItemResponse(
        Long productId,
        String productName,
        Long inventoryBatchId,
        Integer quantity,
        BigDecimal mrp,
        BigDecimal dealerDiscountPercentage,
        BigDecimal purchasePrice,
        LocalDate expiryDate,
        BigDecimal sellingDiscountPercentage
) {
}