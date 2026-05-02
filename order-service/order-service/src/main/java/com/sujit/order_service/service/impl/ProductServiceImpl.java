package com.sujit.order_service.service.impl;

import com.sujit.order_service.dto.CreateProductRequest;
import com.sujit.order_service.dto.ProductResponse;
import com.sujit.order_service.entity.Product;
import com.sujit.order_service.entity.ProductStock;
import com.sujit.order_service.exception.OrderBadRequestException;
import com.sujit.order_service.repository.ProductRepository;
import com.sujit.order_service.repository.ProductStockRepository;
import com.sujit.order_service.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.sujit.order_service.utils.ApplicationUtil.mapToResponse;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductStockRepository productStockRepository) {
        this.productRepository = productRepository;
        this.productStockRepository = productStockRepository;
    }

    @Transactional
    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        // Check if product already exists
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            log.error("Product with SKU {} already exists", request.getSku());
            throw new OrderBadRequestException("Product with SKU " + request.getSku() + " already exists");
        }

        // Create product
        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .build();
        Product savedProduct = productRepository.save(product);

        // Create stock entry
        ProductStock stock = ProductStock.builder()
                .productId(savedProduct.getId())
                .sku(request.getSku())
                .quantity(request.getInitialQuantity())
                .build();
        productStockRepository.save(stock);

        log.info("Product created: id={}, sku={}, quantity={}", savedProduct.getId(), request.getSku(), request.getInitialQuantity());
        return mapToResponse(savedProduct, request.getInitialQuantity());
    }

    @Override
    public ProductResponse getProduct(UUID productId) {
        log.info("Fetching product: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new OrderBadRequestException(HttpStatus.NOT_FOUND, "Product not found"));

        ProductStock stock = productStockRepository.findByProductId(productId)
                .orElseGet(() -> {
                    ProductStock newStock = new ProductStock();
                    newStock.setProductId(productId);
                    newStock.setQuantity(0);
                    return newStock;
                });

        log.info("Retrieved stock for product {}: quantity={}", productId, stock.getQuantity());
        return mapToResponse(product, stock.getQuantity());
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(product -> {
                    ProductStock stock = productStockRepository.findByProductId(product.getId())
                            .orElseGet(() -> {
                                ProductStock newStock = new ProductStock();
                                newStock.setProductId(product.getId());
                                newStock.setQuantity(0);
                                return newStock;
                            });
                    return mapToResponse(product, stock.getQuantity());
                })
                .toList();
    }
}
