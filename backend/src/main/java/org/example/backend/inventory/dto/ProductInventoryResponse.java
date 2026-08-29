package org.example.backend.inventory.dto;

import java.util.List;

public record ProductInventoryResponse(
        Long productId,
        String productName,
        Integer totalQuantity,
        List<InventoryBatchResponse> batches
) {
}