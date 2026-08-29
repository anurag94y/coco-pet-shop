package org.example.backend.dealer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DealerLedgerResponse(
        Long id,
        Long dealerId,
        Long purchaseId,
        String transactionType,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {}