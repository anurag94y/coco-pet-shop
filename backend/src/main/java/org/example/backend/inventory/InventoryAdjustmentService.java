package org.example.backend.inventory;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.inventory.dto.ExpiredInventoryResponse;
import org.example.backend.inventory.dto.InventoryAdjustmentRequest;
import org.example.backend.inventory.dto.InventoryAdjustmentResponse;
import org.example.backend.shop.ShopRepository;
import org.example.backend.user.AppUser;
import org.example.backend.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryAdjustmentService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final AppUserRepository appUserRepository;
    private final ShopRepository shopRepository;

    public InventoryAdjustmentService(
            InventoryBatchRepository inventoryBatchRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            AppUserRepository appUserRepository,
            ShopRepository shopRepository
    ) {
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.appUserRepository = appUserRepository;
        this.shopRepository = shopRepository;
    }

    @Transactional
    public InventoryAdjustmentResponse adjust(
            InventoryAdjustmentRequest request
    ) {

        String type = request.type().toUpperCase();

        if (!type.equals("DAMAGED")
                && !type.equals("EXPIRED")) {
            throw new IllegalArgumentException(
                    "Invalid adjustment type: " + request.type()
            );
        }

        InventoryBatch batch =
                inventoryBatchRepository
                        .findByIdForUpdate(request.batchId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Inventory batch not found: "
                                                + request.batchId()
                                )
                        );

        AppUser user =
                appUserRepository.findById(request.createdBy())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found: "
                                                + request.createdBy()
                                )
                        );

        if (!batch.getShop().getId()
                .equals(user.getShop().getId())) {

            throw new IllegalArgumentException(
                    "User does not belong to the batch shop"
            );
        }

        if (batch.getQuantity() < request.quantity()) {

            throw new IllegalArgumentException(
                    "Insufficient stock in batch. Available: "
                            + batch.getQuantity()
                            + ", requested: "
                            + request.quantity()
            );
        }

        batch.setQuantity(
                batch.getQuantity() - request.quantity()
        );

        InventoryTransaction transaction =
                new InventoryTransaction();

        transaction.setShop(batch.getShop());
        transaction.setProduct(batch.getProduct());
        transaction.setBatch(batch);
        transaction.setTransactionType(type);
        transaction.setQuantity(-request.quantity());

        transaction.setReferenceType("MANUAL_ADJUSTMENT");
        transaction.setReferenceId(null);

        transaction.setCreatedBy(user);

        inventoryTransactionRepository.save(transaction);

        return new InventoryAdjustmentResponse(
                batch.getId(),
                batch.getProduct().getId(),
                type,
                request.quantity(),
                batch.getQuantity()
        );
    }

    @Transactional(readOnly = true)
    public List<ExpiredInventoryResponse> getExpiredInventory(
            Long shopId
    ) {

        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException(
                    "Shop not found: " + shopId
            );
        }

        return inventoryBatchRepository
                .findByShopIdAndExpiryDateBeforeAndQuantityGreaterThan(
                        shopId,
                        LocalDate.now(),
                        0
                )
                .stream()
                .map(batch ->
                        new ExpiredInventoryResponse(
                                batch.getId(),
                                batch.getProduct().getId(),
                                batch.getProduct().getName(),
                                batch.getExpiryDate(),
                                batch.getQuantity()
                        )
                )
                .toList();
    }
}