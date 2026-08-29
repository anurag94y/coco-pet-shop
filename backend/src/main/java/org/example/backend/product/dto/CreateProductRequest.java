package org.example.backend.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(

        @NotNull
        Long shopId,

        @NotBlank
        @Size(max = 200)
        String name,

        String description,

        @Size(max = 100)
        String category
) {
}