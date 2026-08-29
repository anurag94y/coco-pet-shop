package org.example.backend.dealer.dto;

import java.time.LocalDateTime;

public record DealerResponse(
        Long id,
        Long shopId,
        String name,
        String phone,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}