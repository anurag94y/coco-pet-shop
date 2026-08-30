package org.example.backend.purchase.dto;

public record DealerMatchResponse(
        Long dealerId,
        String name,
        boolean exactMatch
) {
}