package com.sujit.order_service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.sujit.order_service.utils.ApplicationUtil.mapToProductResponse;

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
        return mapToProductResponse(savedProduct, request.getInitialQuantity());
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
        return mapToProductResponse(product, stock.getQuantity());
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
                    return mapToProductResponse(product, stock.getQuantity());
                })
                .toList();
    }

    @Transactional
    @Override
    public List<ProductResponse> seedBulkProducts() {
        log.info("Seeding products from products.json");
        List<ProductResponse> seedProducts = new java.util.ArrayList<>();

        try {
            // Load products from JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("products.json");
            List<Map<String, Object>> productsList = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> productData : productsList) {
                String sku = (String) productData.get("sku");
                String name = (String) productData.get("name");
                double price = ((Number) productData.get("price")).doubleValue();
                String description = (String) productData.get("description");
                int quantity = ((Number) productData.get("initialQuantity")).intValue();

                try {
                    // Check if product exists
                    var existingProduct = productRepository.findBySku(sku);

                    if (existingProduct.isPresent()) {
                        // Update stock to quantity specified in JSON
                        Product product = existingProduct.get();
                        ProductStock stock = productStockRepository.findByProductId(product.getId())
                                .orElseGet(() -> {
                                    ProductStock newStock = new ProductStock();
                                    newStock.setProductId(product.getId());
                                    newStock.setSku(sku);
                                    return newStock;
                                });
                        stock.setQuantity(quantity);
                        stock.setReservedQuantity(0);
                        productStockRepository.save(stock);
                        log.debug("Updated product {} stock to {}", sku, quantity);
                        seedProducts.add(mapToProductResponse(product, quantity));
                    } else {
                        // Create new product
                        Product product = Product.builder()
                                .sku(sku)
                                .name(name)
                                .price(BigDecimal.valueOf(price))
                                .description(description)
                                .build();
                        Product savedProduct = productRepository.save(product);

                        ProductStock stock = ProductStock.builder()
                                .productId(savedProduct.getId())
                                .sku(sku)
                                .quantity(quantity)
                                .reservedQuantity(0)
                                .build();
                        productStockRepository.save(stock);
                        log.debug("Created new product {} with stock {}", sku, quantity);
                        seedProducts.add(mapToProductResponse(savedProduct, quantity));
                    }
                } catch (Exception e) {
                    log.warn("Error processing product {}: {}", sku, e.getMessage());
                }
            }

            log.info("Seeding completed: {} products processed", seedProducts.size());
        } catch (IOException e) {
            log.error("Error loading products.json file", e);
            throw new RuntimeException("Failed to load products from JSON file", e);
        }

        return seedProducts;
    }
}
