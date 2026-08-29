package org.example.backend.dealer.dto;

import java.time.LocalDateTime;

public record DealerAddressHistoryResponse(
        Long id,
        String address,
        LocalDateTime validFrom,
        LocalDateTime validTo
) {
}