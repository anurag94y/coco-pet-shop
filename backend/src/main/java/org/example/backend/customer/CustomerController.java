package org.example.backend.customer;

import jakarta.validation.Valid;
import org.example.backend.customer.dto.CreateCustomerRequest;
import org.example.backend.customer.dto.CustomerResponse;
import org.example.backend.customer.dto.UpdateCustomerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(
            CustomerService customerService
    ) {
        this.customerService = customerService;
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/customers/{customerId}")
    public CustomerResponse getCustomer(
            @PathVariable Long customerId
    ) {
        return customerService.getCustomer(customerId);
    }

    @GetMapping("/shops/{shopId}/customers")
    public List<CustomerResponse> getCustomers(
            @PathVariable Long shopId
    ) {
        return customerService.getCustomers(shopId);
    }

    @PutMapping("/customers/{customerId}")
    public CustomerResponse updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return customerService.updateCustomer(
                customerId,
                request
        );
    }
}