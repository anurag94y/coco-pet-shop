package org.example.backend.purchase.dto;

public record ProductSuggestionResponse(
        Long productId,
        String name,
        boolean exactMatch
) {
}