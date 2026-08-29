package org.example.backend.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryBatchResponse(
        Long batchId,
        Long purchaseId,
        Integer quantity,
        LocalDate expiryDate,
        BigDecimal purchasePrice,
        BigDecimal mrp,
        BigDecimal sellingDiscountPercentage,
        BigDecimal sellingPrice
) {
}