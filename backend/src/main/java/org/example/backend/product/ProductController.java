package org.example.backend.product;

import jakarta.validation.Valid;
import org.example.backend.product.dto.CreateProductRequest;
import org.example.backend.product.dto.ProductMatchResponse;
import org.example.backend.product.dto.ProductResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return productService.createProduct(request);
    }

    @GetMapping("/products/{productId}")
    public ProductResponse getProduct(
            @PathVariable Long productId
    ) {
        return productService.getProduct(productId);
    }

    @GetMapping("/shops/{shopId}/products")
    public List<ProductResponse> getProducts(
            @PathVariable Long shopId
    ) {
        return productService.getProducts(shopId);
    }

    @GetMapping("/api/shops/{shopId}/products/search")
    public List<ProductMatchResponse> searchProducts(
            @PathVariable Long shopId,
            @RequestParam String name
    ) {
        return productService.searchProducts(
                shopId,
                name
        );
    }
}