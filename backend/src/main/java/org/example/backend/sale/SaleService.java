package org.example.backend.sale;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.customer.Customer;
import org.example.backend.customer.CustomerRepository;
import org.example.backend.inventory.InventoryBatch;
import org.example.backend.inventory.InventoryBatchRepository;
import org.example.backend.inventory.InventoryTransaction;
import org.example.backend.inventory.InventoryTransactionRepository;
import org.example.backend.product.Product;
import org.example.backend.product.ProductRepository;
import org.example.backend.sale.dto.*;
import org.example.backend.shop.Shop;
import org.example.backend.shop.ShopRepository;
import org.example.backend.user.AppUser;
import org.example.backend.user.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final PaymentRepository paymentRepository;

    private final ShopRepository shopRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;

    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    private final SaleBillPdfService saleBillPdfService;

    public SaleService(
            SaleRepository saleRepository,
            SaleItemRepository saleItemRepository,
            PaymentRepository paymentRepository,
            ShopRepository shopRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            AppUserRepository appUserRepository,
            InventoryBatchRepository inventoryBatchRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            SaleBillPdfService saleBillPdfService
    ) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.paymentRepository = paymentRepository;
        this.shopRepository = shopRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.appUserRepository = appUserRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.inventoryTransactionRepository =
                inventoryTransactionRepository;
        this.saleBillPdfService = saleBillPdfService;
    }

    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {

        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shop not found: " + request.shopId()
                        )
                );

        Customer customer =
                customerRepository.findById(request.customerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found: "
                                                + request.customerId()
                                )
                        );

        AppUser createdBy =
                appUserRepository.findById(request.createdBy())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found: "
                                                + request.createdBy()
                                )
                        );

        if (!customer.getShop().getId().equals(shop.getId())) {
            throw new IllegalArgumentException(
                    "Customer does not belong to shop: " + shop.getId()
            );
        }

        if (!createdBy.getShop().getId().equals(shop.getId())) {
            throw new IllegalArgumentException(
                    "User does not belong to shop: " + shop.getId()
            );
        }

        Sale sale = new Sale();

        sale.setShop(shop);
        sale.setCustomer(customer);
        sale.setBillNumber(request.billNumber());
        sale.setBillDate(LocalDateTime.now());
        sale.setSubtotal(BigDecimal.ZERO);
        sale.setDiscountAmount(BigDecimal.ZERO);
        sale.setTotalAmount(BigDecimal.ZERO);
        sale.setCreatedBy(createdBy);

        Sale savedSale = saleRepository.save(sale);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (CreateSaleItemRequest item : request.items()) {

            Product product =
                    productRepository.findById(item.productId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Product not found: "
                                                    + item.productId()
                                    )
                            );

            if (!product.getShop().getId().equals(shop.getId())) {
                throw new IllegalArgumentException(
                        "Product does not belong to shop: "
                                + shop.getId()
                );
            }

            AllocationResult allocation =
                    allocateProduct(
                            savedSale,
                            shop,
                            createdBy,
                            product,
                            item.quantity()
                    );

            subtotal = subtotal.add(allocation.subtotal());
            total = total.add(allocation.total());
        }

        BigDecimal discountAmount = subtotal.subtract(total);

        savedSale.setSubtotal(subtotal);
        savedSale.setDiscountAmount(discountAmount);
        savedSale.setTotalAmount(total);

        Payment payment = new Payment();

        payment.setSale(savedSale);
        payment.setAmount(total);
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentStatus("PAID");
        payment.setTransactionReference(
                request.transactionReference()
        );

        paymentRepository.save(payment);

        return toResponse(savedSale, payment);
    }

    @Transactional(readOnly = true)
    public SaleResponse getSale(Long saleId) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sale not found: " + saleId
                        )
                );

        Payment payment =
                paymentRepository
                        .findBySaleIdOrderByCreatedAtDesc(saleId)
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Payment not found for sale: " + saleId
                                )
                        );

        return toResponse(sale, payment);
    }

    @Transactional(readOnly = true)
    public Page<SaleSummaryResponse> getCustomerSales(
            Long customerId,
            Pageable pageable
    ) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found: " + customerId
            );
        }

        return saleRepository
                .findByCustomerIdOrderByBillDateDesc(
                        customerId,
                        pageable
                )
                .map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<SaleSummaryResponse> getShopSales(
            Long shopId,
            Pageable pageable
    ) {

        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException(
                    "Shop not found: " + shopId
            );
        }

        return saleRepository
                .findByShopIdOrderByBillDateDesc(
                        shopId,
                        pageable
                )
                .map(this::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public byte[] generateBillPdf(Long saleId) {

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Sale not found: " + saleId
                        )
                );

        Payment payment =
                paymentRepository
                        .findBySaleIdOrderByCreatedAtDesc(saleId)
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Payment not found for sale: "
                                                + saleId
                                )
                        );

        SaleResponse saleResponse =
                toResponse(sale, payment);

        return saleBillPdfService.generateBill(
                saleResponse,
                sale.getShop()
        );
    }

    private record AllocationResult(
            BigDecimal subtotal,
            BigDecimal total
    ) {
    }
    private SaleResponse toResponse(
            Sale sale,
            Payment payment
    ) {

        List<SaleItemResponse> items =
                saleItemRepository.findBySaleId(sale.getId())
                        .stream()
                        .map(item ->
                                new SaleItemResponse(
                                        item.getProduct().getId(),
                                        item.getProduct().getName(),
                                        item.getInventoryBatch().getId(),
                                        item.getQuantity(),
                                        item.getMrp(),
                                        item.getDiscountPercentage(),
                                        item.getUnitPrice(),
                                        item.getTotalPrice()
                                )
                        )
                        .toList();

        return new SaleResponse(
                sale.getId(),
                sale.getShop().getId(),
                sale.getCustomer().getId(),
                sale.getCustomer().getName(),
                sale.getBillNumber(),
                sale.getBillDate(),
                sale.getSubtotal(),
                sale.getDiscountAmount(),
                sale.getTotalAmount(),
                sale.getCreatedBy().getId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getTransactionReference(),
                items
        );
    }

    private AllocationResult allocateProduct(
            Sale sale,
            Shop shop,
            AppUser createdBy,
            Product product,
            int requestedQuantity
    ) {

        List<InventoryBatch> batches =
                inventoryBatchRepository.findAvailableBatchesForSale(
                        shop.getId(),
                        product.getId(),
                        LocalDate.now()
                );

        int availableQuantity =
                batches.stream()
                        .mapToInt(InventoryBatch::getQuantity)
                        .sum();

        if (availableQuantity < requestedQuantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product: "
                            + product.getName()
                            + ". Requested: "
                            + requestedQuantity
                            + ", available: "
                            + availableQuantity
            );
        }

        int remaining = requestedQuantity;

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (InventoryBatch batch : batches) {

            if (remaining == 0) {
                break;
            }

            int quantityFromBatch =
                    Math.min(
                            remaining,
                            batch.getQuantity()
                    );

            // Customer-facing price after batch discount
            BigDecimal unitPrice =
                    calculateSellingPrice(batch);

            // MRP × quantity
            BigDecimal mrpTotal =
                    batch.getMrp()
                            .multiply(
                                    BigDecimal.valueOf(quantityFromBatch)
                            );

            // Selling price × quantity
            BigDecimal totalPrice =
                    unitPrice.multiply(
                            BigDecimal.valueOf(quantityFromBatch)
                    );

            SaleItem saleItem = new SaleItem();

            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setInventoryBatch(batch);
            saleItem.setQuantity(quantityFromBatch);
            saleItem.setMrp(batch.getMrp());
            saleItem.setDiscountPercentage(
                    batch.getSellingDiscountPercentage()
            );
            saleItem.setUnitPrice(unitPrice);
            saleItem.setTotalPrice(totalPrice);

            saleItemRepository.save(saleItem);

            // Reduce available inventory
            batch.setQuantity(
                    batch.getQuantity() - quantityFromBatch
            );

            InventoryTransaction transaction =
                    new InventoryTransaction();

            transaction.setShop(shop);
            transaction.setProduct(product);
            transaction.setBatch(batch);
            transaction.setTransactionType("SALE");
            transaction.setQuantity(-quantityFromBatch);
            transaction.setReferenceType("SALE");
            transaction.setReferenceId(sale.getId());
            transaction.setCreatedBy(createdBy);

            inventoryTransactionRepository.save(transaction);

            subtotal = subtotal.add(mrpTotal);
            total = total.add(totalPrice);

            remaining -= quantityFromBatch;
        }

        return new AllocationResult(
                subtotal,
                total
        );
    }

    private BigDecimal calculateSellingPrice(InventoryBatch batch) {

        BigDecimal discount =
                batch.getSellingDiscountPercentage()
                        .divide(
                                BigDecimal.valueOf(100),
                                4,
                                RoundingMode.HALF_UP
                        );

        return batch.getMrp()
                .multiply(BigDecimal.ONE.subtract(discount))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private SaleSummaryResponse toSummaryResponse(
            Sale sale
    ) {

        Payment payment =
                paymentRepository
                        .findBySaleIdOrderByCreatedAtDesc(
                                sale.getId()
                        )
                        .stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Payment not found for sale: "
                                                + sale.getId()
                                )
                        );

        return new SaleSummaryResponse(
                sale.getId(),
                sale.getBillNumber(),
                sale.getBillDate(),
                sale.getCustomer().getId(),
                sale.getCustomer().getName(),
                sale.getTotalAmount(),
                payment.getPaymentStatus()
        );
    }
}