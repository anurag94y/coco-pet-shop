package org.example.backend.purchase;

import org.example.backend.dealer.DealerRepository;
import org.example.backend.product.Product;
import org.example.backend.product.ProductRepository;
import org.example.backend.purchase.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DealerBillReviewService {

    private final DealerRepository dealerRepository;
    private final ProductRepository productRepository;

    public DealerBillReviewService(
            DealerRepository dealerRepository,
            ProductRepository productRepository
    ) {
        this.dealerRepository = dealerRepository;
        this.productRepository = productRepository;
    }

    public DealerBillReviewResponse buildReview(
            Long shopId,
            DealerBillParseResponse extraction
    ) {

        DealerMatchResponse dealerMatch =
                findDealerMatch(
                        shopId,
                        extraction.dealerName()
                );

        List<DealerBillReviewItemResponse> items =
                extraction.items()
                        .stream()
                        .map(item ->
                                buildItem(
                                        shopId,
                                        item
                                )
                        )
                        .toList();

        boolean needsReview =
                extraction.needsReview()
                        || dealerMatch == null
                        || items.stream()
                        .anyMatch(item ->
                                item.productSuggestions()
                                        .isEmpty()
                        );

        return new DealerBillReviewResponse(
                extraction.storedPath(),
                extraction.dealerName(),
                extraction.billNumber(),
                extraction.billDate(),
                extraction.invoiceTotal(),
                needsReview,
                dealerMatch,
                items
        );
    }

    private DealerMatchResponse findDealerMatch(
            Long shopId,
            String dealerName
    ) {

        if (dealerName == null
                || dealerName.isBlank()) {
            return null;
        }

        return dealerRepository
                .findByShopIdAndNameIgnoreCase(
                        shopId,
                        dealerName.trim()
                )
                .map(dealer ->
                        new DealerMatchResponse(
                                dealer.getId(),
                                dealer.getName(),
                                true
                        )
                )
                .orElse(null);
    }

    private DealerBillReviewItemResponse buildItem(
            Long shopId,
            DealerBillItemDraft item
    ) {

        List<ProductSuggestionResponse> suggestions =
                findProductSuggestions(
                        shopId,
                        item.name()
                );

        return new DealerBillReviewItemResponse(
                item.name(),
                item.mrp(),
                item.dealerDiscountPercentage(),
                item.quantity(),
                item.purchasePrice(),
                item.totalPrice(),
                item.needsReview(),
                suggestions
        );
    }

    private List<ProductSuggestionResponse> findProductSuggestions(
            Long shopId,
            String productName
    ) {

        if (productName == null
                || productName.isBlank()) {
            return List.of();
        }

        var exact =
                productRepository
                        .findByShopIdAndNameIgnoreCase(
                                shopId,
                                productName.trim()
                        );

        if (exact.isPresent()) {

            Product product = exact.get();

            return List.of(
                    new ProductSuggestionResponse(
                            product.getId(),
                            product.getName(),
                            true
                    )
            );
        }

        return productRepository
                .findTop10ByShopIdAndNameContainingIgnoreCase(
                        shopId,
                        productName.trim()
                )
                .stream()
                .map(product ->
                        new ProductSuggestionResponse(
                                product.getId(),
                                product.getName(),
                                false
                        )
                )
                .toList();
    }
}