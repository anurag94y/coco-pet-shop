package org.example.backend.purchase;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.product.Product;
import org.example.backend.product.ProductRepository;
import org.example.backend.purchase.dto.*;
import org.example.backend.shop.Shop;
import org.example.backend.shop.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class DealerBillImportService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final PurchaseService purchaseService;
    private final PurchaseRepository purchaseRepository;

    public DealerBillImportService(
            ProductRepository productRepository,
            ShopRepository shopRepository,
            PurchaseService purchaseService,
            PurchaseRepository purchaseRepository
    ) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.purchaseService = purchaseService;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public PurchaseResponse importBill(
            ConfirmDealerBillImportRequest request
    ) {

        if (request.billNumber() != null
                && !request.billNumber().isBlank()) {

            boolean alreadyExists =
                    purchaseRepository
                            .existsByShopIdAndDealerIdAndBillNumber(
                                    request.shopId(),
                                    request.dealerId(),
                                    request.billNumber()
                            );

            if (alreadyExists) {
                throw new IllegalArgumentException(
                        "Bill already imported: "
                                + request.billNumber()
                );
            }
        }

        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shop not found: " + request.shopId()
                        )
                );

        List<CreatePurchaseItemRequest> purchaseItems =
                new ArrayList<>();

        for (ConfirmDealerBillItemRequest item : request.items()) {

            Product product =
                    resolveProduct(shop, item);

            purchaseItems.add(
                    new CreatePurchaseItemRequest(
                            product.getId(),
                            item.quantity(),
                            item.mrp(),
                            item.dealerDiscountPercentage(),
                            item.purchasePrice(),
                            item.expiryDate(),
                            BigDecimal.ZERO
                    )
            );
        }

        CreatePurchaseRequest purchaseRequest =
                new CreatePurchaseRequest(
                        request.shopId(),
                        request.dealerId(),
                        request.createdBy(),
                        request.billNumber(),
                        request.billDate(),
                        request.billImagePath(),
                        purchaseItems
                );

        return purchaseService.createPurchase(
                purchaseRequest
        );
    }

    private Product resolveProduct(
            Shop shop,
            ConfirmDealerBillItemRequest item
    ) {

        if (item.productId() != null) {

            Product product =
                    productRepository.findById(item.productId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Product not found: "
                                                    + item.productId()
                                    )
                            );

            if (!product.getShop().getId()
                    .equals(shop.getId())) {

                throw new IllegalArgumentException(
                        "Product does not belong to shop"
                );
            }

            return product;
        }

        Product product = new Product();

        product.setShop(shop);
        product.setName(item.productName());

        return productRepository.save(product);
    }
}