package org.example.backend.dealer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DealerPaymentResponse(
        Long ledgerEntryId,
        Long dealerId,
        Long purchaseId,
        BigDecimal amount,
        String transactionType,
        LocalDateTime createdAt
) {}