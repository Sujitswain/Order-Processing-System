package com.sujit.inventory_service.util;

import com.sujit.inventory_service.dto.InventoryResponse;
import com.sujit.inventory_service.entity.ProductStock;

public class ApplicationUtil {

    public static InventoryResponse toResponse(ProductStock stock) {
        InventoryResponse response = new InventoryResponse();
        response.setProductId(stock.getProductId());
        response.setSku(stock.getSku());
        response.setQuantity(stock.getQuantity());
        return response;
    }

}
