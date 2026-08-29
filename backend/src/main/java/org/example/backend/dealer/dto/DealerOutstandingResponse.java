package org.example.backend.dealer.dto;

import java.math.BigDecimal;

public record DealerOutstandingResponse(
        Long dealerId,
        String dealerName,
        BigDecimal outstandingAmount
) {}