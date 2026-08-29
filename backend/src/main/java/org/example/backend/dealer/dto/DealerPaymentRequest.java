package org.example.backend.dealer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DealerPaymentRequest(
        @NotNull Long dealerId,
        @NotNull Long purchaseId,
        @NotNull @Positive BigDecimal amount,
        String description,
        @NotNull Long createdBy
) {}