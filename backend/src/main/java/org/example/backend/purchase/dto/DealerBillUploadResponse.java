package org.example.backend.purchase.dto;

public record DealerBillUploadResponse(
        String fileName,
        String storedPath
) {}