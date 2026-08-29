package org.example.backend.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventorySummaryResponse(
        Long productId,
        String productName,
        Integer totalQuantity,
        LocalDate nearestExpiryDate,
        BigDecimal lowestSellingPrice
) {
}