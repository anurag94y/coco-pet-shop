package org.example.backend.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DealerBillReviewResponse(
        String storedPath,
        String dealerName,
        String billNumber,
        LocalDate billDate,
        BigDecimal invoiceTotal,
        boolean needsReview,
        DealerMatchResponse dealerMatch,
        List<DealerBillReviewItemResponse> items
) {
}