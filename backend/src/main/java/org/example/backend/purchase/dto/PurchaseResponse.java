package org.example.backend.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseResponse(
        Long id,
        Long shopId,
        Long dealerId,
        String dealerName,
        String billNumber,
        LocalDate billDate,
        String billImagePath,
        BigDecimal totalAmount,
        Long createdBy,
        List<PurchaseItemResponse> items
) {
}