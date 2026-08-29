package org.example.backend.sale.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSaleItemRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity
) {
}