package org.example.backend.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryAdjustmentRequest(
        @NotNull Long batchId,
        @NotNull @Positive Integer quantity,
        @NotBlank String type,
        String reason,
        @NotNull Long createdBy
) {}