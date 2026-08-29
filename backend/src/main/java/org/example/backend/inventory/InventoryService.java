package org.example.backend.inventory;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.inventory.dto.ExpiredInventoryResponse;
import org.example.backend.inventory.dto.InventoryBatchResponse;
import org.example.backend.inventory.dto.InventorySummaryResponse;
import org.example.backend.inventory.dto.ProductInventoryResponse;
import org.example.backend.product.Product;
import org.example.backend.product.ProductRepository;
import org.example.backend.shop.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    public InventoryService(
            InventoryBatchRepository inventoryBatchRepository,
            ProductRepository productRepository,
            ShopRepository shopRepository
    ) {
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
    }

    @Transactional(readOnly = true)
    public ProductInventoryResponse getProductInventory(
            Long shopId,
            Long productId
    ) {

        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException(
                    "Shop not found: " + shopId
            );
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + productId
                        )
                );

        if (!product.getShop().getId().equals(shopId)) {
            throw new ResourceNotFoundException(
                    "Product not found for shop: " + shopId
            );
        }

        List<InventoryBatch> batches =
                inventoryBatchRepository
                        .findByShopIdAndProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(
                                shopId,
                                productId,
                                0
                        );

        List<InventoryBatchResponse> batchResponses =
                batches.stream()
                        .map(this::toBatchResponse)
                        .toList();

        int totalQuantity =
                batches.stream()
                        .mapToInt(InventoryBatch::getQuantity)
                        .sum();

        return new ProductInventoryResponse(
                product.getId(),
                product.getName(),
                totalQuantity,
                batchResponses
        );
    }

    @Transactional(readOnly = true)
    public List<InventorySummaryResponse> getShopInventory(Long shopId) {

        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException(
                    "Shop not found: " + shopId
            );
        }

        List<InventoryBatch> batches =
                inventoryBatchRepository
                        .findByShopIdAndQuantityGreaterThan(
                                shopId,
                                0
                        );

        return batches.stream()
                .collect(Collectors.groupingBy(
                        batch -> batch.getProduct().getId()
                ))
                .values()
                .stream()
                .map(this::toSummaryResponse)
                .toList();
    }

    private InventoryBatchResponse toBatchResponse(
            InventoryBatch batch
    ) {
        return new InventoryBatchResponse(
                batch.getId(),
                batch.getPurchase().getId(),
                batch.getQuantity(),
                batch.getExpiryDate(),
                batch.getPurchasePrice(),
                batch.getMrp(),
                batch.getSellingDiscountPercentage(),
                calculateSellingPrice(batch)
        );
    }

    private BigDecimal calculateSellingPrice(
            InventoryBatch batch
    ) {
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

    private InventorySummaryResponse toSummaryResponse(
            List<InventoryBatch> batches
    ) {

        InventoryBatch firstBatch = batches.getFirst();

        int totalQuantity =
                batches.stream()
                        .mapToInt(InventoryBatch::getQuantity)
                        .sum();

        LocalDate nearestExpiryDate =
                batches.stream()
                        .map(InventoryBatch::getExpiryDate)
                        .filter(Objects::nonNull)
                        .min(LocalDate::compareTo)
                        .orElse(null);

        BigDecimal lowestSellingPrice =
                batches.stream()
                        .map(this::calculateSellingPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        return new InventorySummaryResponse(
                firstBatch.getProduct().getId(),
                firstBatch.getProduct().getName(),
                totalQuantity,
                nearestExpiryDate,
                lowestSellingPrice
        );
    }
}