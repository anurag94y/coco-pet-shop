package org.example.backend.purchase;

import org.example.backend.dealer.Dealer;
import org.example.backend.dealer.DealerRepository;
import org.example.backend.product.Product;
import org.example.backend.product.ProductRepository;
import org.example.backend.purchase.dto.ConfirmDealerBillImportRequest;
import org.example.backend.purchase.dto.ConfirmDealerBillItemRequest;
import org.example.backend.purchase.dto.CreatePurchaseItemRequest;
import org.example.backend.purchase.dto.CreatePurchaseRequest;
import org.example.backend.purchase.dto.PurchaseResponse;
import org.example.backend.shop.Shop;
import org.example.backend.shop.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DealerBillImportService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final PurchaseService purchaseService;
    private final DealerRepository dealerRepository;
    private final PurchaseRepository purchaseRepository;

    public DealerBillImportService(
            ProductRepository productRepository,
            ShopRepository shopRepository,
            PurchaseService purchaseService,
            DealerRepository dealerRepository,
            PurchaseRepository purchaseRepository
    ) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
        this.purchaseService = purchaseService;
        this.dealerRepository = dealerRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public PurchaseResponse importBill(
            ConfirmDealerBillImportRequest request
    ) {

        Dealer dealer =
                resolveDealer(request);

        if (request.billNumber() != null
                && !request.billNumber().isBlank()) {

            boolean alreadyImported =
                    purchaseRepository
                            .existsByShopIdAndDealerIdAndBillNumber(
                                    request.shopId(),
                                    dealer.getId(),
                                    request.billNumber().trim()
                            );

            if (alreadyImported) {
                throw new IllegalArgumentException(
                        "Dealer bill already imported: "
                                + request.billNumber()
                );
            }
        }

        Shop shop =
                shopRepository.findById(request.shopId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Shop not found: "
                                                + request.shopId()
                                )
                        );

        List<CreatePurchaseItemRequest> purchaseItems =
                new ArrayList<>();

        for (ConfirmDealerBillItemRequest item :
                request.items()) {

            Product product =
                    resolveProduct(
                            shop,
                            item
                    );

            purchaseItems.add(
                    new CreatePurchaseItemRequest(
                            product.getId(),
                            item.quantity(),
                            item.mrp(),
                            item.dealerDiscountPercentage(),
                            item.purchasePrice(),
                            item.expiryDate(),
                            item.sellingDiscountPercentage()
                    )
            );
        }

        CreatePurchaseRequest purchaseRequest =
                new CreatePurchaseRequest(
                        request.shopId(),
                        dealer.getId(),
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
                    productRepository
                            .findById(item.productId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Product not found: "
                                                    + item.productId()
                                    )
                            );

            if (!product.getShop()
                    .getId()
                    .equals(shop.getId())) {

                throw new IllegalArgumentException(
                        "Product does not belong to shop"
                );
            }

            return product;
        }

        if (item.productName() == null
                || item.productName().isBlank()) {

            throw new IllegalArgumentException(
                    "productName is required when productId is not provided"
            );
        }

        Product product = new Product();

        product.setShop(shop);
        product.setName(
                item.productName().trim()
        );

        return productRepository.save(product);
    }

    private Dealer resolveDealer(
            ConfirmDealerBillImportRequest request
    ) {

        if (request.dealerId() != null) {

            Dealer dealer =
                    dealerRepository
                            .findById(request.dealerId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Dealer not found: "
                                                    + request.dealerId()
                                    )
                            );

            if (!dealer.getShop()
                    .getId()
                    .equals(request.shopId())) {

                throw new IllegalArgumentException(
                        "Dealer does not belong to shop"
                );
            }

            return dealer;
        }

        if (request.dealerName() == null
                || request.dealerName().isBlank()) {

            throw new IllegalArgumentException(
                    "dealerId or dealerName is required"
            );
        }

        return dealerRepository
                .findByShopIdAndNameIgnoreCase(
                        request.shopId(),
                        request.dealerName().trim()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Dealer not found for name: "
                                        + request.dealerName()
                                        + ". Please create/review dealer first."
                        )
                );
    }
}