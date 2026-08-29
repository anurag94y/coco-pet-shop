package org.example.backend.purchase.dto;

public record DealerBillOcrResponse(
        String storedPath,
        String extractedText
) {}