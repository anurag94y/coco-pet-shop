package org.example.backend.purchase;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.dealer.Dealer;
import org.example.backend.dealer.DealerLedgerEntry;
import org.example.backend.dealer.DealerLedgerEntryRepository;
import org.example.backend.dealer.DealerRepository;
import org.example.backend.inventory.InventoryBatch;
import org.example.backend.inventory.InventoryBatchRepository;
import org.example.backend.inventory.InventoryTransaction;
import org.example.backend.inventory.InventoryTransactionRepository;
import org.example.backend.product.Product;
import org.example.backend.product.ProductRepository;
import org.example.backend.purchase.dto.CreatePurchaseItemRequest;
import org.example.backend.purchase.dto.CreatePurchaseRequest;
import org.example.backend.purchase.dto.PurchaseItemResponse;
import org.example.backend.purchase.dto.PurchaseResponse;
import org.example.backend.shop.Shop;
import org.example.backend.shop.ShopRepository;
import org.example.backend.user.AppUser;
import org.example.backend.user.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    private final ShopRepository shopRepository;
    private final DealerRepository dealerRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;

    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    private final DealerLedgerEntryRepository dealerLedgerEntryRepository;

    public PurchaseService(
            PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository,
            ShopRepository shopRepository,
            DealerRepository dealerRepository,
            ProductRepository productRepository,
            AppUserRepository appUserRepository,
            InventoryBatchRepository inventoryBatchRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            DealerLedgerEntryRepository dealerLedgerEntryRepository
    ) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.shopRepository = shopRepository;
        this.dealerRepository = dealerRepository;
        this.productRepository = productRepository;
        this.appUserRepository = appUserRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.inventoryTransactionRepository =
                inventoryTransactionRepository;
        this.dealerLedgerEntryRepository =
                dealerLedgerEntryRepository;
    }

    @Transactional
    public PurchaseResponse createPurchase(CreatePurchaseRequest request) {

        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shop not found: " + request.shopId()
                        )
                );

        Dealer dealer = dealerRepository.findById(request.dealerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Dealer not found: " + request.dealerId()
                        )
                );

        AppUser createdBy = appUserRepository.findById(request.createdBy())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + request.createdBy()
                        )
                );

        BigDecimal totalAmount = calculateTotal(request);

        Purchase purchase = new Purchase();

        purchase.setShop(shop);
        purchase.setDealer(dealer);
        purchase.setBillNumber(request.billNumber());
        purchase.setBillDate(request.billDate());
        purchase.setBillImagePath(request.billImagePath());
        purchase.setTotalAmount(totalAmount);
        purchase.setCreatedBy(createdBy);

        Purchase savedPurchase = purchaseRepository.save(purchase);
        List<PurchaseItemResponse> responseItems =
                new ArrayList<>();

        for (CreatePurchaseItemRequest itemRequest : request.items()) {

            Product product =
                    productRepository.findById(itemRequest.productId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Product not found: "
                                                    + itemRequest.productId()
                                    )
                            );

            PurchaseItem purchaseItem = new PurchaseItem();

            purchaseItem.setPurchase(savedPurchase);
            purchaseItem.setProduct(product);
            purchaseItem.setQuantity(itemRequest.quantity());
            purchaseItem.setMrp(itemRequest.mrp());
            purchaseItem.setDealerDiscountPercentage(
                    itemRequest.dealerDiscountPercentage()
            );
            purchaseItem.setPurchasePrice(
                    itemRequest.purchasePrice()
            );

            PurchaseItem savedPurchaseItem =
                    purchaseItemRepository.save(purchaseItem);

            InventoryBatch batch = new InventoryBatch();

            batch.setShop(shop);
            batch.setProduct(product);
            batch.setPurchase(savedPurchase);
            batch.setPurchaseItem(savedPurchaseItem);
            batch.setExpiryDate(itemRequest.expiryDate());
            batch.setQuantity(itemRequest.quantity());
            batch.setPurchasePrice(itemRequest.purchasePrice());
            batch.setMrp(itemRequest.mrp());
            batch.setSellingDiscountPercentage(
                    itemRequest.sellingDiscountPercentage()
            );

            InventoryBatch savedBatch =
                    inventoryBatchRepository.save(batch);

            InventoryTransaction transaction =
                    new InventoryTransaction();

            transaction.setShop(shop);
            transaction.setProduct(product);
            transaction.setBatch(savedBatch);
            transaction.setTransactionType("PURCHASE");
            transaction.setQuantity(itemRequest.quantity());
            transaction.setReferenceType("PURCHASE");
            transaction.setReferenceId(savedPurchase.getId());
            transaction.setCreatedBy(createdBy);

            inventoryTransactionRepository.save(transaction);

            responseItems.add(
                    new PurchaseItemResponse(
                            product.getId(),
                            product.getName(),
                            savedBatch.getId(),
                            itemRequest.quantity(),
                            itemRequest.mrp(),
                            itemRequest.dealerDiscountPercentage(),
                            itemRequest.purchasePrice(),
                            itemRequest.expiryDate(),
                            itemRequest.sellingDiscountPercentage()
                    )
            );
        }

        DealerLedgerEntry ledgerEntry =
                new DealerLedgerEntry();

        ledgerEntry.setShop(shop);
        ledgerEntry.setDealer(dealer);
        ledgerEntry.setPurchase(savedPurchase);
        ledgerEntry.setTransactionType("PURCHASE");
        ledgerEntry.setAmount(totalAmount);
        ledgerEntry.setDescription(
                "Purchase bill " + request.billNumber()
        );
        ledgerEntry.setCreatedBy(createdBy);

        dealerLedgerEntryRepository.save(ledgerEntry);
        return new PurchaseResponse(
                savedPurchase.getId(),
                shop.getId(),
                dealer.getId(),
                dealer.getName(),
                savedPurchase.getBillNumber(),
                savedPurchase.getBillDate(),
                savedPurchase.getBillImagePath(),
                savedPurchase.getTotalAmount(),
                createdBy.getId(),
                responseItems
        );
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchase(Long purchaseId) {

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Purchase not found: " + purchaseId
                        )
                );

        return toResponse(purchase);
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getDealerPurchases(Long dealerId) {

        if (!dealerRepository.existsById(dealerId)) {
            throw new ResourceNotFoundException(
                    "Dealer not found: " + dealerId
            );
        }

        return purchaseRepository
                .findByDealerIdOrderByBillDateDesc(dealerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getShopPurchases(Long shopId) {

        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException(
                    "Shop not found: " + shopId
            );
        }

        return purchaseRepository
                .findByShopIdOrderByBillDateDesc(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PurchaseResponse toResponse(Purchase purchase) {

        List<PurchaseItemResponse> items =
                purchaseItemRepository
                        .findByPurchaseId(purchase.getId())
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getShop().getId(),
                purchase.getDealer().getId(),
                purchase.getDealer().getName(),
                purchase.getBillNumber(),
                purchase.getBillDate(),
                purchase.getBillImagePath(),
                purchase.getTotalAmount(),
                purchase.getCreatedBy().getId(),
                items
        );
    }

    private PurchaseItemResponse toItemResponse(
            PurchaseItem purchaseItem
    ) {

        InventoryBatch batch =
                inventoryBatchRepository
                        .findByPurchaseItemId(purchaseItem.getId())
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Inventory batch not found for purchase item: "
                                                + purchaseItem.getId()
                                )
                        );

        return new PurchaseItemResponse(
                purchaseItem.getProduct().getId(),
                purchaseItem.getProduct().getName(),
                batch.getId(),
                purchaseItem.getQuantity(),
                purchaseItem.getMrp(),
                purchaseItem.getDealerDiscountPercentage(),
                purchaseItem.getPurchasePrice(),
                batch.getExpiryDate(),
                batch.getSellingDiscountPercentage()
        );
    }

    private BigDecimal calculateTotal(
            CreatePurchaseRequest request
    ) {

        return request.items()
                .stream()
                .map(item ->
                        item.purchasePrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.quantity()
                                        )
                                )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}