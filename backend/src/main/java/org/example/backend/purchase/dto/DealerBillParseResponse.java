package org.example.backend.purchase.dto;

import java.time.LocalDate;
import java.util.List;

public record DealerBillParseResponse(
        String storedPath,
        String dealerName,
        String billNumber,
        LocalDate billDate,
        List<DealerBillItemDraft> items,
        String value
) {}