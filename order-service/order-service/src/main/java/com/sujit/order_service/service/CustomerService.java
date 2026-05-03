package com.sujit.order_service.service;

import com.sujit.order_service.dto.CustomerResponse;
import com.sujit.order_service.entity.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse createCustomer(String email, String firstName, String lastName, String phone,
                                   String address, String city, String country, String zipCode);

    CustomerResponse getCustomer(UUID customerId);

    List<CustomerResponse> getAllCustomers();

    List<CustomerResponse> seedBulkCustomers();

    Customer getCustomerEntity(UUID customerId);
}
