package com.sujit.inventory_service.controller;

import com.sujit.inventory_service.dto.InventoryResponse;
import com.sujit.inventory_service.dto.RestockRequest;
import com.sujit.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @PostMapping("/restock")
    public ResponseEntity<InventoryResponse> restock(@Valid @RequestBody RestockRequest request) {
        return ResponseEntity.ok(inventoryService.restock(request));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<InventoryResponse>> lowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(inventoryService.getLowStock(threshold));
    }
}
