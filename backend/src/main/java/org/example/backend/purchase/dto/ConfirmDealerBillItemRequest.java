package org.example.backend.purchase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConfirmDealerBillItemRequest(
        Long productId,

        @NotBlank String productName,

        @NotNull @Positive Integer quantity,

        @NotNull @Positive BigDecimal mrp,

        @NotNull BigDecimal dealerDiscountPercentage,

        @NotNull @Positive BigDecimal purchasePrice,

        LocalDate expiryDate
) {}