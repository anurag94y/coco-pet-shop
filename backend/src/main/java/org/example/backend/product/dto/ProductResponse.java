package org.example.backend.product.dto;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        Long shopId,
        String name,
        String description,
        String category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}