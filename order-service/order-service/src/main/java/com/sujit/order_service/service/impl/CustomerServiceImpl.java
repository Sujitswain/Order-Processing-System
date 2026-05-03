package com.sujit.order_service.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sujit.order_service.dto.CustomerResponse;
import com.sujit.order_service.entity.Customer;
import com.sujit.order_service.exception.OrderBadRequestException;
import com.sujit.order_service.repository.CustomerRepository;
import com.sujit.order_service.service.CustomerService;
import com.sujit.order_service.utils.ApplicationUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.sujit.order_service.utils.ApplicationUtil.mapToCustomerResponse;
import static com.sujit.order_service.utils.ApplicationUtil.mapToProductResponse;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    @Override
    public CustomerResponse createCustomer(String email, String firstName, String lastName, String phone,
                                          String address, String city, String country, String zipCode) {
        log.info("Creating customer with email: {}", email);

        if (customerRepository.findByEmail(email).isPresent()) {
            log.error("Customer with email {} already exists", email);
            throw new OrderBadRequestException("Customer with email " + email + " already exists");
        }

        Customer customer = Customer.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .address(address)
                .city(city)
                .country(country)
                .zipCode(zipCode)
                .createdAt(Instant.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created: id={}, email={}", savedCustomer.getId(), email);
        return mapToCustomerResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomer(UUID customerId) {
        log.info("Fetching customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new OrderBadRequestException(HttpStatus.NOT_FOUND, "Customer not found"));
        return mapToCustomerResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        log.info("Fetching all customers");
        return customerRepository.findAll().stream()
                .map(ApplicationUtil::mapToCustomerResponse)
                .toList();
    }

    @Override
    public Customer getCustomerEntity(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new OrderBadRequestException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @Transactional
    @Override
    public List<CustomerResponse> seedBulkCustomers() {
        log.info("Seeding customers from customers.json");
        List<CustomerResponse> seedCustomers = new java.util.ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("customers.json");
            List<Map<String, Object>> customersList = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> customerData : customersList) {
                String email = (String) customerData.get("email");
                String firstName = (String) customerData.get("firstName");
                String lastName = (String) customerData.get("lastName");
                String phone = (String) customerData.get("phone");
                String address = (String) customerData.get("address");
                String city = (String) customerData.get("city");
                String country = (String) customerData.get("country");
                String zipCode = (String) customerData.get("zipCode");

                try {
                    var existingCustomer = customerRepository.findByEmail(email);

                    if (existingCustomer.isPresent()) {
                        log.debug("Customer {} already exists, skipping", email);
                        seedCustomers.add(mapToCustomerResponse(existingCustomer.get()));
                    } else {
                        Customer customer = Customer.builder()
                                .email(email)
                                .firstName(firstName)
                                .lastName(lastName)
                                .phone(phone)
                                .address(address)
                                .city(city)
                                .country(country)
                                .zipCode(zipCode)
                                .createdAt(Instant.now())
                                .build();

                        Customer savedCustomer = customerRepository.save(customer);
                        log.debug("Created new customer: {}", email);
                        seedCustomers.add(mapToCustomerResponse(savedCustomer));
                    }
                } catch (Exception e) {
                    log.warn("Error processing customer {}: {}", email, e.getMessage());
                }
            }

            log.info("Seeding completed: {} customers processed", seedCustomers.size());
        } catch (IOException e) {
            log.error("Error loading customers.json file", e);
            throw new RuntimeException("Failed to load customers from JSON file", e);
        }

        return seedCustomers;
    }
}
