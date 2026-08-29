package org.example.backend.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSaleRequest(

        @NotNull Long shopId,

        @NotNull Long customerId,

        @NotNull Long createdBy,

        @NotBlank
        @Size(max = 100)
        String billNumber,

        @NotEmpty
        List<@Valid CreateSaleItemRequest> items,

        @NotBlank
        @Size(max = 30)
        String paymentMethod,

        @Size(max = 100)
        String transactionReference
) {
}