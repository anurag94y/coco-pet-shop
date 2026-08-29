package org.example.backend.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleSummaryResponse(
        Long id,
        String billNumber,
        LocalDateTime billDate,
        Long customerId,
        String customerName,
        BigDecimal totalAmount,
        String paymentStatus
) {}