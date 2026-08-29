package org.example.backend.purchase.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePurchaseItemRequest(

        @NotNull
        Long productId,

        @NotNull
        @Positive
        Integer quantity,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal mrp,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal dealerDiscountPercentage,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal purchasePrice,

        LocalDate expiryDate,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        BigDecimal sellingDiscountPercentage
) {
}