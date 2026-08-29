package org.example.backend.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        Long shopId,
        Long customerId,
        String customerName,
        String billNumber,
        LocalDateTime billDate,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        Long createdBy,
        String paymentMethod,
        String paymentStatus,
        String transactionReference,
        List<SaleItemResponse> items
) {
}