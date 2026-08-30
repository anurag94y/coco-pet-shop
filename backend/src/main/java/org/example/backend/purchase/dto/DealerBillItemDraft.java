package org.example.backend.purchase.dto;

import java.math.BigDecimal;

public record DealerBillItemDraft(
        String name,
        BigDecimal mrp,
        BigDecimal dealerDiscountPercentage,
        Integer quantity,
        BigDecimal purchasePrice,
        BigDecimal totalPrice,
        BigDecimal extractedPurchasePrice,
        BigDecimal extractedTotalPrice,
        boolean needsReview
) {
}