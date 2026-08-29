package org.example.backend.sale.dto;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long productId,
        String productName,
        Long inventoryBatchId,
        Integer quantity,
        BigDecimal mrp,
        BigDecimal discountPercentage,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}