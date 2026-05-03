package com.sujit.order_service.service;

import com.sujit.order_service.dto.CreateProductRequest;
import com.sujit.order_service.dto.ProductResponse;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProduct(UUID productId);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> seedBulkProducts();
}
