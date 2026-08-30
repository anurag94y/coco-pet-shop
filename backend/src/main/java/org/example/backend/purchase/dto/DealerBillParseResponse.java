package org.example.backend.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DealerBillParseResponse(
        String storedPath,
        String dealerName,
        String billNumber,
        LocalDate billDate,
        BigDecimal invoiceTotal,
        List<DealerBillItemDraft> items,
        boolean needsReview
) {}