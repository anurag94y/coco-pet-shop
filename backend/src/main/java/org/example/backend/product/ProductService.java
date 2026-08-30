package org.example.backend.product;

import org.example.backend.common.exception.ResourceNotFoundException;
import org.example.backend.product.dto.CreateProductRequest;
import org.example.backend.product.dto.ProductMatchResponse;
import org.example.backend.product.dto.ProductResponse;
import org.example.backend.shop.Shop;
import org.example.backend.shop.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    public ProductService(
            ProductRepository productRepository,
            ShopRepository shopRepository
    ) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        Shop shop = shopRepository.findById(request.shopId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Shop not found: " + request.shopId()
                        )
                );

        Product product = new Product();
        product.setShop(shop);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(request.category());

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + productId
                        )
                );

        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts(Long shopId) {

        return productRepository.findByShopId(shopId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductMatchResponse> searchProducts(
            Long shopId,
            String name
    ) {
        if (name == null || name.isBlank()) {
            return List.of();
        }

        return productRepository
                .findTop10ByShopIdAndNameContainingIgnoreCase(
                        shopId,
                        name.trim()
                )
                .stream()
                .map(product ->
                        new ProductMatchResponse(
                                product.getId(),
                                product.getName()
                        )
                )
                .toList();
    }

    private ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getShop().getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}