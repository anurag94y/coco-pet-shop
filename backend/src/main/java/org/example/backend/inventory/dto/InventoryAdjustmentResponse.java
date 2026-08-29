package org.example.backend.inventory.dto;

public record InventoryAdjustmentResponse(
        Long batchId,
        Long productId,
        String transactionType,
        Integer adjustedQuantity,
        Integer remainingQuantity
) {}