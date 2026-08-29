package org.example.backend.inventory;

import jakarta.validation.Valid;
import org.example.backend.inventory.dto.ExpiredInventoryResponse;
import org.example.backend.inventory.dto.InventoryAdjustmentRequest;
import org.example.backend.inventory.dto.InventoryAdjustmentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryAdjustmentController {

    private final InventoryAdjustmentService service;

    public InventoryAdjustmentController(
            InventoryAdjustmentService service
    ) {
        this.service = service;
    }

    @PostMapping("/adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryAdjustmentResponse adjust(
            @Valid
            @RequestBody
            InventoryAdjustmentRequest request
    ) {
        return service.adjust(request);
    }

    @GetMapping("/shops/{shopId}/expired")
    public List<ExpiredInventoryResponse> getExpiredInventory(
            @PathVariable Long shopId
    ) {
        return service.getExpiredInventory(shopId);
    }
}