package com.sujit.inventory_service.controller;

import com.sujit.inventory_service.dto.ApiResponse;
import com.sujit.inventory_service.dto.InventoryDto;
import com.sujit.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryDto.InventoryResponse>> getInventory(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(200, inventoryService.getInventory(productId)));
    }

    @PostMapping("/restock")
    public ResponseEntity<ApiResponse<InventoryDto.InventoryResponse>> restock(@Valid @RequestBody InventoryDto.RestockRequest request) {
        return ResponseEntity.ok(ApiResponse.success(200, inventoryService.restock(request)));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryDto.InventoryResponse>>> lowStock(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(ApiResponse.success(200, inventoryService.getLowStock(threshold)));
    }
}
