package org.example.backend.inventory.dto;

import java.time.LocalDate;

public record ExpiredInventoryResponse(
        Long batchId,
        Long productId,
        String productName,
        LocalDate expiryDate,
        Integer quantity
) {}