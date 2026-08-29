package org.example.backend.customer;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.customer.dto.CreateCustomerRequest;
import org.example.backend.customer.dto.CustomerResponse;
import org.example.backend.customer.dto.UpdateCustomerRequest;
import org.example.backend.shop.Shop;
import org.example.backend.shop.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ShopRepository shopRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            ShopRepository shopRepository
    ) {
        this.customerRepository = customerRepository;
        this.shopRepository = shopRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(
            CreateCustomerRequest request
    ) {

        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shop not found: " + request.shopId()
                        )
                );

        Customer customer = new Customer();

        customer.setShop(shop);
        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());

        Customer savedCustomer =
                customerRepository.save(customer);

        return toResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + customerId
                        )
                );

        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomers(Long shopId) {

        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException(
                    "Shop not found: " + shopId
            );
        }

        return customerRepository
                .findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CustomerResponse updateCustomer(
            Long customerId,
            UpdateCustomerRequest request
    ) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + customerId
                        )
                );

        customer.setName(request.name());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());

        return toResponse(customer);
    }

    private CustomerResponse toResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getShop().getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}