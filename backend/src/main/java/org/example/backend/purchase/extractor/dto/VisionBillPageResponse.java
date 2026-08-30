package org.example.backend.purchase.extractor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VisionBillPageResponse(
        String dealerName,
        String billNumber,
        LocalDate billDate,
        BigDecimal invoiceTotal,
        Integer pageNumber,
        List<VisionBillItem> items
) {

    public record VisionBillItem(
            String name,
            BigDecimal mrp,
            BigDecimal discountPercentage,
            BigDecimal purchasePrice,
            Integer quantity,
            BigDecimal totalPrice
    ) {}
}