package org.example.backend.inventory;

import org.example.backend.inventory.dto.InventorySummaryResponse;
import org.example.backend.inventory.dto.ProductInventoryResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService
    ) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/shops/{shopId}/products/{productId}/inventory")
    public ProductInventoryResponse getProductInventory(
            @PathVariable Long shopId,
            @PathVariable Long productId
    ) {
        return inventoryService.getProductInventory(
                shopId,
                productId
        );
    }

    @GetMapping("/shops/{shopId}/inventory")
    public List<InventorySummaryResponse> getShopInventory(
            @PathVariable Long shopId
    ) {
        return inventoryService.getShopInventory(shopId);
    }
}