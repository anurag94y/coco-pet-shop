package org.example.backend.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @Size(max = 20)
        String phone,

        String address
) {
}