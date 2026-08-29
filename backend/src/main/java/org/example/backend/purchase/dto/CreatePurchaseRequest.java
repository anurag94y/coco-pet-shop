package org.example.backend.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreatePurchaseRequest(

        @NotNull
        Long shopId,

        @NotNull
        Long dealerId,

        @NotNull
        Long createdBy,

        @Size(max = 100)
        String billNumber,

        @NotNull
        LocalDate billDate,

        @Size(max = 500)
        String billImagePath,

        @NotEmpty
        List<@Valid CreatePurchaseItemRequest> items
) {
}