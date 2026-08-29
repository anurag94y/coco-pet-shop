package org.example.backend.customer.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
        Long id,
        Long shopId,
        String name,
        String phone,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}