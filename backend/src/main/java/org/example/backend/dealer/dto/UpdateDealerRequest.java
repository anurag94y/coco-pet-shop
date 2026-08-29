package org.example.backend.dealer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDealerRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 20)
        String phone,

        String address
) {
}