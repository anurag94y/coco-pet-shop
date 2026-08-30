package org.example.backend.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ConfirmDealerBillImportRequest(
        @NotNull Long shopId,
        Long dealerId,
        String dealerName,
        @NotNull Long createdBy,
        String billNumber,
        @NotNull LocalDate billDate,
        String billImagePath,
        @NotEmpty List<@Valid ConfirmDealerBillItemRequest> items
) {}