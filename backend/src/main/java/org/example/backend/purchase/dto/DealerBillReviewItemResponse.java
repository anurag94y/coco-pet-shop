package org.example.backend.purchase.dto;

import java.math.BigDecimal;
import java.util.List;

public record DealerBillReviewItemResponse(
        String extractedName,
        BigDecimal mrp,
        BigDecimal dealerDiscountPercentage,
        Integer quantity,
        BigDecimal purchasePrice,
        BigDecimal totalPrice,
        boolean needsReview,
        List<ProductSuggestionResponse> productSuggestions
) {
}