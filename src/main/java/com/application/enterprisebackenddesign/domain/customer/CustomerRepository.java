package com.application.enterprisebackenddesign.domain.customer;

import com.application.enterprisebackenddesign.infrastructure.persistence.customer.entity.CustomerEntity;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(Long id);

    Optional<Customer> findByEmail(String email);

    List<Customer> findAll();

    void deleteById(Long customerId);
}
